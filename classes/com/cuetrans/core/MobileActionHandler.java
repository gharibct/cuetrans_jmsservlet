package com.cuetrans.core;

import com.bpms.core.webservice.WebserviceUtility;
import com.bpms.engine.util.WebserviceAttachmentInfo;
import com.cuetrans.core.MobileActionHandler;
import com.cuetrans.utils.HashMapJSONParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

public class MobileActionHandler {
  public static void mobileActionHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userName = "";
    String orgName = "";
    String langId = "";
    String roleId = "";
    String tenantId = "";
    String strUserId = "";
    String strPassword = "";
    String initTenantId = "";
    String token = "";
    System.out.println("Request: " + request.getParameterNames());
    String param = (String) request.getParameterNames().nextElement();
    System.out.println("param: " + param);
    JSONObject inputParamObject = JSONObject.fromObject(param);
    String workFlowName = inputParamObject.getString("workFlowName");
    String workFlowParams = inputParamObject.getString("workFlowParams");
    String processType = "";
    JSONObject responseData = new JSONObject();
    Map<Object, Object> result = new HashMap<Object, Object>();
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0L);
    System.out.println("response header");
    Map<Object, Object> workFlowParamsMap = new HashMap<Object, Object>();  
    JSONObject inputObject = JSONObject.fromObject(workFlowParams);
    System.out.println("inputObject: " + inputObject);
    String strMethodName = inputObject.getString("methodName");
    if (workFlowName.equals("CoreTenantService")) {
      JSONObject LoginInputObject = JSONObject.fromObject(workFlowParams);
      strUserId = inputObject.getString("strUserId");
      System.out.println("LoginInputObject" + LoginInputObject);
      //tenantId = "cuecent_tenant";
      final MobileActionHandler mobileActionHandler = new MobileActionHandler();
      tenantId = mobileActionHandler.getPropertyValues("tenantId");
      LoginInputObject.put("strTenantId", tenantId);
      LoginInputObject.put("methodName", "MOBILELOGIN");
      LoginInputObject.put("strUserId", strUserId);
      workFlowParamsMap = HashMapJSONParser.convertJSONObjectToHashMap(LoginInputObject);
      result = invokeWorkFlow("CoreLoginService", workFlowParamsMap);
    } else if (workFlowName.equals("CoreLoginService") && strMethodName.equals("USER_RESETPASSWORD")) {
      System.out.println("OTOAPP_RESETPASSWORD");
      JSONObject LoginInputObject = JSONObject.fromObject(workFlowParams);
      strUserId = inputObject.getString("strUserId");
      System.out.println("LoginInputObject" + LoginInputObject);
      tenantId = "cuecent_tenant";
      LoginInputObject.put("strTenantId", tenantId);
      LoginInputObject.put("methodName", "USER_RESETPASSWORD");
      LoginInputObject.put("strUserId", strUserId);
      workFlowParamsMap = HashMapJSONParser.convertJSONObjectToHashMap(LoginInputObject);
      result = invokeWorkFlow("CoreLoginService", workFlowParamsMap);
    } else {
      JSONObject SessionInputObject = JSONObject.fromObject(workFlowParams);
      final MobileActionHandler mobileActionHandler = new MobileActionHandler();
      tenantId = mobileActionHandler.getPropertyValues("tenantId");
      //SessionInputObject.put("strTenantId", "cuecent_tenant");
      SessionInputObject.put("strTenantId", tenantId);
      strUserId = inputObject.getString("strUserId");
      System.out.println(inputObject.get("strToken"));
      System.out.println(inputObject.get("strToken").toString());
      
      System.out.println("fetchloginsession" + inputObject);
      
      
      token = inputObject.get("strToken").toString();
      SessionInputObject.put("strToken", token);
      SessionInputObject.put("strUserId", strUserId);
      SessionInputObject.put("methodName", "fetchloginsession");
      System.out.println("SessionInputObject" + SessionInputObject);
      workFlowParamsMap = HashMapJSONParser.convertJSONObjectToHashMap(SessionInputObject);
      result = invokeWorkFlow("CoreLoginService", workFlowParamsMap);
      String s = "0";
      s = result.get("errorFlag").toString();
      System.out.println("fetchloginsession ErrorFlag:" + s);
      System.out.println("result1 =" + result);
      if (s.equals("0")) {
        JSONObject sessionParmObject = HashMapJSONParser.convertHashMapToJSONObject(result);
        String hdrcache = sessionParmObject.getString("hdrcache");
        System.out.println("hdrcache" + hdrcache);
        if (hdrcache.contains("["))
          hdrcache = hdrcache.replace("[", ""); 
        if (hdrcache.contains("]"))
          hdrcache = hdrcache.replace("]", ""); 
        if (hdrcache.contains("{") && hdrcache.contains("}"))
          sessionParmObject = JSONObject.fromObject(hdrcache); 
        System.out.println("sessionParmObject" + sessionParmObject);
        if ((hdrcache.contains("strToken") && hdrcache.contains("strUserId")) || StringUtils.isNotEmpty((String)sessionParmObject.get("strToken"))) {
          System.out.println("sessionResult if" + sessionParmObject);
          orgName = sessionParmObject.get("strOrgId").toString();
          langId = sessionParmObject.get("strLangId").toString();
          roleId = sessionParmObject.get("strRoleId").toString();
          strUserId = sessionParmObject.get("strUserId").toString();
          System.out.println("User Id" + strUserId);
          tenantId = sessionParmObject.get("strTenantId").toString();
          System.out.println("tenantId" + tenantId);
          token = sessionParmObject.get("strToken").toString();
          inputObject.put("strUserId", strUserId);
          inputObject.put("strOrgId", orgName);
          inputObject.put("strLangId", langId);
          inputObject.put("strRoleId", roleId);
          inputObject.put("strTenantId", tenantId);
          System.out.println("inputobject1" + inputObject);
          workFlowParamsMap = HashMapJSONParser.convertJSONObjectToHashMap(inputObject);
          System.out.println("inputobject" + inputObject);
          result = invokeWorkFlow(workFlowName, workFlowParamsMap);
        } else {
          result.put("strSessionError", "Session Is invalid");
        } 
      } 
    } 
    System.out.println("result1 =" + result);
    responseData = HashMapJSONParser.convertHashMapToJSONObject(result);
    System.out.println("responseData =" + responseData);
    JSONObject responseObj = new JSONObject();
    Iterator<String> iter = responseData.keys();
    while (iter.hasNext()) {
      String key = iter.next();
      System.out.println("key" + key.toString());
      try {
        if (key.equals("UID") || key.equals("hdrcache") || key.equals("retFilePath") || key.equals("OutFileName") || key.indexOf("_array") > 0 || key.equals("strSuccessMsg") || key.equals("strFailureMsg") || key.indexOf("_svgdata") > 0 || key.equals("strSessionError"))
          responseObj.put(key, responseData.get(key)); 
      } catch (JSONException e) {
        responseObj.put("strSessionError", "Compilation Error");
      } 
    } 
    try {
      response.getWriter().write(responseObj.toString());
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public static Map invokeWorkFlow(String workFlowName) {
    Map result = null;
    Map<Object, Object> inputParams = new HashMap<Object, Object>();
    result = invokeWorkFlow(workFlowName, inputParams);
    return result;
  }
  
  private static Map invokeWorkFlow(String workFlowName, Map args) {
    Map result = null;
    System.out.println(":: Started Calling workflow - " + workFlowName);
    try {
      MobileActionHandler mobileActionHandler = new MobileActionHandler();
      String port = mobileActionHandler.getPropertyValues("Port");
      String ipAddress = mobileActionHandler.getPropertyValues("IPAddress");
      WebserviceAttachmentInfo.tenantId = args.get("strTenantId").toString();
      result = WebserviceUtility.executeWorkFlow(workFlowName, ipAddress, port, (HashMap)args);
      System.out.println(":: Completed Calling workflow - " + workFlowName);
    } catch (Exception e) {
      System.out.println(":: Exception while calling the workflow " + workFlowName + "\n" + e.getMessage());
    } 
    return result;
  }
  
  private String getPropertyValues(String key) throws IOException {
    Properties prop = new Properties();
    String propFileName = "./config.properties";
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
    prop.load(inputStream);
    if (inputStream == null)
      throw new FileNotFoundException("Property File" + propFileName + "Not Found"); 
    String value = prop.getProperty(key);
    return value;
  }
}
