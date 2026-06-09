package com.bct.cuetrans;

import com.bct.bpms.encoder.Base64;
import com.bpms.core.webservice.WebserviceUtility;
import com.bpms.engine.util.WebserviceAttachmentInfo;
import com.cuetrans.utils.PropertyUtility;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.sf.json.JSONObject;

public class DownloadFileServletBkp extends HttpServlet {
   private static final long serialVersionUID = 1L;
   JSONObject responseData = new JSONObject();

   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      HttpSession session = request.getSession(false);
      if (session != null) {
         System.out.println("entity name:" + request.getParameter("entityName"));
         String entityName = request.getParameter("entityName");
         String fName = request.getParameter("fileName");
         String fileName = null;
         if (fName.indexOf(95) == -1) {
            fileName = fName;
         } else {
            fileName = fName.substring(0, fName.indexOf(95));
         }

         String uploadfilePath = request.getParameter("filePath");
         System.out.println("fileName " + fileName);
         System.out.println("uploadfilePath " + uploadfilePath);
         if (fileName == null || fileName.equals("")) {
            throw new ServletException("File Name can't be null or empty");
         }

         String filePath = "";
         System.out.println("file_attachment_id " + fileName);
         if (uploadfilePath.equalsIgnoreCase("LOCAL")) {
            filePath = PropertyUtility.getPropertyValues("filePath", this.getClass().getClassLoader());
         } else {
            filePath = this.getServletContext().getRealPath("/");
            System.out.println("filePath ====" + filePath);
         }

         filePath = filePath + entityName + "/";
         System.out.println("filePath " + filePath);
         System.out.println("fileName " + fileName);
         System.out.println("Before calling the invokeEbPacFlow");
         String tenantId = PropertyUtility.getPropertyValues("tenantId", this.getClass().getClassLoader());
         Map resultMap = this.invokeEbPacFlow(tenantId, fileName, filePath);
         System.out.println("After calling the invokeEbPacFlow");
         ServletContext ctx = this.getServletContext();
         response.setContentType("application/octet-stream");
         response.setHeader("Content-Disposition", "attachment; filename=\"" + resultMap.get("fileName") + "\"");
         ServletOutputStream os = response.getOutputStream();
         os.write((byte[])resultMap.get("fileContent"));
         os.flush();
         os.close();
         System.out.println("File downloaded at client successfully");
         RequestDispatcher rd = request.getRequestDispatcher("/index.html");
         rd.forward(request, response);
      } else {
         this.responseData.put("Failure", "Session expired");
         response.getWriter().write(this.responseData.toString());
      }

   }

   private Map<String, Object> invokeEbPacFlow(String tenantId, String file_attachment_id, String filePath) {
      HashMap inputMap = new HashMap();
      inputMap.put("file_attachment_id", file_attachment_id);
      WebserviceAttachmentInfo.tenantId = tenantId;
      Map resultMap = null;
      byte[] tempFileContent = (byte[])null;

      try {
         String port = PropertyUtility.getPropertyValues("Port", this.getClass().getClassLoader());
         String ipAddress = PropertyUtility.getPropertyValues("IPAddress", this.getClass().getClassLoader());
         long iTimeOut = Long.parseLong(PropertyUtility.getPropertyValues("TimeOut", this.getClass().getClassLoader()));
         System.out.println("Before calling the executeWorkFlow");
         HashMap outputMap = WebserviceUtility.executeWorkFlow("selectFileContent", ipAddress, port, inputMap);
         System.out.println("After calling the executeWorkFlow");
         if (outputMap != null) {
            resultMap = new HashMap();
            String ja = (String)outputMap.get("contentArr");
            Gson gson = (new GsonBuilder()).create();
            JsonArray je = getJsonValue(ja, gson).getAsJsonArray();
            JsonObject jo = je.get(0).getAsJsonObject();
            String blobContent = jo.get("gocloud_binary_fileContent").getAsString();
            String fileName = jo.get("FILE_NAME").getAsString();
            tempFileContent = Base64.decode(blobContent);
            resultMap.put("fileName", fileName);
            resultMap.put("fileContent", tempFileContent);
         }
      } catch (IOException var18) {
         var18.printStackTrace();
      }

      return resultMap;
   }

   private static JsonElement getJsonValue(String inStr, Gson gson) {
      JsonParser jp = new JsonParser();
      JsonElement je = jp.parse(inStr);
      return je;
   }

   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      this.doGet(request, response);
   }
}
