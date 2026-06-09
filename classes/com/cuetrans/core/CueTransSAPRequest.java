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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

@WebServlet({"/CueTransSAPRequest"})

public class CueTransSAPRequest extends HttpServlet {
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
    String wfName ="";
    Date date = new Date();
    System.out.println("Request: " + request.getParameterNames().toString());
    String param = (String) request.getParameterNames().nextElement();
    System.out.println("param: " + param);
    //System.out.println("param1: " + param.replace("xml", "xml version='1.0' encoding='ISO-8859-1' standalone='no' "));
    param =param.replace("xml", "xml version='1.0' encoding='ISO-8859-1' standalone='no' ");
    JSONObject inputParamObject = JSONObject.fromObject(param);
    String workFlowName = inputParamObject.getString("workFlowName");
    String workFlowParams = inputParamObject.getString("workFlowParams");
    wfName = workFlowName;
   
    /*
    String workFlowName = request.getParameter("workFlowName");
    String workFlowParams = request.getParameter("workFlowParams");
    */
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
        
        Id = java.net.URLDecoder.decode(Id);
        pwd = java.net.URLDecoder.decode(pwd);
        
        String userId="",password="";
        try {
        	 SecretKey encryptionKey = DataEncryptUtility.getSecretEncryptionKey("C1U245T@#%R*)^");
             byte[] base64UserId = DatatypeConverter.parseBase64Binary(Id);
             byte[] base64Pwd = DatatypeConverter.parseBase64Binary(pwd);
             userId = DataEncryptUtility.decryptText(base64UserId, encryptionKey);
             password = DataEncryptUtility.decryptText(base64Pwd, encryptionKey);    
             
             //String userId = Id;
             //String password = pwd;
             
             System.out.println("Decrypted userid" + userId + "\n" + "Decrypted Pwd" + password);
			
		} catch (Exception e) {
			// TODO: handle exception
			result.put("strFailureMsg", "Encryption failed.Please contact system administrator.");
			e.printStackTrace();
		}       
        System.out.println("Workflowname" + workFlowName);
        
        String strDoNo ="";
        if(workFlowName.equals("PDOSAPDOVALIDATION")){
        	 workFlowName ="PDOSAPUSERVALIDATION";
        	 strDoNo = inputObject.getString("strDoNo");  
        	 inputObject.put("strDocNo", strDoNo);
        }
       
		System.out.println("strDoNo" + strDoNo );
		System.out.println("workFlowName" + workFlowName );
        
        if (userId != "" && password != ""){        		
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
            //result.put("strSuccessMsg", "Data posted successfully.");
            System.out.println("wfName" + wfName );
            if(wfName.equals("PDOSAPDOVALIDATION"))
            {
            	if(strDoNo == "" || strDoNo == null || strDoNo == "null" || strDoNo.isEmpty()){
            		result.put("strFailureMsg", "Delivery order number is blank.");  
            	}
            	else{
            		//inputObject.remove("methodName");
            		//inputObject.put("methodName", "onenterDocNoTS");

            		System.out.println("workFlowParams" + workFlowParams );
            		JSONObject inObj = JSONObject.fromObject(workFlowParams);
                     
            		inObj.put("strOrgId", "Admin");
            		inObj.put("strLangId", "1");
            		inObj.put("strRoleId", "SuperRole");
            		inObj.put("strDeptId", "Admin");
            		inObj.put("strTenantId", tenantIdFromProp);
            		inObj.put("strDocNo", strDoNo);
            		inObj.put("methodName", "PDOSAPDOVALIDATION"); 
            		workFlowParamsMap = HashMapJSONParser.convertJSONObjectToHashMap(inObj);
            		System.out.println("workFlowParamsMap" + workFlowParamsMap );
            		result = invokeWorkFlow("TMSCoreTransportTS", workFlowParamsMap);
            		System.out.println("------result---------" + result);
            	}
            	          	
       	 	}
            
        }
        
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
        if (!key.equals("UID") && !key.equals("hdrcache") && !key.equals("grid_array") && !key.equals("strSuccessMsg") && !key.equals("strFailureMsg") && !key.equals("strSessionError"))
          continue;
        /*
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
        */
        if (key.equals("hdrcache")) {
            JSONObject hdrCacheJsonObj;
            JSONArray hdrCacheArray;
            System.out.println("Admin" + responseData.get(key).toString());
            if (responseData.get(key).toString().equalsIgnoreCase("null") || responseData.get(key).toString().equalsIgnoreCase("[]")) {
              hdrCacheArray = new JSONArray();
              hdrCacheJsonObj = new JSONObject();
            } else {
              hdrCacheArray = (JSONArray)responseData.get(key);
              if (!((JSONObject)hdrCacheArray.get(0)).isEmpty()) {
                hdrCacheJsonObj = (JSONObject)hdrCacheArray.get(0);
              } else {
                hdrCacheJsonObj = new JSONObject();
              } 
            }
            hdrCacheArray.clear();
            hdrCacheArray.add(0, hdrCacheJsonObj);
            responseObj.put(key, hdrCacheArray);
            System.out.println("key inside HdrCache2:" + key.toString());
            continue;
          } 
        
        if (key.equals("strFailureMsg")) {
        	System.out.println("------strFailureMsg---------" + key);
        	System.out.println("------strFailureMsg---------" + responseData.get(key).toString());
        	if(responseData.get(key).toString() == "" || responseData.get(key).toString() == null || responseData.get(key).toString() == "null" )
        	responseObj.put("strSuccessMsg", "Data posted successfully.");
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
