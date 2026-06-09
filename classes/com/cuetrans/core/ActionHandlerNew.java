package com.cuetrans.core;

import com.bpms.core.webservice.WebserviceUtility;
import com.bpms.engine.util.WebserviceAttachmentInfo;
import com.cuetrans.utils.HashMapJSONParser;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import nl.captcha.Captcha;
import org.apache.commons.lang.StringUtils;

public class ActionHandlerNew {
   private static final Logger LOGGER = Logger.getLogger(ActionHandlerNew.class.getName());

   static {
      try {
         FileHandler fileHandler = new FileHandler("CuetransLogin%g", 5242880, 10, true);
         fileHandler.setFormatter(new SingleLineLogFormatter());
         LOGGER.addHandler(fileHandler);
      } catch (SecurityException var1) {
         // // System.out.println("File Handler could not be created");
      } catch (IOException var2) {
         // // System.out.println("File Handler could not be created");
      }

   }

   public static void actionHandle(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
      String processType = request.getParameter("processType");
      String authToken = request.getParameter("AuthToken");
      String strIPAddr = request.getHeader("X-FORWARDED-FOR");

      System.out.println("\n\n**********Received Request for Workflow: " + workFlowName);
      System.out.println("\n\n**********Received Request Parameters: " + workFlowParams);

      boolean methodForSkipValidate = false;
      if (strIPAddr == null) {
         strIPAddr = request.getRemoteAddr();
      }

      new JSONObject();
      Map result = new HashMap();
      boolean sessionFlag = true;
      ActionHandlerNew actionHandlerNew = new ActionHandlerNew();
      response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
      response.setHeader("Pragma", "no-cache");
      response.setDateHeader("Expires", 0L);
      if (workFlowName.equals("Logout")) {
         HttpSession session = request.getSession(false);
         // // System.out.println("Logout");
         strUserId = session.getAttribute("UserName").toString();
         session.invalidate();
         sessionFlag = false;
         result.put("strSuccessMsg", "Logged Out Successfully");
         LOGGER.log(Level.INFO, strIPAddr + "," + strUserId + "," + "LOGOUT");
      }

      if (processType != null && processType.equals("ForgotPassword")) {
         // // System.out.println("Forgot Password");
         sessionFlag = false;
         JSONObject inputObject = JSONObject.fromObject(workFlowParams);
         result = actionHandlerNew.callTenantService(workFlowParams, workFlowName, inputObject);
         strUserId = inputObject.getString("strUserId");
         // // System.out.println("After Invoking ForgotPassword User Id : " +
         // strUserId);
         String s = result.get("errorFlag").toString();
         // // System.out.println("s-ErrorFlag:" + s);
         if (s.equals("0")) {
            JSONObject resetPwdInputObject = JSONObject.fromObject(workFlowParams);
            new HashMap();
            // // System.out.println("Before Invoking ForgotPassword Result : " + result);
            tenantId = "cuecent_tenant";
            // // System.out.println("After Invoking ForgotPassword Tenant Id : " +
            // tenantId);
            resetPwdInputObject.put("strTenantId", tenantId);
            resetPwdInputObject.put("strUserId", strUserId);
            resetPwdInputObject.put("methodName", "validateUserPassword");
            resetPwdInputObject.put("CUECENT_OWNER_TENANTID_2015", inputObject.get("strTenantId"));
            Map workFlowParamsMap = HashMapJSONParser.convertJSONObjectToHashMap(resetPwdInputObject);
            // // System.out.println("Before invoking Reset Password");
            result = (HashMap) invokeWorkFlow("CoreAdminService", workFlowParamsMap);
            // // System.out.println("After invoking Reset Password");
         }
      }

      if (!workFlowName.equals("CoreTenantService")) {
         HttpSession session = request.getSession(false);
         System.out.println("session = " + session);
         if (session != null && session.getAttribute("TenantId") == null) {
            session = null;
         }

         if (session == null) {
            try {
               String sessionmethods = actionHandlerNew.readSesValPropFile("SessionMethodNames");
               if (!workFlowParams.isEmpty()) {
                  for (String str : Arrays.asList(sessionmethods.split(","))) {
                     // // System.out.println("Verifying:" + str);
                     if (workFlowParams.contains(str)) {
                        methodForSkipValidate = true;
                     }
                  }
               }

               // // System.out.println("methodForSkipValidate:" + methodForSkipValidate);
               if (methodForSkipValidate) {
                  sessionFlag = true;
                  session = request.getSession(true);
                  session.setAttribute("UserName", "SuperUser");
                  session.setAttribute("AuthToken", "Dummy");
                  session.setAttribute("LangId", "1");
                  session.setAttribute("RoleId", "SuperRole");
                  session.setAttribute("strDeptId", "Admin");
                  session.setAttribute("OrgName", "Admin");
                  session.setAttribute("strLangSuffix", "strLangSuffix");
                  session.setAttribute("strDeptId", "Admin");
                  session.setAttribute("UserType", "Admin");
               } else {
                  result.put("strSessionError", "Session Timeout");
                  sessionFlag = false;
               }
            } catch (IOException e1) {
               result.put("strFailureMsg", "System Error. Contact Administrator. Error Code: S001");
               // // System.out.println(e1.getMessage());
            }
         }
      } else {
         String strCaptchaValidate = "Y";

         try {
            strCaptchaValidate = actionHandlerNew.getPropertyValues("CaptchaValidate");
         } catch (IOException e1) {
            result.put("strFailureMsg", "System Error. Contact Administrator. Error Code: S002");
            // // System.out.println(e1.getMessage());
         }

         if (!strCaptchaValidate.equals("N")) {
            HttpSession session = request.getSession(false);
            Captcha captcha = (Captcha) session.getAttribute("simpleCaptcha");
            JSONObject inputObject = JSONObject.fromObject(workFlowParams);
            answer = inputObject.getString("answer");
            // // System.out.println("captcha" + captcha);

            try {
               request.setCharacterEncoding("UTF-8");
               // // System.out.println("answer" + answer);
               if (captcha.isCorrect(answer)) {
                  // // System.out.println(" Correct Captcha Code !");
               } else {
                  // // System.out.println("Please enter a valid Captcha value");
                  new JSONObject();
                  result.put("strFailureMsg", "Please enter a valid Captcha value");
                  sessionFlag = false;
               }
            } catch (Exception e) {
               result.put("strFailureMsg", "System Error. Contact Administrator. Error Code: S003");
               e.printStackTrace();
            }
         }
      }

      System.out.println("Session Flag before execution: " + sessionFlag);
      if (sessionFlag) {
         new HashMap();

         try {
            JSONObject inputObject = JSONObject.fromObject(workFlowParams);
            if (workFlowName.equals("CoreTenantService")) {
               // // System.out.println("Inside core Tenant Service");
               HttpSession session = request.getSession(true);
               result = actionHandlerNew.callTenantService(workFlowParams, workFlowName, inputObject);
               session.setAttribute("result", result);
               // // System.out.println("After Invoking Workflow : " + workFlowName);
               strUserId = inputObject.getString("strUserId");
               initTenantId = inputObject.getString("strTenantId");
               // // System.out.println("Before Invoking CoreTenantService User Id : " +
               // strUserId);
               String s = result.get("errorFlag").toString();
               // // System.out.println("s-ErrorFlag:" + s);
               if (s.equals("0")) {
                  JSONObject LoginInputObject = JSONObject.fromObject(workFlowParams);
                  // // System.out.println("IP Address : " + strIPAddr);
                  tenantId = actionHandlerNew.getPropertyValues("tenantId");
                  LoginInputObject.put("strTenantId", tenantId);
                  LoginInputObject.put("methodName", "validateLogin");
                  LoginInputObject.put("strUserId", strUserId);
                  LoginInputObject.put("strIPAddr", strIPAddr);
                  LoginInputObject.put("CUECENT_OWNER_TENANTID_2015", inputObject.get("strTenantId"));
                  Map var69 = HashMapJSONParser.convertJSONObjectToHashMap(LoginInputObject);
                  // // System.out.println("Before invoking ValidateLogin");
                  result = (HashMap) invokeWorkFlow("CoreLoginService", var69);
                  session.setAttribute("result", result);
                  // // System.out.println("After invoking ValidateLogin");
                  session.setAttribute("TenantId", tenantId);
                  session.setAttribute("result", result);
                  String res = result.get("errorFlag").toString();
                  // // System.out.println("login result errorflag" + res);
                  if (res.equals("0")) {
                     LOGGER.log(Level.INFO, strIPAddr + "," + strUserId + "," + "SUCCESS");
                     // // System.out.println("Login Successful");
                     userName = result.get("strUserId").toString();
                     // // System.out.println("Login username : " + userName);
                     orgName = result.get("strOrgId").toString();
                     // // System.out.println("Login Org Id : " + orgName);
                     langId = result.get("strLangId").toString();
                     // // System.out.println("Login Lang : " + langId);
                     roleId = result.get("strRoleId").toString();
                     // // System.out.println("Login Role : " + roleId);
                     strDeptId = result.get("strDeptId").toString();
                     // // System.out.println("Login Dept : " + strDeptId);
                     session.setAttribute("UserName", userName);
                     session.setAttribute("OrgName", orgName);
                     session.setAttribute("LangId", langId);
                     session.setAttribute("RoleId", roleId);
                     session.setAttribute("TenantId", tenantId);
                     session.setAttribute("strDeptId", strDeptId);
                  } else {
                     LOGGER.log(Level.INFO, strIPAddr + "," + strUserId + "," + "FAILED");
                     result.put("strSessionError", "Login Failed. Tenant Error");
                     sessionFlag = false;
                     // // System.out.println("Session Flag" + sessionFlag);
                  }
               } else {
                  LOGGER.log(Level.INFO, strIPAddr + "," + strUserId + "," + "FAILED");
                  result.put("strSessionError", "Login Failed");
                  sessionFlag = false;
                  // // System.out.println("Session Flag" + sessionFlag);
               }
            } else {
               // // System.out.println("Workflowname" + workFlowName);
               HttpSession session = request.getSession(false);
               boolean bTaskPermission = false;
               // // System.out.println("Input Auth token:" + authToken);
               System.out.println("methodForSkipValidate: " + methodForSkipValidate);
               if (!methodForSkipValidate) {
                  // // System.out.println("Session Auth token:" +
                  // session.getAttribute("hdnAuthToken").toString());
                  if (!authToken.equals(session.getAttribute("hdnAuthToken").toString())) {
                     result.put("strSessionError", "Authentication Failed");
                     sessionFlag = false;
                  }
               }

               tenantId = actionHandlerNew.getPropertyValues("tenantId");
               roleId = session.getAttribute("RoleId").toString();
               if (actionHandlerNew.getPropertyValues("TaskValidate") == "Y" && sessionFlag) {
                  JSONObject TaskInputObject = JSONObject.fromObject(workFlowParams);
                  strMethodName = TaskInputObject.getString("methodName");
                  TaskInputObject.put("strTenantId", tenantId);
                  TaskInputObject.put("methodName", "validateTask");
                  TaskInputObject.put("strMethodName", strMethodName);
                  TaskInputObject.put("strRoleId", roleId);
                  String tenantIdFromProp = actionHandlerNew.getPropertyValues("tenantId");
                  if (tenantIdFromProp == null || tenantIdFromProp.isEmpty()) {
                     tenantIdFromProp = "cuecent_tenant";
                  }

                  TaskInputObject.put("CUECENT_OWNER_TENANTID_2015", tenantIdFromProp);
                  Map var67 = HashMapJSONParser.convertJSONObjectToHashMap(TaskInputObject);
                  // // System.out.println("Before invoking validateTask");
                  result = (HashMap) invokeWorkFlow("CoreLoginService", var67);
                  session.setAttribute("result", result);
                  // // System.out.println("After invoking validateTask");
                  JSONObject sessionParmObject = HashMapJSONParser.convertHashMapToJSONObject(result);
                  String hdrcache = sessionParmObject.getString("hdrcache");
                  // // System.out.println("hdrcache" + hdrcache);
                  if (hdrcache.contains("[")) {
                     hdrcache = hdrcache.replace("[", "");
                  }

                  if (hdrcache.contains("]")) {
                     hdrcache = hdrcache.replace("]", "");
                  }

                  if (hdrcache.contains("{") && hdrcache.contains("}")) {
                     sessionParmObject = JSONObject.fromObject(hdrcache);
                  }

                  // // System.out.println("sessionParmObject" + sessionParmObject);
                  if (StringUtils.isEmpty((String) sessionParmObject.get("task_id"))
                        && !workFlowName.equals("CoreAdminService") && !workFlowName.equals("CoreLoginService")) {
                     bTaskPermission = false;
                     result.put("strFailureMsg", "User does not have Task level Privilege.");
                  } else {
                     bTaskPermission = true;
                  }
               } else if (sessionFlag) {
                  bTaskPermission = true;
               }

               if (bTaskPermission) {
                  userName = session.getAttribute("UserName").toString();
                  orgName = session.getAttribute("OrgName").toString();
                  langId = session.getAttribute("LangId").toString();
                  strDeptId = session.getAttribute("strDeptId").toString();
                  inputObject.put("strUserId", userName);
                  inputObject.put("strOrgId", orgName);
                  inputObject.put("strLangId", langId);
                  inputObject.put("strRoleId", roleId);
                  inputObject.put("strTenantId", tenantId);
                  inputObject.put("strDeptId", strDeptId);
                  String tenantIdFromProp = actionHandlerNew.getPropertyValues("tenantId");
                  if (tenantIdFromProp == null || tenantIdFromProp.isEmpty()) {
                     tenantIdFromProp = "cuecent_tenant";
                  }

                  inputObject.put("CUECENT_OWNER_TENANTID_2015", tenantIdFromProp);
                  Map var68 = HashMapJSONParser.convertJSONObjectToHashMap(inputObject);
                  // System.out.println("Before invoking " + workFlowName);
                  // System.out.println("Before invoking " + var68);
                  result = (HashMap) invokeWorkFlow(workFlowName, var68);
                  session.setAttribute("result", result);
                  // System.out.println("After invoking " + workFlowName);
                  strUserId = inputObject.getString("strUserId");
                  initTenantId = inputObject.getString("strTenantId");
                  // System.out.println("initTenantID" + initTenantId);
                  strMethodName = inputObject.getString("methodName");
                  // System.out.println("strMethodName" + strMethodName);
                  if (strMethodName.equals("OKBtnSwithContextTS")) {
                     orgName = inputObject.get("strOrgName").toString();
                     roleId = inputObject.get("strRoleIdFrom").toString();
                     strDeptId = inputObject.get("strOrgName").toString();
                     session.setAttribute("OrgName", orgName);
                     session.setAttribute("RoleId", roleId);
                     session.setAttribute("strDeptId", strDeptId);
                  }
               }
            }

            if (processType.equals("Report")) {
               HttpSession session = request.getSession(false);
               String filePath = result.get("retFilePath").toString();
               // System.out.println("In Action Handler-retFilePath:" + filePath);
               String OutFileName = result.get("OutFileName").toString();
               // System.out.println("In Action Handler-OutFileName:" + OutFileName);
               String UID = (new Timestamp(date.getTime())).toString();
               UID.replaceAll("\\s", "");
               session.setAttribute(UID, filePath);
               session.setAttribute("FilePath", filePath);
               session.setAttribute("OutFileName", OutFileName);
               result.put("UID", UID);
            }
         } catch (Exception e) {
            // System.out.println("## Exception while parsing input parameters into a JSON
            // Object ##" + e);
            result.put("strFailureMsg", "System Error. Contact Administrator. Error Code: S004");
            e.printStackTrace();
         }
      }

      JSONObject responseData = HashMapJSONParser.convertHashMapToJSONObject(result);
      JSONObject responseObj = new JSONObject();
      Iterator iter = responseData.keys();
      String strAuthToken = actionHandlerNew.getUUID();
      if (sessionFlag) {
         HttpSession session = request.getSession(false);
         session.setAttribute("hdnAuthToken", strAuthToken);
      }

      while (iter.hasNext()) {
         String key = (String) iter.next();

         try {
            if (key.equals("UID") || key.equals("hdrcache") || key.indexOf("_array") > 0 || key.equals("strSuccessMsg")
                  || key.equals("strFailureMsg") || key.indexOf("_svgdata") > 0 || key.equals("strSessionError")) {
               if (!key.equals("hdrcache")) {
                  responseObj.put(key, responseData.get(key));
               } else {
                  // System.out.println("Admin" + responseData.get(key).toString());
                  JSONObject hdrCacheJsonObj;
                  JSONArray hdrCacheArray;
                  if (!responseData.get(key).toString().equalsIgnoreCase("null")
                        && !responseData.get(key).toString().equalsIgnoreCase("[]")) {
                     hdrCacheArray = (JSONArray) responseData.get(key);
                     if (!((JSONObject) hdrCacheArray.get(0)).isEmpty()) {
                        hdrCacheJsonObj = (JSONObject) hdrCacheArray.get(0);
                     } else {
                        hdrCacheJsonObj = new JSONObject();
                     }
                  } else {
                     hdrCacheArray = new JSONArray();
                     hdrCacheJsonObj = new JSONObject();
                  }

                  // System.out.println("Adding SyncToken to Header Cache" + strAuthToken);
                  hdrCacheJsonObj.put("hdnAuthToken", strAuthToken);
                  hdrCacheArray.clear();
                  hdrCacheArray.add(0, hdrCacheJsonObj);
                  responseObj.put(key, hdrCacheArray);
                  // System.out.println("key inside HdrCache2:" + key.toString());
               }
            }
         } catch (JSONException var35) {
            responseObj.put("strSessionError", "Compilation Error");
         }
      }

      try {
         TrafficLogger.log("\n" + "\nOUTGOING <<< Response: \n" + responseObj.toString() + "\n\n  ");

         response.getWriter().write(responseObj.toString());
      } catch (IOException e) {
         result.put("strFailureMsg", "System Error. Contact Administrator. Error Code: S006");
         e.printStackTrace();
      }

   }

