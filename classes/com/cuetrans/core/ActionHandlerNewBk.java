package com.cuetrans.core;

import com.bct.bpms.client.rest.RestClientApi;
import com.cuetrans.utils.HashMapJSONParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.security.Principal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import nl.captcha.Captcha;
import org.apache.commons.lang.StringUtils;

public class ActionHandlerNewBk
{
  private static final Logger LOGGER = Logger.getLogger(ActionHandlerNewBk.class.getName());
  private static String uniqueID = null;
  private static Date date;
  private static SimpleDateFormat sdf=null;
  
  
  static {
    try { FileHandler fileHandler = new FileHandler("CuetransLogin%g", 
        5242880, 10, true);
      fileHandler.setFormatter(new SingleLineLogFormatter());
      LOGGER.addHandler(fileHandler);
    } catch (SecurityException e) {
      System.out.println("File Handler could not be created");
    } catch (IOException e) {
      System.out.println("File Handler could not be created");
    }
  }

  public static void actionHandle(HttpServletRequest request, HttpServletResponse response)
  {
    HttpSession ses = null;
    try {
      ses = request.getSession(false);
      if (ses == null) {
        ses = request.getSession();
      }
      uniqueID = UUID.randomUUID().toString();
      sdf=new SimpleDateFormat("HH:mm:ss");
      
      
      printSesBefore(ses);
      actionHandle1(request, response);
      Cookie sessionCookie = new Cookie("JSESSIONID", ses.getId());
      response.addCookie(sessionCookie);
    } finally {
      printSesAfter(ses);
    }
  }

  private static void actionHandle1(HttpServletRequest request, HttpServletResponse response)
  {
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
    String authTokenValidationFlag = "Y";
    String casFlag = "N";
    Date date = new Date();
    JsonParser jp = new JsonParser();
    
    String cReqTime= null;
    
    cReqTime = request.getParameter("CREQTIME");
    LOGGER.log(Level.INFO, "CLIENT REQUEST :: "+uniqueID+" || "+cReqTime);
    
    date = new Date();
    LOGGER.log(Level.INFO, "REQ_START :: "+uniqueID+" || "+sdf.format(date));
    
    
    String workFlowName = request.getParameter("workFlowName");
    String workFlowParams = request.getParameter("workFlowParams");
    String processType = request.getParameter("processType");
    String authToken = request.getParameter("AuthToken");

    String strIPAddr = request.getHeader("X-FORWARDED-FOR");

    if (strIPAddr == null) {
      strIPAddr = request.getRemoteAddr();
    }

    JsonObject result = new JsonObject();
    boolean sessionFlag = true;
    ActionHandlerNewBk actionHandlerNew = new ActionHandlerNewBk();

    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0L);

    if (workFlowName.equals("Logout")) {
      HttpSession session = request.getSession(false);
      System.out.println("Logout");
      strUserId = session.getAttribute("UserName").toString();
      session.invalidate();
      sessionFlag = false;

      result.addProperty("strSuccessMsg", "Logged Out Successfully");
      LOGGER.log(Level.INFO, strIPAddr + "," + strUserId + "," + "LOGOUT");
    }

    if ((processType != null) && (processType.equals("ForgotPassword"))) {
      System.out.println("Forgot Password");
      sessionFlag = false;
      String multiTenancySetup = "N";
      String s = "0";
      try {
        multiTenancySetup = actionHandlerNew.getPropertyValues("MultiTenancy");
      } catch (IOException e) {
        System.out.println(e.getMessage());
      }
      try {
        tenantId = actionHandlerNew.getPropertyValues("tenantId");
      } catch (IOException e) {
        System.out.println(e.getMessage());
      }

      JsonObject inputObject = (JsonObject)jp.parse(workFlowParams);

      strUserId = inputObject.get("strUserId").getAsString();
      if (multiTenancySetup.equals("Y")) {
        result = actionHandlerNew.callTenantService(workFlowParams, workFlowName, inputObject);

        tenantId = result.get("strLoginTenantId").toString();
        System.out.println("After Invoking ForgotPassword User Id : " + strUserId);

        s = result.get("errorFlag").getAsString();
        System.out.println("s-ErrorFlag:" + s);
      }
      if (s.equals("0")) {
        JsonObject resetPwdInputObject = (JsonObject)jp.parse(workFlowParams);
        Map workFlowParamsMap = new HashMap();
        System.out.println("Before Invoking ForgotPassword Result : " + result);

        System.out.println("After Invoking ForgotPassword Tenant Id : " + tenantId);

        resetPwdInputObject.addProperty("strTenantId", tenantId);
        resetPwdInputObject.addProperty("strUserId", strUserId);
        resetPwdInputObject.addProperty("methodName", "validateUserPassword");

        System.out.println("Before invoking Reset Password");

        result = invokeWorkFlow("CoreLoginService", resetPwdInputObject);
        System.out.println("After invoking Reset Password");
      }

    }

