package com.cuetrans.core;

import com.bct.bpms.client.rest.RestClientApi;
import com.cuetrans.utils.HashMapJSONParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.naming.AuthenticationException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import sun.nio.cs.StandardCharsets;

public class MobileActionHandlerBk
{
  public static void mobileActionHandler(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    String orgName = "";
    String langId = "";
    String roleId = "";
    String tenantId = "";
    String strUserId = "";
    String token = "";
    String userType = "";
    JsonParser jp = new JsonParser();
    System.out.println("Request: " + request.getParameterNames());
    String param = (String)request.getParameterNames().nextElement();
    System.out.println("param: " + param);
    
    /*
    byte[] bytes = request.getParameterNames().nextElement().toString().getBytes("UTF-8");
    String param = new String(bytes, "UTF-8");
    System.out.println("Param after UTF-8 ="+param);
    */
    
    JSONObject inputParamObject = JSONObject.fromObject(param);
    String workFlowName = inputParamObject.getString("workFlowName");
    String workFlowParams = inputParamObject.getString("workFlowParams");
    JsonObject responseData = new JsonObject();
    JsonObject result = new JsonObject();

    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0L);
    System.out.println("response header");
    Map workFlowParamsMap = new HashMap();

    MobileActionHandlerBk actionHandler = new MobileActionHandlerBk();
    JsonObject inputObject = (JsonObject)jp.parse(workFlowParams);
    System.out.println("inputObject: " + inputObject);

    boolean isADAuthFlag = Boolean.valueOf(actionHandler.getPropertyValues("ADAuthFlag")).booleanValue();
    Pattern emailPatternString = 
      Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", 2);
    JsonObject LoginInputObject;
    if (workFlowName.equals("CoreLoginService"))
    {
      String tenantIdFromProp = "cuecent_tenant";
      String multiTenantcyFlag = "N";
      String s = "0";
      try
      {
        tenantIdFromProp = actionHandler.getPropertyValues("tenantId");
        multiTenantcyFlag = actionHandler.getPropertyValues("MultiTenancy");
      }
      catch (IOException e)
      {
        System.out.println("Error:" + e.getMessage());
      }
      LoginInputObject = (JsonObject)jp.parse(workFlowParams);
      tenantId = tenantIdFromProp;
      strUserId = inputObject.get("strUserId").getAsString();
      if (multiTenantcyFlag.equals("Y"))
      {
        System.out.println("LoginInputObject" + LoginInputObject);

        LoginInputObject.addProperty("strTenantId", tenantIdFromProp);

        result = invokeWorkFlow("CoreTenantService", LoginInputObject);
        s = result.get("errorFlag").toString();
        System.out.println("s-ErrorFlag:" + s);
        if (s.equals("0")) {
          tenantId = result.get("strLoginTenantId").toString();
        }
      }
      boolean isAlreadyAuthenticated = false;
      if (isADAuthFlag)
      {
        System.out.println("strUserId " + strUserId);
        System.out.println("isADAuthFlag " + isADAuthFlag);
        Matcher matcher = emailPatternString.matcher(strUserId);
        if (!matcher.find())
        {
          System.out.println("matching found !!!");
          isAlreadyAuthenticated = true;
          boolean isValidUser = false;
          try
          {
            isValidUser = actionHandler.adAuthenticate(strUserId, inputObject.get("strPassword").getAsString(), actionHandler);

            System.out.println("isValidUser " + isValidUser);
          }
          catch (ClassNotFoundException e)
          {
            System.out.println("Class not found Exception");
          }
          if (isValidUser)
          {
            result.addProperty("errorFlag", "0");
          }
          else
          {
            result.addProperty("errorFlag", "1");
            result.addProperty("strFailureMsg", "Invalid user name or password.");
          }
        }
      }
      if (!isAlreadyAuthenticated)
      {
        System.out.println("Before invoking ValidateLogin");
        result = invokeCoreLoginService(workFlowParams, tenantId, strUserId, inputObject, "validateMobileLogin");
        System.out.println("After invoking ValidateLogin");
      }
      String res = result.get("errorFlag").toString();

      System.out.println("login result errorflag" + res);
      if (!res.equals("0")) {
        result.addProperty("strFailureMsg", "A failed");
      }
    }
    else
    {
      JsonObject SessionInputObject = (JsonObject)jp.parse(workFlowParams);
      String tenantIdFromProp = "cuecent_tenant";
      tenantIdFromProp = actionHandler.getPropertyValues("tenantId");

      SessionInputObject.addProperty("strTenantId", tenantIdFromProp);

      strUserId = inputObject.get("strUserId").getAsString();
      System.out.println(inputObject.get("strToken"));
      System.out.println(inputObject.get("strToken").getAsString());
      token = inputObject.get("strToken").getAsString();
      SessionInputObject.addProperty("strToken", token);
      SessionInputObject.addProperty("strUserId", strUserId);
      SessionInputObject.addProperty("methodName", "fetchloginsession");
      System.out.println("SessionInputObject" + SessionInputObject);

      result = invokeWorkFlow("CoreLoginService", SessionInputObject);

      JsonObject sessionParmObject = new JsonObject();
      String hdrcache = result.get("hdrcache").toString();
      System.out.println("hdrcache" + hdrcache);
      if (hdrcache.contains("[")) {
        hdrcache = hdrcache.replace("[", "");
      }
      if (hdrcache.contains("]")) {
        hdrcache = hdrcache.replace("]", "");
      }
      if ((hdrcache.contains("{")) && (hdrcache.contains("}"))) {
        sessionParmObject = (JsonObject)jp.parse(hdrcache);
      }
      System.out.println("sessionParmObject" + sessionParmObject);
      if (((hdrcache.contains("strToken")) && (hdrcache.contains("strUserId"))) || (StringUtils.isNotEmpty(sessionParmObject.get("strToken").getAsString())))
      {
        System.out.println("sessionResult if" + sessionParmObject);

        strUserId = SessionInputObject.get("strUserId").getAsString();
        System.out.println("User Id" + strUserId);
        tenantId = SessionInputObject.get("strTenantId").getAsString();
        System.out.println("tenantId" + tenantId);
        token = SessionInputObject.get("strToken").getAsString();
        if (SessionInputObject.get("strUserType") != null) {
          userType = SessionInputObject.get("strUserType").getAsString();
        }
        inputObject.addProperty("strUserId", strUserId);

        inputObject.addProperty("strTenantId", tenantId);

        System.out.println("inputobject1" + inputObject);

        System.out.println("inputobject" + inputObject);
        result = invokeWorkFlow(workFlowName, inputObject);
      }
      else
      {
        result.addProperty("strSessionError", "Session Is invalid");
      }
    }
    System.out.println("result1 =" + result);

