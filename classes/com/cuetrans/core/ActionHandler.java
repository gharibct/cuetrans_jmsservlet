package com.cuetrans.core;

import com.bpms.core.webservice.WebserviceUtility;
import com.cuetrans.utils.HashMapJSONParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class ActionHandler
{
  public static void actionHandle(HttpServletRequest request, HttpServletResponse response)
  {
    String userName = "";
    String orgName = "";
    String langId = "";
    String roleId = "";
    String tenantId = "";
    String strUserId = "";
    String strPassword = "";

    Date date = new Date();

    String workFlowName = request.getParameter("workFlowName");
    String workFlowParams = request.getParameter("workFlowParams");
    String processType = request.getParameter("processType");

    JSONObject responseData = new JSONObject();
    Map result = new HashMap();
    boolean sessionFlag = true;

    if (workFlowName.equals("Logout"))
    {
      HttpSession session = request.getSession(false);
      session.invalidate();
      sessionFlag = false;
      result.put("strSuccessMsg", "Logged Out Successfully");
    }

    if (!workFlowName.equals("CoreTenantService"))
    {
      HttpSession session = request.getSession(false);
      if (session == null)
      {
        result.put("strSessionError", "Session Timeout");
        sessionFlag = false;
        System.out.println("Session Flag" + sessionFlag);
      }

    }

    if (sessionFlag)
    {
      if ((workFlowParams == null) || (workFlowParams.length() == 0) || (workFlowParams.trim().equalsIgnoreCase("")))
      {
        result = (HashMap)invokeWorkFlow(workFlowName);
        responseData = HashMapJSONParser.convertHashMapToJSONObject(result);
      }
      else
      {
        Map workFlowParamsMap = new HashMap();
        try {
          JSONObject inputObject = JSONObject.fromObject(workFlowParams);

          if (!workFlowName.equals("CoreTenantService"))
          {
            System.out.println("Workflowname" + workFlowName);
            HttpSession session = request.getSession(false);
            userName = session.getAttribute("UserName").toString();
            orgName = session.getAttribute("OrgName").toString();
            langId = session.getAttribute("LangId").toString();
            roleId = session.getAttribute("RoleId").toString();
            tenantId = session.getAttribute("TenantId").toString();
            inputObject.put("strUserId", userName);
            inputObject.put("strOrgId", orgName);
            inputObject.put("strLangId", langId);
            inputObject.put("strRoleId", roleId);
            inputObject.put("strTenantId", tenantId);
          }
          else
          {
            inputObject.put("strTenantId", "cuecent_tenant");
          }
          workFlowParamsMap = HashMapJSONParser.convertJSONObjectToHashMap(inputObject);
          System.out.println("inputobject" + inputObject);
          result = (HashMap)invokeWorkFlow(workFlowName, workFlowParamsMap);
          System.out.println("Result" + result);
          strUserId = inputObject.getString("strUserId");

          if (workFlowName.equals("CoreTenantService"))
          {
            String s = result.get("errorFlag").toString();
            System.out.println("s-ErrorFlag:" + s);
            HttpSession session = request.getSession(true);
            session.setAttribute("result_tenantcall", result);
            if (s.equals("0"))
            {
              JSONObject LoginInputObject = JSONObject.fromObject(workFlowParams);
              System.out.println("LoginInputObject" + LoginInputObject);
              tenantId = result.get("strLoginTenantId").toString();
              session.setAttribute("tenantIdtest", tenantId);
              LoginInputObject.put("strTenantId", tenantId);
              LoginInputObject.put("methodName", "validateLogin");
              LoginInputObject.put("strUserId", strUserId);

              workFlowParamsMap = HashMapJSONParser.convertJSONObjectToHashMap(LoginInputObject);
              result = (HashMap)invokeWorkFlow("CoreLoginService", workFlowParamsMap);
              session.setAttribute("TenantId", tenantId);
              session.setAttribute("result_logincall", result);
              String res = result.get("errorFlag").toString();
              System.out.println("login result errorflag" + res);

              if (res.equals("0"))
              {
                session.setAttribute("test_test", "test");
                userName = result.get("strUserId").toString();
                System.out.println("username" + userName);
                orgName = result.get("strOrgId").toString();
                langId = result.get("strLangId").toString();
                roleId = result.get("strRoleId").toString();

                session.setAttribute("UserName", userName);
                session.setAttribute("OrgName", orgName);
                session.setAttribute("LangId", langId);
                session.setAttribute("RoleId", roleId);
                session.setAttribute("TenantId", tenantId);
              }
              else
              {
                result.put("strSessionError", "Tenant2 Login Failed");
                sessionFlag = false;
                System.out.println("Session Flag" + sessionFlag);
              }
            }
            else
            {
              result.put("strSessionError", "Login Failed");
              sessionFlag = false;
              System.out.println("Session Flag" + sessionFlag);
            }

          }

          if (processType.equals("Report"))
          {
            HttpSession session = request.getSession(false);
            String filePath = result.get("retFilePath").toString();
            String UID = new Timestamp(date.getTime()).toString();
            UID.replaceAll("\\s", "");
            session.setAttribute(UID, filePath);
            session.setAttribute("FilePath", filePath);
            result.put("UID", UID);
          }

        }
        catch (Exception e)
        {
          System.out.println("## Exception while parsing input parameters into a JSON Object ##" + e);
          e.printStackTrace();
        }
      }

    }

    responseData = HashMapJSONParser.convertHashMapToJSONObject(result);
    JSONObject responseObj = new JSONObject();

    Iterator iter = responseData.keys();
    while (iter.hasNext()) {
      String key = (String)iter.next();

      System.out.println("key" + key.toString());
      try
      {
        if ((!key.equals("UID")) && (!key.equals("hdrcache")) && (key.indexOf("_array") <= 0) && (!key.equals("strSuccessMsg")) && (!key.equals("strFailureMsg")) && (key.indexOf("_svgdata") <= 0) && (!key.equals("strSessionError")))
          continue;
        responseObj.put(key, responseData.get(key));
      }
      catch (JSONException e)
      {
        responseObj.put("strSessionError", "Compilation Error");
      }

    }

    try
    {
      response.getWriter().write(responseObj.toString());
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public static Map invokeWorkFlow(String workFlowName)
  {
    Map result = null;
    Map inputParams = new HashMap();
    result = invokeWorkFlow(workFlowName, inputParams);
    return result;
  }

  private static Map invokeWorkFlow(String workFlowName, Map args)
  {
    Map result = null;
    System.out.println(":: Started Calling workflow - " + workFlowName);
    try {
      ActionHandler actionHandler = new ActionHandler();
      String port = actionHandler.getPropertyValues("Port");
      String ipAddress = actionHandler.getPropertyValues("IPAddress");
      com.bpms.engine.util.WebserviceAttachmentInfo.tenantId = args.get("strTenantId").toString();
      System.out.println("Invoke workflow- TenantId" + args.get("strTenantId").toString());
      System.out.println("Invoke workflow-Params" + args);
      System.out.println("Invoke workflow-Workflow name" + workFlowName);
      result = WebserviceUtility.executeWorkFlow(workFlowName, ipAddress, port, (HashMap)args);
      System.out.println(":: Completed Calling workflow - " + workFlowName);
    }
    catch (Exception e) {
      System.out.println(":: Exception while calling the workflow " + workFlowName + "\n" + e.getMessage());
    }

    return result;
  }

  private String getPropertyValues(String key) throws IOException
  {
    Properties prop = new Properties();
    String propFileName = "./config.properties";
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
    prop.load(inputStream);
    if (inputStream == null)
    {
      throw new FileNotFoundException("Property File" + propFileName + "Not Found");
    }
    String value = prop.getProperty(key);
    return value;
  }
}