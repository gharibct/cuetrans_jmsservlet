package com.bct.cuetrans;

import com.bpms.core.webservice.WebserviceUtility;
import com.bpms.engine.util.WebserviceAttachmentInfo;
import com.cuetrans.utils.PropertyUtility;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

public class UploadFileServletBkp extends HttpServlet {
   private static final long serialVersionUID = 1L;
   private ServletFileUpload uploader = null;
   JSONObject responseData = new JSONObject();

   public void init() throws ServletException {
	   final DiskFileItemFactory fileFactory = new DiskFileItemFactory();
	   final File filesDir = (File)this.getServletContext().getAttribute("FILES_DIR_FILE");
       fileFactory.setRepository(filesDir);
       this.uploader = new ServletFileUpload(fileFactory);
   }

   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      if (!ServletFileUpload.isMultipartContent(request)) {
         throw new ServletException("Content type is not multipart/form-data");
      } else {
    	  final String json = null;
    	  final PrintWriter out = response.getWriter();
    	  final HttpSession session = request.getSession(false);
         if (session != null) {
            try {
            	final List fileItemsList = this.uploader.parseRequest(request);
               System.out.println("entity name:" + request.getParameter("entityName"));
               final String entityName = request.getParameter("entityName");
               final String uploadfilePath = request.getParameter("filePath");
               final Iterator fileItemsIterator = fileItemsList.iterator();
               final int maxSize = Integer.parseInt(PropertyUtility.getPropertyValues("MaxUploadFileSize", this.getClass().getClassLoader()));
               System.out.println("maxSize:" + maxSize);
               final String fileExtensions = PropertyUtility.getPropertyValues("AllowedFileExt", this.getClass().getClassLoader());
               final List<String> extensionList = Arrays.asList(fileExtensions.split(","));
               this.responseData.clear();

               while(fileItemsIterator.hasNext()) {
                  String fileName = null;
                  FileItem fileItem = (FileItem)fileItemsIterator.next();
                  System.out.println("fileName:" + fileName + ",Size:" + fileItem.getSize());
                  if (!extensionList.contains(FilenameUtils.getExtension(fileItem.getName().toLowerCase()))) {
                     System.out.println("File extension : Exception");
                     throw new ServletException("Invalid File extension");
                  }

                  if (fileItem.getSize() / 1024L / 1024L > (long)maxSize) {
                     System.out.println("File Size Exception : File size cannot be more than " + maxSize + " MB");
                     throw new ServletException("File size cannot be more than " + maxSize + " MB");
                  }

                  String tenantId = PropertyUtility.getPropertyValues("tenantId", this.getClass().getClassLoader());
                  String ext = FilenameUtils.getExtension(fileItem.getName());
                  System.out.println("FieldName=" + fileItem.getFieldName());
                  System.out.println("FileName=" + fileItem.getName());
                  System.out.println("ContentType=" + fileItem.getContentType());
                  System.out.println("Size in bytes=" + fileItem.getSize());
                  Map result = null;
                  String filePath = "";
                  if (uploadfilePath.equalsIgnoreCase("LOCAL")) {
                     filePath = PropertyUtility.getPropertyValues("filePath", this.getClass().getClassLoader());
                  } else {
                     filePath = this.getServletContext().getRealPath("/");
                     System.out.println("filePath ====" + filePath);
                     System.out.println("uploaded Path--" + uploadfilePath);
                     new File(uploadfilePath);
                     byte[] fileBytes = fileItem.get();
                     ArrayList<HashMap<String, Object>> listObj = new ArrayList();
                     HashMap<String, Object> map = new HashMap();
                     fileName = fileItem.getName();
                     map.put("fileName_id", this.getUUID());
                     map.put("fileName", fileName);
                     map.put("extension", ext);
                     map.put("fileContent", fileBytes);
                     listObj.add(map);
                     HashMap<String, Object> inputVar = new HashMap();
                     inputVar.put("arrList", listObj);
                     WebserviceAttachmentInfo.tenantId = tenantId;
                     String port = PropertyUtility.getPropertyValues("Port", this.getClass().getClassLoader());
                     String ipAddress = PropertyUtility.getPropertyValues("IPAddress", this.getClass().getClassLoader());
                     long iTimeOut = Long.parseLong(PropertyUtility.getPropertyValues("TimeOut", this.getClass().getClassLoader()));

                     try {
                        result = WebserviceUtility.executeWorkFlow("fileQuery2", ipAddress, port, inputVar, iTimeOut);
                     } catch (Exception var30) {
                        System.out.println("Exception occured while invoking the service " + var30.getMessage());
                        var30.printStackTrace();
                     }

                     System.out.println("Output is " + result);
                     System.out.println("Final File String" + result.get("finalStr"));
                  }

                  this.responseData.put("fileName", result.get("finalStr") + "_" + fileName);
                  this.responseData.put("Success", "File Uploaded Successfully");
               }
            } catch (FileUploadException var31) {
               this.responseData.put("Failure", "File Uploading Failed...");
            } catch (ServletException var32) {
               this.responseData.put("Failure", var32.getMessage());
            } catch (Exception var33) {
               this.responseData.put("Failure", "Error Occurred while uploading...");
            }

            try {
               response.getWriter().write(this.responseData.toString());
            } catch (IOException var29) {
               var29.printStackTrace();
            }
         } else {
            this.responseData.put("Failure", "Session expired");
            response.getWriter().write(this.responseData.toString());
         }

      }
   }

   private String getUUID() {
      String iUID = UUID.randomUUID().toString();
      return iUID;
   }

   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      this.doGet(request, response);
   }
}
