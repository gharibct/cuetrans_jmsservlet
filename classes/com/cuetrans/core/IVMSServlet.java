package com.cuetrans.core;

import com.bct.bpms.client.rest.RestClientApi;
import com.bpms.core.webservice.WebserviceUtility;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

@Path("/IVMSServlet")
public class IVMSServlet
{

  @Context
  private HttpServletRequest httpRequest;

  @Context
  private HttpServletResponse httpResponse;

  @Context
  private ServletContext context;

  @Context
  private ServletConfig servletConfig;
  private Properties prop = null;
  final GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
  Gson gson = this.gsonBuilder.create();

  @POST
  @Path("/getJourneyPlans")
  @Produces({"text/plain"})
  public Response getJourneyPlans()
  {
    JsonObject inputObject = new JsonObject();
    Map inputMap = new HashMap();

    JsonObject result = new JsonObject();
    String jsonStr=null;
    try
    {
      String rest = getPropertyValues("rest");

      if ((rest != null) && (rest.equalsIgnoreCase("Y"))) {
        result = invokeWorkFlow("JourneyPlanDetails", inputObject);
        jsonStr = this.gson.toJson(result);
      } else {
        inputMap.put("strTenantId", getPropertyValues("tenantId"));
        jsonStr = invokeWorkFlow("JourneyPlanDetails", inputMap);
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println("result = " + result);
    System.out.println("jsonStr = "+jsonStr);
  
    return getReponse(Response.Status.CREATED, jsonStr, "application/json");
  }

  private String invokeWorkFlow(String workFlowName, Map args) {
    JsonParser jp = new JsonParser();

    String jsonObject = null;
    Map result = null;
    try {
      String port = getPropertyValues("Port");
      String ipAddress = getPropertyValues("IPAddress");
      long iTimeOut = Long.parseLong(getPropertyValues("TimeOut"));
      com.bpms.engine.util.WebserviceAttachmentInfo.tenantId = args.get("strTenantId").toString();

      result = WebserviceUtility.executeWorkFlow(workFlowName, ipAddress, port, (HashMap)args);
      
      System.out.println("result = " + result.get("journeyPlanDetails"));   
      jsonObject=(String) result.get("journeyPlanDetails");
    }
    catch (Exception e)
    {
      System.out.println(":: Exception while calling the workflow " + workFlowName + "\n" + e.getMessage());
    }
    return jsonObject;
  }

  private JsonObject invokeWorkFlow(String workFlowName, JsonObject args) {
    JsonParser jp = new JsonParser();

    JsonObject jsonObject = null;
    try
    {
      String restUrl = getPropertyValues("restUrl");

      RestClientApi restClientApi = new RestClientApi(restUrl);
      String tenantId = getPropertyValues("tenantId");

      String result = restClientApi.executeService(tenantId, tenantId, workFlowName, args);

      System.out.println("Complete result >>> " + result);

      if (!result.isEmpty()) {
        jsonObject = (JsonObject)jp.parse(result);

        return jsonObject;
      }
    } catch (Exception e) {
      System.out.println(":: Exception while calling the workflow " + workFlowName + "\n" + e.getMessage());
    }
    return jsonObject;
  }

  public void changeNullStringsToNull(JsonElement je) {
    if ((je instanceof JsonObject)) {
      changeNullStringsToNull((JsonObject)je);
    } else if ((je instanceof JsonArray)) {
      JsonArray ja = (JsonArray)je;
      int size = ja.size();
      for (int i = 0; i < size; i++)
        changeNullStringsToNull(ja.get(i));
    }
  }

  private String getPropertyValues(String key) throws IOException {
    if (this.prop == null) {
      this.prop = new Properties();
      String propFileName = "./config.properties";
      InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
      this.prop.load(inputStream);
      if (inputStream == null) {
        throw new FileNotFoundException("Property File" + propFileName + "Not Found");
      }
    }
    String value = this.prop.getProperty(key);
    return value;
  }

  public static Response getReponse(Response.Status status, String message, String contentType) {
    Response.ResponseBuilder responseBuilder = Response.status(status);
    responseBuilder.type(contentType);
    responseBuilder.entity(message);
    responseBuilder.header("Access-Control-Allow-Origin", "*");
    responseBuilder.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
    responseBuilder.header("Access-Control-Allow-Credentials", "true");
    responseBuilder.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    responseBuilder.header("Access-Control-Max-Age", "1209600");
    responseBuilder.header("Keep-Alive", "timeout=10, max=100");
    responseBuilder.header("Connection", "close");
    return responseBuilder.build();
  }
}