   public static Map invokeWorkFlow(String workFlowName) {
      Map result = null;
      Map inputParams = new HashMap();
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
         System.out.println("\n\n**********Before Invoking Workflow : " + args);
         System.out.println("\n\n**********Before Invoking Workflow : " + workFlowName);
         if (workFlowName.equals("CoreTenantService") || workFlowName.equals("CoreAdminService") || workFlowName.equals("Logout") || workFlowName.equals("CoreLoginService")) {
            result = WebserviceUtility.executeWorkFlow(workFlowName, ipAddress, port, (HashMap) args, iTimeOut);
         } 
         else {
            String portRest = actionHandlerNew.getPropertyValues("PortRest");
            String ipAddressRest = actionHandlerNew.getPropertyValues("IPAddressRest");
            result = sendPost(workFlowName, ipAddressRest, portRest, (HashMap) args, iTimeOut);
         }
      } catch (Exception e) {
         // System.out.println(":: Exception while calling the workflow " + workFlowName
         // + "\n" + e.getMessage());
         result.put("strFailureMsg", "System Error. Contact Administrator. Error Code: InvokeWorkflow001");
      }

      return result;
   }

   public String getPropertyValues(String key) throws IOException {
      Properties prop = new Properties();
      String propFileName = "./config.properties";
      InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(propFileName);
      prop.load(inputStream);
      if (inputStream == null) {
         throw new FileNotFoundException("Property File" + propFileName + "Not Found");
      } else {
         String value = prop.getProperty(key);
         return value;
      }
   }

   public String readSesValPropFile(String key) throws IOException {
      Properties prop = new Properties();
      String propFileName = "./sessionvalidation.properties";
      InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(propFileName);
      prop.load(inputStream);
      if (inputStream == null) {
         throw new FileNotFoundException("Session Property File" + propFileName + "Not Found");
      } else {
         String value = prop.getProperty(key);
         return value;
      }
   }

   private String getUUID() {
      String iUID = UUID.randomUUID().toString();
      return iUID;
   }

   private Map callTenantService(String workFlowParams, String workFlowName, JSONObject inputObject) {
      new HashMap();
      new HashMap();
      ActionHandlerNew actionHandlerNew = new ActionHandlerNew();
      String tenantIdFromProp = "";

      try {
         tenantIdFromProp = actionHandlerNew.getPropertyValues("tenantId");
      } catch (IOException e) {
         // System.out.println(e.getMessage());
      }

      if (tenantIdFromProp == null || tenantIdFromProp.isEmpty()) {
         tenantIdFromProp = "cuecent_tenant";
      }

      inputObject.put("strTenantId", tenantIdFromProp);
      Map workFlowParamsMap = HashMapJSONParser.convertJSONObjectToHashMap(inputObject);
      // System.out.println("Before Invoking CoreTenantService Tenant Id : " +
      // tenantIdFromProp);
      // System.out.println("Before Invoking Workflow : " + workFlowName);
      Map result = (HashMap) invokeWorkFlow("CoreTenantService", workFlowParamsMap);
      return result;
   }

   public static Map sendPost(String workFlowName, String ipAddress, String port, HashMap args, long iTimeOut) throws Exception {
      URL url = new URL("http://" + ipAddress + ":" + port + "/JMSServlet/");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Accept", "application/json");
      conn.setConnectTimeout((int) iTimeOut);
      conn.setReadTimeout((int) iTimeOut);
      conn.setDoOutput(true);

      System.out.println("\n\n**********Sending POST request to: " + url);
      JSONObject requestJson = new JSONObject();
      requestJson.put("workFlowName", workFlowName);
      requestJson.put("workFlowParams", HashMapJSONParser.convertHashMapToJSONObject(args).toString());
      requestJson.put("processType", "string");
      String jsonInput = requestJson.toString();
      System.out.println("\n\n**********FASTAPI REQUEST BODY: " + jsonInput);
      try(OutputStream os = conn.getOutputStream()) {
         byte[] input = jsonInput.getBytes("utf-8");
         os.write(input, 0, input.length);
      }
      
      int responseCode = conn.getResponseCode();
      
      // Read response
      StringBuilder response = new StringBuilder();
      try (BufferedReader reader = new BufferedReader(
               new InputStreamReader(responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream(), "utf-8"))) {
         String line;
         while ((line = reader.readLine()) != null) {
               response.append(line);
         }
      }
      System.out.println("Response Code: " + responseCode);
      System.out.println("Response Body: " + response.toString()); 
      conn.disconnect();

      if (responseCode < 200 || responseCode >= 300) {
         throw new IOException("REST call failed. HTTP Status: " + responseCode + " Response: " + response.toString());
      }
      Map responseMap = HashMapJSONParser.convertJSONObjectToHashMap(JSONObject.fromObject(response.toString()));
      return responseMap;
   }

}