    if (!workFlowName.equals("CoreLoginService")) {
      HttpSession session = request.getSession(false);
      if (session == null) {
        result.addProperty("strSessionError", "Session Timeout");
        sessionFlag = false;
      }
    } else {
      String strCaptchaValidate = "Y";
      try {
        strCaptchaValidate = actionHandlerNew.getPropertyValues("CaptchaValidate");
      } catch (IOException e1) {
        System.out.println(e1.getMessage());
      }
      if (!strCaptchaValidate.equals("N")) {
        HttpSession session = request.getSession(false);
        Captcha captcha = (Captcha)session.getAttribute("simpleCaptcha");

        JsonObject inputObject = (JsonObject)jp.parse(workFlowParams);

        answer = inputObject.get("answer").getAsString();
        System.out.println("captcha" + captcha);
        try {
          request.setCharacterEncoding("UTF-8");
          System.out.println("answer" + answer);
          if (captcha.isCorrect(answer)) {
            System.out.println(" Correct Captcha Code !");
          } else {
            System.out.println("Please enter a valid Captcha value");

            result.addProperty("strFailureMsg", "Please enter a valid Captcha value");
            sessionFlag = false;
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    System.out.println("Session Flag" + sessionFlag);
    String tenantIdFromProp;
    if (sessionFlag) {
      try {
        JsonObject inputObject = (JsonObject)jp.parse(workFlowParams);
        if (workFlowName.equals("CoreLoginService")) {
          System.out.println("Inside core Login Service");
          HttpSession session = request.getSession(true);

          String multiTenancySetup = "N";
          try {
            multiTenancySetup = actionHandlerNew.getPropertyValues("MultiTenancy");
          } catch (IOException e) {
            System.out.println(e.getMessage());
          }
          String s = "0";
          casFlag = actionHandlerNew.getPropertyValues("casAuthenticateFlag");
          tenantId = actionHandlerNew.getPropertyValues("tenantId");
          System.out.println("TenantId before invoking *********************************" + tenantId);
          System.out.println("multiTenancySetup ----------- : " + multiTenancySetup);

          if (casFlag.equals("Y")) {
            Principal principal = request.getUserPrincipal();
            System.out.println("principal" + principal);
            String remoteUserName = request.getRemoteUser();

            String remoteUserId = principal == null ? "null" : principal.getName();
            System.out.println("remoteuser" + remoteUserName);
            System.out.println("remoteuserid" + remoteUserId);
            String[] userNameArr = remoteUserId.split("/");
            System.out.println("usernamearr" + userNameArr);
            s = "1";
            if (userNameArr.length > 1) {
              s = "0";
              tenantId = userNameArr[0];
              strUserId = userNameArr[1];
            }
          }

          if (multiTenancySetup.equals("Y")) {
            result = actionHandlerNew.callTenantService(workFlowParams, workFlowName, inputObject);

            session.setAttribute("result", result);
            System.out.println("After Invoking Workflow : " + workFlowName);

            strUserId = inputObject.get("strUserId").getAsString();

            initTenantId = inputObject.get("strTenantId").getAsString();

            System.out.println("After Invoking CoreTenantService User Id : " + strUserId);

            s = result.get("errorFlag").getAsString();
            tenantId = result.get("strLoginTenantId").getAsString();
            s = "0";
            System.out.println("s-ErrorFlag:" + s);
          }

          if (s.equals("0")) {
            JsonObject LoginInputObject = (JsonObject)jp.parse(workFlowParams);

            System.out.println("IP Address : " + strIPAddr);

            LoginInputObject.addProperty("strTenantId", tenantId);
            LoginInputObject.addProperty("methodName", "validateLogin");
            if ((!strUserId.equals("")) && (strUserId != null)) {
              LoginInputObject.addProperty("strUserId", strUserId);
            }
            LoginInputObject.addProperty("strIPAddr", strIPAddr);

            result = invokeWorkFlow("CoreLoginService", LoginInputObject);
            if (!result.isJsonNull()) {
              session.setAttribute("result", result);
              System.out.println("After invoking ValidateLogin");

              session.setAttribute("TenantId", tenantId);

              String res = result.get("errorFlag").getAsString();
              System.out.println("login result errorflag" + res);
              if (res.equals("0")) {
                LOGGER.log(Level.INFO, strIPAddr + "," + strUserId + "," + "SUCCESS");
                System.out.println("Login Successful");
                userName = result.get("strUserId").getAsString();
                System.out.println("Login username : " + userName);
                orgName = result.get("strOrgId").getAsString();
                System.out.println("Login Org Id : " + orgName);
                langId = result.get("strLangId").getAsString();
                System.out.println("Login Lang : " + langId);
                roleId = result.get("strRoleId").getAsString();
                System.out.println("Login Role : " + roleId);
                strDeptId = result.get("strDeptId").getAsString();
                System.out.println("Login Dept : " + strDeptId);

                session.setAttribute("UserName", userName);
                session.setAttribute("OrgName", orgName);
                session.setAttribute("LangId", langId);
                session.setAttribute("RoleId", roleId);
                session.setAttribute("TenantId", tenantId);
                session.setAttribute("strDeptId", strDeptId);
                printSessionAttributes(session);
              } else {
                LOGGER.log(Level.INFO, strIPAddr + "," + strUserId + "," + "FAILED");
                result.addProperty("strSessionError", "Login Failed. Tenant Error");
                sessionFlag = false;
                System.out.println("Session Flag" + sessionFlag);
              }
            }
          } else {
            LOGGER.log(Level.INFO, strIPAddr + "," + strUserId + "," + "FAILED");
            result.addProperty("strSessionError", "Login Failed");
            sessionFlag = false;
            System.out.println("Session Flag" + sessionFlag);
          }
        } else {
          System.out.println("Workflowname" + workFlowName);
          HttpSession session = request.getSession(false);
          boolean bTaskPermission = false;
          System.out.println("Input Auth token:" + authToken);

          tenantId = session.getAttribute("TenantId").toString();
          roleId = session.getAttribute("RoleId").toString();
          if ((actionHandlerNew.getPropertyValues("TaskValidate") == "Y") && (sessionFlag)) {
            JsonObject TaskInputObject = (JsonObject)jp.parse(workFlowParams);
            strMethodName = TaskInputObject.get("methodName").toString();

            TaskInputObject.addProperty("strTenantId", tenantId);
            TaskInputObject.addProperty("methodName", "validateTask");
            TaskInputObject.addProperty("strMethodName", strMethodName);
            TaskInputObject.addProperty("strRoleId", roleId);

            tenantIdFromProp = actionHandlerNew.getPropertyValues("tenantId");
            if ((tenantIdFromProp == null) || (tenantIdFromProp.isEmpty())) {
              tenantIdFromProp = "cuecent_tenant";
            }

            TaskInputObject.addProperty("CUECENT_OWNER_TENANTID_2015", tenantIdFromProp);

            System.out.println("Before invoking validateTask");

            result = invokeWorkFlow("CoreLoginService", TaskInputObject);
            session.setAttribute("result", result);
            System.out.println("After invoking validateTask");

            String hdrcache = result.get("hdrcache").toString();
            System.out.println("hdrcache" + hdrcache);
            if (hdrcache.contains("[")) {
              hdrcache = hdrcache.replace("[", "");
            }
            if (hdrcache.contains("]")) {
              hdrcache = hdrcache.replace("]", "");
            }
            if (hdrcache.contains("{")) {
              hdrcache.contains("}");
            }

            System.out.println("sessionParmObject" + result);

            if ((StringUtils.isEmpty(result.get("task_id").getAsString())) && 
              (!workFlowName.equals("CoreAdminService")) && 
              (!workFlowName.equals("CoreLoginService"))) {
              bTaskPermission = false;

              result.addProperty("strFailureMsg", "User does not have Task level Privilege.");
            } else {
              bTaskPermission = true;
            }
          } else if (sessionFlag) {
            bTaskPermission = true;
          }
          if (bTaskPermission) {
            userName = session.getAttribute("UserName").toString();

            langId = session.getAttribute("LangId").toString();
            strDeptId = session.getAttribute("strDeptId").toString();

            inputObject.addProperty("strUserId", userName);
            inputObject.addProperty("strOrgId", orgName);
            inputObject.addProperty("strLangId", langId);
            inputObject.addProperty("strRoleId", roleId);
            inputObject.addProperty("strTenantId", tenantId);
            inputObject.addProperty("strDeptId", strDeptId);

            tenantIdFromProp = actionHandlerNew.getPropertyValues("tenantId");
            if ((tenantIdFromProp == null) || (tenantIdFromProp.isEmpty())) {
              tenantIdFromProp = "cuecent_tenant";
            }

            inputObject.addProperty("CUECENT_OWNER_TENANTID_2015", tenantIdFromProp);

            result = invokeWorkFlow(workFlowName, inputObject);

            session.setAttribute("result", result);

            strUserId = inputObject.get("strUserId").getAsString();
            initTenantId = inputObject.get("strTenantId").getAsString();

            System.out.println("initTenantID" + initTenantId);

            strMethodName = inputObject.get("methodName").getAsString();

            System.out.println("strMethodName" + strMethodName);
            if (strMethodName.equals("OKBtnSwithContextTS")) {
              orgName = inputObject.get("strOrgName").getAsString();
              roleId = inputObject.get("strRoleIdFrom").getAsString();

              strDeptId = inputObject.get("strOrgName").getAsString();

              session.setAttribute("OrgName", orgName);
              session.setAttribute("RoleId", roleId);
              session.setAttribute("strDeptId", strDeptId);
              printSessionAttributes(session);
            }
          }
        }
        if ("Report".equals(processType)) {
          HttpSession session = request.getSession(false);
          String filePath = result.get("retFilePath").getAsString();
          System.out.println("In Action Handler-retFilePath:" + filePath);
          String OutFileName = result.get("OutFileName").getAsString();

          System.out.println("In Action Handler-OutFileName:" + OutFileName);
          String UID = new Timestamp(date.getTime()).toString();
          UID.replaceAll("\\s", "");
          session.setAttribute(UID, filePath);
          session.setAttribute("FilePath", filePath);
          session.setAttribute("OutFileName", OutFileName);

          result.addProperty("UID", UID);
        }
      } catch (Exception e) {
        System.out.println("## Exception while parsing input parameters into a JSON Object ##" + e);
        e.printStackTrace();
      }

    }

    JsonObject responseObj = new JsonObject();

    String strAuthToken = actionHandlerNew.getUUID();
    if (sessionFlag) {
      HttpSession session = request.getSession(false);
      session.setAttribute("hdnAuthToken", strAuthToken);
    }
    JsonArray hdrCacheArray = new JsonArray();
    Set<Entry<String, JsonElement>> entrySet = result.entrySet();
    for (Map.Entry entry : entrySet) {
      String key = (String)entry.getKey();
      try {
        if ((!key.equals("UID")) && (!key.equals("hdrcache")) && (key.indexOf("_array") <= 0) && 
          (!key.equals("strSuccessMsg")) && (!key.equals("strFailureMsg")) && 
          (key.indexOf("_svgdata") <= 0) && (!key.equals("strSessionError")))
          continue;
        if (key.equals("hdrcache"))
        {
          JsonObject hdrCacheJsonObj;
          
          if (result.get(key).isJsonNull()) {
            //JsonArray hdrCacheArray = new JsonArray();
            hdrCacheJsonObj = new JsonObject();
          } else {
            hdrCacheArray = (JsonArray)result.get(key);
            //JsonObject hdrCacheJsonObj;
            if (hdrCacheArray.size() == 0) {
              hdrCacheJsonObj = new JsonObject();
            } else {
              JsonElement je = hdrCacheArray.get(0);
              //JsonObject hdrCacheJsonObj;
              if (je.isJsonNull())
                hdrCacheJsonObj = new JsonObject();
              else {
                hdrCacheJsonObj = (JsonObject)je;
              }
            }
          }
          System.out.println("Adding SyncToken to Header Cache" + strAuthToken);

          hdrCacheJsonObj.addProperty("hdnAuthToken", strAuthToken);

         
          hdrCacheArray.add(hdrCacheJsonObj);
          responseObj.add(key, hdrCacheArray);
          System.out.println("key inside HdrCache2:" + key.toString());
        } else {
          responseObj.add(key, result.get(key));
        }
      } catch (JsonIOException e) {
        responseObj.addProperty("strSessionError", "Compilation Error");
      }
    }

    try
    {
     
        date = new Date();
        LOGGER.log(Level.INFO, "REQ_END :: "+uniqueID+" || "+sdf.format(date));
        
        
       // responseObj.addProperty("CRESTIME",sdf.format(date));
        
      response.getWriter().write(responseObj.toString());
      //System.out.println("responseObj" + responseObj);
     
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static JsonObject invokeWorkFlow(String workFlowName)
  {
    JsonObject inputParams = new JsonObject();
    JsonObject result = null;
    try
    {
      ActionHandlerNewBk actionHandlerNew = new ActionHandlerNewBk();

      result = invokeWorkFlow(workFlowName, inputParams);
    } catch (Exception e) {
      System.out.println(":: Exception while calling the workflow " + 
        workFlowName + "\n" + e.getMessage());
    }

    return result;
  }

  public Map invokeNewEcareWorkFlow(String workFlowName, Map args)
  {
    System.out.println("Ecare Workflow " + workFlowName);

    JsonObject jsonObj = convertMapToJsonObject(args);

    JsonObject responsejsonObj = invokeWorkFlow(workFlowName, jsonObj);

    Map responseMap = convertJsonObjectToMap(responsejsonObj);
    return responseMap;
  }

  private Map convertJsonObjectToMap(JsonObject jsonObj)
  {
    Map responseMap = new HashMap();
    Set<Entry<String, JsonElement>> entrySet = jsonObj.entrySet();
    //Set entrySet = jsonObj.entrySet();

    for (Map.Entry entry : entrySet)
    {
      responseMap.put((String)entry.getKey(), jsonObj.get((String)entry.getKey())
        .toString().replace("\"", ""));
    }
    return responseMap;
  }

  private JsonObject convertMapToJsonObject(Map map)
  {
    JsonObject jsonObject = new JsonObject();
    Iterator it = map.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry pairs = (Map.Entry)it.next();
      jsonObject.addProperty(pairs.getKey().toString(), pairs.getValue().toString());
    }
    System.out.println(jsonObject);
    return jsonObject;
  }

  private static JsonObject invokeWorkFlow(String workFlowName, JsonObject args)
  {
	date = new Date();
	LOGGER.log(Level.INFO, "INVOKE EBPAC STARTED :: "+uniqueID+" || "+sdf.format(date));
	    
    JsonParser jp = new JsonParser();

    JsonObject jsonObject = null;
    try
    {
      ActionHandlerNewBk actionHandlerNew = new ActionHandlerNewBk();
      String restUrl = actionHandlerNew.getPropertyValues("restUrl");
      RestClientApi restClientApi = new RestClientApi(restUrl);
      String tenantId = args.get("strTenantId").getAsString();
      System.out.println("tenantid------------------------" + tenantId);
      String result = restClientApi.executeService(tenantId, tenantId, 
        workFlowName, args);

      System.out.println("result as it is" + result);
      if (!result.isEmpty()) {
        jsonObject = (JsonObject)jp.parse(result);
        System.out
          .println("parsed json Object. before converting null------->" + 
          jsonObject);
        HashMapJSONParser.changeNullStringsToNull(jsonObject);
        System.out
          .println("parsed json Object. after converting null------->" + 
          jsonObject);
        return jsonObject;
      }
    } catch (Exception e) {
      System.out.println(":: Exception while calling the workflow " + 
        workFlowName + "\n" + e.getMessage());
    }
    date = new Date();
	LOGGER.log(Level.INFO, "INVOKE EBPAC COMPLETED :: "+uniqueID+" || "+sdf.format(date));
	
    return jsonObject;
  }

  public String getPropertyValues(String key) throws IOException {
    Properties prop = new Properties();
    String propFileName = "./config.properties";
    InputStream inputStream = getClass().getClassLoader()
      .getResourceAsStream(propFileName);
    prop.load(inputStream);
    if (inputStream == null) {
      throw new FileNotFoundException("Property File" + propFileName + 
        "Not Found");
    }
    String value = prop.getProperty(key);
    return value;
  }

  private String getUUID() {
    String iUID = UUID.randomUUID().toString();
    return iUID;
  }

  private JsonObject callTenantService(String workFlowParams, String workFlowName, JsonObject inputObject)
  {
    Map workFlowParamsMap = new HashMap();
    ActionHandlerNewBk actionHandlerNew = new ActionHandlerNewBk();

    String tenantIdFromProp = "";
    try {
      tenantIdFromProp = actionHandlerNew.getPropertyValues("tenantId");
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }

    if ((tenantIdFromProp == null) || (tenantIdFromProp.isEmpty())) {
      tenantIdFromProp = "cuecent_tenant";
    }

    inputObject.addProperty("strTenantId", tenantIdFromProp);

    System.out.println("Before Invoking CoreTenantService Tenant Id : " + 
      tenantIdFromProp);

    System.out.println("Before Invoking Workflow : " + workFlowName);
    JsonObject result = invokeWorkFlow("CoreTenantService", inputObject);
    return result;
  }

  private static void log(String str) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
    System.out.println(sdf.format(new Date()) + " ::  " + str);
  }

  private static void printSesBefore(HttpSession sessionObj) {
    if (sessionObj == null) {
      log("session object is null");
      return;
    }
    StringBuilder str = new StringBuilder();
    str.append("Before:::");
    str.append("session id: " + sessionObj.getId());
    str.append("; user id: " + sessionObj.getAttribute("UserName"));
    str.append("; role id: " + sessionObj.getAttribute("RoleId"));
    str.append("; dept id: " + sessionObj.getAttribute("strDeptId"));
    log(str.toString());
  }

  private static void printSesAfter(HttpSession sessionObj) {
    if (sessionObj == null) {
      log("session object is null");
      return;
    }
    StringBuilder str = new StringBuilder();
    str.append("After:::");
    str.append("session id: " + sessionObj.getId());
    str.append("; user id: " + sessionObj.getAttribute("UserName"));
    str.append("; role id: " + sessionObj.getAttribute("RoleId"));
    str.append("; dept id: " + sessionObj.getAttribute("strDeptId"));
    log(str.toString());
  }

  private static void printSessionAttributes(HttpSession sessionObj) {
    if (sessionObj == null) {
      log("session object is null");
      return;
    }
    StringBuilder str = new StringBuilder();
    str.append("session id: " + sessionObj.getId());
    str.append("user id: " + sessionObj.getAttribute("UserName"));
    str.append("role id: " + sessionObj.getAttribute("RoleId"));
    str.append("dept id: " + sessionObj.getAttribute("strDeptId"));
    log(str.toString());
  }
}