    JsonObject responseObj = new JsonObject();
    System.out.println("Entryset:" + responseData.entrySet());

   Set<Entry<String, JsonElement>> entrySet = result.entrySet();
   for (Map.Entry entry : entrySet)
    {
      String key = (String)entry.getKey();
      try
      {
        if ((!key.equals("UID")) && (!key.equals("hdrcache")) && (key.indexOf("_array") <= 0) && (!key.equals("strSuccessMsg")) && (!key.equals("strFailureMsg")) && (key.indexOf("_svgdata") <= 0) && (!key.equals("strSessionError")))
          continue;
        System.out.println("key:" + key.toString());
        responseObj.add(key, result.get(key));
      }
      catch (JSONException e)
      {
        responseObj.addProperty("strSessionError", "Compilation Error");
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

  private String getPropertyValues(String key)
    throws IOException
  {
    Properties prop = new Properties();
    String propFileName = "./config.properties";
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
    prop.load(inputStream);
    if (inputStream == null) {
      throw new FileNotFoundException("Property File" + propFileName + "Not Found");
    }
    String value = prop.getProperty(key);
    return value;
  }

  private static JsonObject invokeCoreLoginService(String workFlowParams, String tenantId, String strUserId, JsonObject inputObject, String methodName)
  {
    JsonParser jp = new JsonParser();
    JsonObject LoginInputObject = (JsonObject)jp.parse(workFlowParams);
    LoginInputObject.addProperty("strTenantId", tenantId);
    LoginInputObject.addProperty("methodName", methodName);
    LoginInputObject.addProperty("strUserId", strUserId);

    System.out.println("Before invoking ValidateLogin:" + LoginInputObject.toString());
    JsonObject result = invokeWorkFlow("CoreLoginService", LoginInputObject);
    return result;
  }

  private boolean adAuthenticate(String userName, String password, MobileActionHandlerBk mobileActionHandler)
    throws ClassNotFoundException
  {
    DirContext ctx = null;
    try
    {
      Hashtable env = new Hashtable();
      env.put("java.naming.factory.initial", mobileActionHandler.getPropertyValues("INITIAL_CONTEXT_FACTORY"));
      env.put("java.naming.provider.url", mobileActionHandler.getPropertyValues("PROVIDER_URL"));
      env.put("java.naming.security.principal", mobileActionHandler.getPropertyValues("USERNAME"));
      String decryptionKey = mobileActionHandler.getPropertyValues("decryptionKey");
      String decryptedPassowrd = encryptOrDecrypt(decryptionKey, 2, mobileActionHandler.getPropertyValues("PASSWORD"));
      env.put("java.naming.security.credentials", decryptedPassowrd);

      ctx = new InitialDirContext(env);
      System.out.println("Object created " + ctx);
      boolean userValidationStatus = authenticateUser(userName, password, ctx, env, mobileActionHandler);
      boolean bool1 = userValidationStatus;
      return bool1;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
    finally
    {
      if (ctx != null)
        try
        {
          ctx.close();
        }
        catch (Exception e)
        {
          System.out.println("Error in closing the DirContext");
        }
    }
   }

  private static String encryptOrDecrypt(String key, int mode, String inputString)
    throws Exception
  {
    String responseString = null;
    DESKeySpec dks = new DESKeySpec(key.getBytes());
    SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
    SecretKey desKey = skf.generateSecret(dks);
    Cipher cipher = Cipher.getInstance("DES");
    if (mode == 1)
    {
      cipher.init(1, desKey);
      responseString = encrypt(cipher, inputString);
    }
    else if (mode == 2)
    {
      cipher.init(2, desKey);
      responseString = decrypt(cipher, inputString);
    }
    return responseString;
  }

  private static String encrypt(Cipher cipher, String str)
    throws Exception
  {
    byte[] utf8 = str.getBytes("UTF8");

    byte[] enc = cipher.doFinal(utf8);

    return new BASE64Encoder().encode(enc);
  }

  public static String decrypt(Cipher dcipher, String str)
    throws Exception
  {
    byte[] dec = new BASE64Decoder().decodeBuffer(str);

    byte[] utf8 = dcipher.doFinal(dec);

    return new String(utf8, "UTF8");
  }

  private boolean authenticateUser(String username, String password, DirContext ctx, Hashtable<String, String> env, MobileActionHandlerBk mobileActionHandler)
    throws IOException
  {
    NamingEnumeration results = null;
    try
    {
      String[] userAttributes = { 
        "distinguishedName", "cn", "name", 
        "givenname", "memberOf", "samaccountname", 
        "userPrincipalName" };

      SearchControls controls = new SearchControls();
      controls.setSearchScope(2);
      controls.setReturningAttributes(userAttributes);
      String baseString = mobileActionHandler.getPropertyValues("baseString");
      if ((baseString != null) && (baseString.toUpperCase().startsWith("CN="))) {
        baseString = baseString.substring(baseString.indexOf(',') + 1, baseString.length());
      }
      NamingEnumeration answer = ctx.search(baseString, "(& (" + mobileActionHandler.getPropertyValues("searchString") + "=" + username + ")(objectClass=user))", controls);
      if (answer.hasMore())
      {
        SearchResult result = (SearchResult)answer.next();
        Attributes attr = result.getAttributes();
        System.out.println("attr ---> " + attr.toString());

        Attribute distinguishedNameAttr = attr.get("distinguishedName");
        if (distinguishedNameAttr != null)
        {
          System.out.println("distinguished name --> " + distinguishedNameAttr.toString());

          env.put("java.naming.security.principal", distinguishedNameAttr.get().toString());
          env.put("java.naming.security.credentials", password);
          new InitialDirContext(env);
          return true;
        }
        return false;
      }
    }
    catch (AuthenticationException e)
    {
      e.printStackTrace();
      return false;
    }
    catch (NameNotFoundException e)
    {
      e.printStackTrace();
      return false;
    }
    catch (SizeLimitExceededException e)
    {
      throw new RuntimeException("LDAP Query Limit Exceeded, adjust the query to bring back less records", e);
    }
    catch (NamingException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      if (results != null) {
        try
        {
          results.close();
        }
        catch (Exception e)
        {
          System.out.println("Error in closing the NamingEnumeration");
        }
      }
      if (ctx != null)
        try
        {
          ctx.close();
        }
        catch (Exception e)
        {
          System.out.println("Error in closing the DirContext");
        }
    }
    if (results != null) {
      try
      {
        results.close();
      }
      catch (Exception e)
      {
        System.out.println("Error in closing the NamingEnumeration");
      }
    }
    if (ctx != null) {
      try
      {
        ctx.close();
      }
      catch (Exception e)
      {
        System.out.println("Error in closing the DirContext");
      }
    }

    if (results != null) {
      try
      {
        results.close();
      }
      catch (Exception e)
      {
        System.out.println("Error in closing the NamingEnumeration");
      }
    }
    if (ctx != null) {
      try
      {
        ctx.close();
      }
      catch (Exception e)
      {
        System.out.println("Error in closing the DirContext");
      }
    }
    if ((results == null) || 
      (ctx != null)) {
      try
      {
        ctx.close();
      }
      catch (Exception e)
      {
        System.out.println("Error in closing the DirContext");
      }
    }
    return false;
  }

  private static JsonObject invokeWorkFlow(String workFlowName, JsonObject args)
  {
    JsonParser jp = new JsonParser();

    JsonObject jsonObject = null;
    try
    {
      ActionHandlerNew actionHandlerNew = new ActionHandlerNew();
      String restUrl = actionHandlerNew.getPropertyValues("restUrl");
      RestClientApi restClientApi = new RestClientApi(restUrl);
      String tenantId = args.get("strTenantId").getAsString();
      System.out.println("tenantid------------------------" + tenantId);
      String result = restClientApi.executeService(tenantId, tenantId, 
        workFlowName, args);

      System.out.println("result as it is" + result);
      if (!result.isEmpty())
      {
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
    }
    catch (Exception e)
    {
      System.out.println(":: Exception while calling the workflow " + 
        workFlowName + "\n" + e.getMessage());
    }
    return jsonObject;
  }
}