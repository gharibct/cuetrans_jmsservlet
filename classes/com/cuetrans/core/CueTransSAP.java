package com.cuetrans.core;
import com.bpms.core.webservice.WebserviceUtility;
import com.bpms.engine.util.WebserviceAttachmentInfo;
import com.cuetrans.core.ActionHandlerNew;
import com.cuetrans.core.CueTransSAP;
import com.cuetrans.core.DataEncryptUtility;
import com.cuetrans.utils.HashMapJSONParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class CueTransSAP extends HttpServlet {
  private static final long serialVersionUID = 1L;
  
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }
  
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String userName = "";
    String orgName = "";
    String langId = "";
    String roleId = "";
    String tenantId = "";
    String strUserId = "";
    String strDeptId = "";
    String strPassword = "";
    String initTenantId = "";
    String strMethodName = "";
    String answer = "";
    Date date = new Date();
    String workFlowName = request.getParameter("workFlowName");
    String workFlowParams = request.getParameter("workFlowParams");
    JSONObject responseData = new JSONObject();
    Map<Object, Object> result = new HashMap<Object, Object>();
    boolean sessionFlag = true;
    ActionHandlerNew actionHandlerNew = new ActionHandlerNew();
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0L);
    System.out.println("Session Flag" + sessionFlag);
    if (sessionFlag) {
      Map<Object, Object> workFlowParamsMap = new HashMap<Object, Object>();
      try {
        JSONObject inputObject = JSONObject.fromObject(workFlowParams);
        String Id = inputObject.getString("strUserId");
        String pwd = inputObject.getString("strPassword");
        SecretKey encryptionKey = DataEncryptUtility.getSecretEncryptionKey("C1U245T@#%R*)^");
        byte[] base64UserId = DatatypeConverter.parseBase64Binary(Id);
        byte[] base64Pwd = DatatypeConverter.parseBase64Binary(pwd);
        String userId = DataEncryptUtility.decryptText(base64UserId, encryptionKey);
        String password = DataEncryptUtility.decryptText(base64Pwd, encryptionKey);
        System.out.println("Decrypted userid" + userId + "\n" + "Decrypted Pwd" + password);
        System.out.println("Workflowname" + workFlowName);
        boolean bTaskPermission = true;
        inputObject.remove("strUserId");
        inputObject.remove("strPassword");
        inputObject.put("strUserId", userId);
        inputObject.put("strPassword", password);
        String tenantIdFromProp = actionHandlerNew.getPropertyValues("tenantId");
        if (tenantIdFromProp == null || tenantIdFromProp.isEmpty())
          tenantIdFromProp = "cuecent_tenant"; 
        inputObject.put("CUECENT_OWNER_TENANTID_2015", tenantIdFromProp);
        workFlowParamsMap = HashMapJSONParser.convertJSONObjectToHashMap(inputObject);
        System.out.println("workflowparams" + workFlowParamsMap);
        result = invokeWorkFlow(workFlowName, workFlowParamsMap);
        System.out.println("result" + result);
        JSONObject sessionParmObject = HashMapJSONParser.convertHashMapToJSONObject(result);
        strUserId = inputObject.getString("strUserId");
        result.put("strSuccessMsg", "Data posted successfully.");
      } catch (Exception e) {
    	result.put("strFailureMsg", "Error in Execution.Please contact system administrator.");
        System.out.println("## Exception while parsing input parameters into a JSON Object ##" + e);
        e.printStackTrace();
      } 
    } 
    responseData = HashMapJSONParser.convertHashMapToJSONObject(result);
    System.out.println("------reponseData---------" + responseData);
    JSONObject responseObj = new JSONObject();
    Iterator<String> iter = responseData.keys();
    while (iter.hasNext()) {
      String key = iter.next();
      try {
        if (!key.equals("UID") && !key.equals("hdrcache") && key.indexOf("_array") <= 0 && !key.equals("strSuccessMsg") && !key.equals("strFailureMsg") && key.indexOf("_svgdata") <= 0 && !key.equals("strSessionError"))
          continue; 
        if (key.equals("hdrcache")) {
          System.out.println("Admin" + responseData.get(key).toString());
          if (responseData.get(key).toString().equalsIgnoreCase("null") || responseData.get(key).toString().equalsIgnoreCase("[]")) {
            JSONArray jSONArray = new JSONArray();
            JSONObject jSONObject = new JSONObject();
            continue;
          } 
          JSONArray hdrCacheArray = (JSONArray)responseData.get(key);
          if (!((JSONObject)hdrCacheArray.get(0)).isEmpty()) {
            JSONObject jSONObject = (JSONObject)hdrCacheArray.get(0);
            continue;
          } 
          JSONObject hdrCacheJsonObj = new JSONObject();
          continue;
        } 
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
    try {
      ActionHandlerNew actionHandlerNew = new ActionHandlerNew();
      String port = actionHandlerNew.getPropertyValues("Port");
      String ipAddress = actionHandlerNew.getPropertyValues("IPAddress");
      long iTimeOut = Long.parseLong(actionHandlerNew.getPropertyValues("TimeOut"));
      WebserviceAttachmentInfo.tenantId = args.get("strTenantId").toString();
      result = WebserviceUtility.executeWorkFlow(workFlowName, ipAddress, port, (HashMap)args, iTimeOut);
    } catch (Exception e) {
      System.out.println(":: Exception while calling the workflow " + workFlowName + "\n" + e.getMessage());
    } 
    return result;
  }
  
  public String getPropertyValues(String key) throws IOException {
    Properties prop = new Properties();
    String propFileName = "./config.properties";
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
    prop.load(inputStream);
    if (inputStream == null)
      throw new FileNotFoundException("Property File" + propFileName + "Not Found"); 
    String value = prop.getProperty(key);
    return value;
  }
  
  private String getUUID() {
    String iUID = UUID.randomUUID().toString();
    return iUID;
  }
  
  private Map callTenantService(String workFlowParams, String workFlowName, JSONObject inputObject) {
    Map<Object, Object> result = new HashMap<Object, Object>();
    Map<Object, Object> workFlowParamsMap = new HashMap<Object, Object>();
    ActionHandlerNew actionHandlerNew = new ActionHandlerNew();
    String tenantIdFromProp = "";
    try {
      tenantIdFromProp = actionHandlerNew.getPropertyValues("tenantId");
    } catch (IOException e) {
      System.out.println(e.getMessage());
    } 
    if (tenantIdFromProp == null || tenantIdFromProp.isEmpty())
      tenantIdFromProp = "cuecent_tenant"; 
    inputObject.put("strTenantId", tenantIdFromProp);
    workFlowParamsMap = HashMapJSONParser.convertJSONObjectToHashMap(inputObject);
    System.out.println("Before Invoking CoreTenantService Tenant Id : " + tenantIdFromProp);
    System.out.println("Before Invoking Workflow : " + workFlowName);
    result = invokeWorkFlow("CoreTenantService", workFlowParamsMap);
    return result;
  }
}
