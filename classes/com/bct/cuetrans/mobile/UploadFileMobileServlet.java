// 
// Decompiled by Procyon v0.5.36
// 
 
package com.bct.cuetrans.mobile;

import java.util.UUID;
import java.io.ByteArrayOutputStream;

import com.bpms.core.webservice.WebserviceUtility;
import com.bpms.engine.util.WebserviceAttachmentInfo;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonArray;
import com.bct.bpms.client.rest.RestClientApi;
import com.bct.bpms.encoder.Base64;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.Reader;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.lang.reflect.Type;

import com.google.gson.JsonParser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import com.cuetrans.utils.PropertyUtility;
import net.sf.json.JSONObject;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

@WebServlet({ "/UploadFileMobileService" })
public class UploadFileMobileServlet extends HttpServlet
{
  
	public class UploadFileMobileServlet$1 extends TypeToken<List<JsonObject>> {

		 public UploadFileMobileServlet$1()
		 {
			 
		 }
		 
		public UploadFileMobileServlet$1(UploadFileMobileServlet uploadFileMobileServlet) {
			// TODO Auto-generated constructor stub
		}} {


	}

	private String tenantId;
    private String ipAddress;
    private String portNo; 
    public static final String IMAGE_UPLOAD = "imageUpload";
    public static final String IMAGE_DOWNLOAD = "imageDownload";
    public HttpServletResponse response;
    private static final long serialVersionUID = 1L;
    private Map workFlowParamsMap;
    
    public UploadFileMobileServlet() {
        this.tenantId = null;
        this.ipAddress = null;
        this.portNo = null;
        this.workFlowParamsMap = null;
    }
    
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IllegalArgumentException, IOException {
        System.out.println("UploadFileMobileServlet >> doGet() >> Entered to the block !!");
        final JSONObject finalResponseData = new JSONObject();
        this.response = response;
        this.portNo = PropertyUtility.getPropertyValues("Port", this.getClass().getClassLoader());
        this.ipAddress = PropertyUtility.getPropertyValues("IPAddress", this.getClass().getClassLoader());
        this.tenantId = PropertyUtility.getPropertyValues("tenantId", this.getClass().getClassLoader());
        InputStream is = null;
        is = (InputStream)request.getInputStream();
        String req_json = "";
        try {
            System.out.println("just before reading>>....");
            if (is != null) {
                req_json = new String(this.read(is));
                
                System.out.println("just before reading complete....");
            }
            else {
                System.out.println("Input Stream is empty!!");
            }
        }
        catch (Exception exp) {
            System.out.println("Exception occured while reading InputStream " + exp);
            exp.printStackTrace();
            finalResponseData.put((Object)"returnMessage", (Object)"-1");
            this.response.getWriter().write(finalResponseData.toString());
            throw new ServletException("Could not read the http request.", (Throwable)exp);
        }
        finally {
            if (is != null) {
                is.close();
                is = null;
            }
        }
        if (is != null) {
            is.close();
            is = null;
        }
        this.parseReqJson(req_json);
    }
    
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
    
    protected String parseReqJson(final String reqJson) throws IllegalArgumentException {
        try {
            final JsonElement je = this.getJsonValue(reqJson);
            if (je.getAsJsonObject().get("inputType") != null) {
                final String inputType = je.getAsJsonObject().get("inputType").getAsString();
                if (inputType != null) {
                    if (inputType.equals("imageUpload")) {
                        this.invokeImageUpload(reqJson);
                    }
                    else {
                        if (!inputType.equals("imageDownload")) {
                            throw new IllegalArgumentException("UploadFileMobileServlet >> parseReqJson() >> Invalid input type is entered !!!");
                        }
                        this.invokeImageDownload(reqJson);
                    }
                }
            }
        }
        catch (Exception e) {
        	System.out.println("Exception in parseReqJson()->"+ e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public void invokeImageUpload(final String reqJson) {
        final JsonObject finalResponseData = new JsonObject();
        Map result = null;
        final Gson gson = new Gson();
        final JsonParser parser = new JsonParser();
        final JsonArray arr = null;
        try {
            JsonObject json = null;
            try {
                System.out.println("reqJson:" + reqJson.toString());
                final JsonReader reader = new JsonReader((Reader)new StringReader(reqJson));
                reader.setLenient(true);
                json = (JsonObject)parser.parse(reader);
            }
            catch (JsonSyntaxException e) {
                e.printStackTrace();
                final JsonReader reader2 = new JsonReader((Reader)new StringReader(reqJson));
                reader2.setLenient(true);
                json = (JsonObject)parser.parse(reader2);
            }
            if (json == null) {
                throw new IllegalArgumentException("UploadFileMobileServlet :: doImageUpload() >> Invalid Request Json entered!!!");
            }
            HashMap<String, Object> hmJsn = null;
            System.out.println("je11:" + json.get("mobileImagesHMVar"));
            System.out.println("je2:" + json.get("workFlowParams"));
            JsonObject jo1 = new JsonObject();
            jo1 = (JsonObject)json.get("workFlowParams");
            final JsonElement je = json.get("mobileImagesHMVar");
            if (je != null) { 
                System.out.println("je:" + je.toString());
                final String hmStringJs = je.toString();
                hmJsn = (HashMap<String, Object>)gson.fromJson(hmStringJs, (Class)HashMap.class);
            }
            if (hmJsn == null) {
                throw new IllegalArgumentException("UploadFileMobileServlet :: doImageUpload() >> mobileImagesHMVar string value is Null ");
            }
            final ArrayList<HashMap<String, Object>> listObj = new ArrayList<HashMap<String, Object>>();
            for (final Map.Entry<String, Object> entry : hmJsn.entrySet()) {
                final String fileName = entry.getKey();
                final String fileContent =(String) entry.getValue();
                final byte[] fileBytes = Base64.decode(fileContent); 
               // final String hexBytes  = Base64.encodeBytes(fileBytes);
               //  System.out.println("File Content = " + hexBytes);
                final HashMap<String, Object> propMap = new HashMap<String, Object>();
                propMap.put("fileName_id", this.getUUID());
                propMap.put("fileName", fileName);
                propMap.put("extension", fileName.substring(fileName.lastIndexOf(46)));
                propMap.put("fileContent", fileBytes);
                listObj.add(propMap);
            }
            
            final String tenantId = PropertyUtility.getPropertyValues("tenantId", this.getClass().getClassLoader());
            
            //final String restUrl = PropertyUtility.getPropertyValues("restUrl", this.getClass().getClassLoader());
            //System.out.println("restUrl:" + restUrl + ":" + this.tenantId);
            //final RestClientApi restClientApi = new RestClientApi(restUrl);
            
            final HashMap<Object, Object> inputVar = new HashMap<Object, Object>();
            inputVar.put("arrList", listObj);
            
            WebserviceAttachmentInfo.tenantId = tenantId;
             
            final String port = PropertyUtility.getPropertyValues("Port", this.getClass().getClassLoader());
            final String ipAddress = PropertyUtility.getPropertyValues("IPAddress", this.getClass().getClassLoader());
            final long iTimeOut = Long.parseLong(PropertyUtility.getPropertyValues("TimeOut", this.getClass().getClassLoader()));
            try {
                //result = restClientApi.executeService(this.tenantId, this.tenantId, "fileQuery2", (Map)inputVar);
                
            	/** Changed by Thiyagu 07-10-2020*/
            	
                result = WebserviceUtility.executeWorkFlow("fileQuery2", ipAddress, port, (HashMap)inputVar, iTimeOut);
                System.out.println("Service Executed.");
            }
            catch (Exception ex) {
                System.out.println("Exception occured while invoking the service " + ex.getMessage());
                ex.printStackTrace();
                finalResponseData.addProperty("returnMessage", "File Upload failed .");
                this.response.getWriter().write(finalResponseData.toString());
            }
            final JsonParser jp = new JsonParser();
            final String retFileId = null;
            JsonArray jsonArray = null; 
            if (result != null) {
                final ArrayList<String> list = (ArrayList<String>) result.get("finalArray");
                final JsonElement element = gson.toJsonTree((Object)list, new UploadFileMobileServlet.UploadFileMobileServlet$1(this).getType());
                jsonArray = element.getAsJsonArray(); 
            } 
            else {
                finalResponseData.addProperty("returnMessage", "File Upload failed.");
                this.response.getWriter().write(finalResponseData.toString());
            }
            if (jsonArray != null && !jsonArray.isJsonNull() && retFileId != "{}") {
                finalResponseData.addProperty("returnMessage", "File Uploaded Successfully");
                System.out.println("File Uploaded Successfully:" + retFileId);
                finalResponseData.add("uploaded_mobile_image_reference_ids", (JsonElement)jsonArray);
                System.out.println("File Uploaded Successfully:" + finalResponseData.toString());
                this.response.getWriter().write(finalResponseData.toString());
            }
            else {
                finalResponseData.addProperty("returnMessage", "File Upload got failed.");
                this.response.getWriter().write(finalResponseData.toString());
            }
        }
        catch (IOException e2) {
            e2.printStackTrace();
            finalResponseData.addProperty("returnMessage", "File Upload got failed.");
            try {
                this.response.getWriter().write(finalResponseData.toString());
            }
            catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
    }
    
    public void invokeImageDownload(final String req_json) {
        final JSONObject responseData = new JSONObject();
        final JSONObject finalResponseData = new JSONObject();
        try {
            final JsonElement jelement = new JsonParser().parse(req_json);
            if (jelement == null) {
                throw new IllegalArgumentException("UploadFileMobileServlet :: doImageDownload() >> Invalid Request JsonElement encountered!!!");
            }
            final JsonObject jobject = jelement.getAsJsonObject();
            if (jobject == null) {
                throw new IllegalArgumentException("UploadFileMobileServlet :: doImageDownload() >> Invalid Request Json encountered!!!");
            }
            final JsonArray jsonArray = jobject.getAsJsonArray("mobile_image_reference_ids");
            if (jsonArray == null) {
                throw new IllegalArgumentException("UploadFileMobileServlet :: doImageDownload() >> 'mobile_image_reference_ids' variable is not found in requested json!!!");
            }
            for (int arrLen = jsonArray.size(), i = 0; i < arrLen; ++i) {
                String outputMap = null;
                final JsonParser jp = new JsonParser();
                final String file_attachment_id = jsonArray.get(i).getAsString();
                final String restUrl = PropertyUtility.getPropertyValues("restUrl", this.getClass().getClassLoader());
                final RestClientApi restClientApi = new RestClientApi(restUrl);
                System.out.println(String.valueOf(i) + ". file_attachment_id >>  " + file_attachment_id);
                JsonObject inputMap = inputMap = new JsonObject();
                WebserviceAttachmentInfo.tenantId = this.tenantId;
                inputMap.addProperty("file_attachment_id", file_attachment_id);
                System.out.println("Before invoking the Service");
                outputMap = restClientApi.executeService(this.tenantId, this.tenantId, "selectFileContent", inputMap);
                if (outputMap != null && !outputMap.isEmpty()) {
                    System.out.println("Service successfully executed");
                    final JsonObject jsonObject = (JsonObject)jp.parse(outputMap);
                    final String returnList = jsonObject.get("contentArr").toString();
                    if (returnList != null) {
                        final JsonArray je = this.getJsonValue(returnList).getAsJsonArray();
                        final JsonObject jo = je.get(0).getAsJsonObject();
                        final String blobContent = jo.get("gocloud_binary_fileContent").getAsString();
                        responseData.put((Object)file_attachment_id, (Object)blobContent);
                    }
                    else {
                        responseData.put((Object)"returnMessage", (Object)"Provided ID's not found");
                    }
                }
            }
            finalResponseData.put((Object)"returnMessage", (Object)"File Downloaded Successfully");
            finalResponseData.put((Object)"downloaded_mobile_image_reference_ids", (Object)responseData);
            this.response.getWriter().write(finalResponseData.toString());
            System.out.println("UploadFileMobileServlet :: doImageDownload() >>  Images downloaded successfully!!!");
        }
        catch (IOException e) {
            System.out.println("Exception occered at Downloading image " + e.getMessage());
            e.printStackTrace();
            responseData.put((Object)"returnMessage", (Object)"Failed, cannot be downloaded the file.");
        }
    }
    
    protected JsonElement getJsonValue(final String inStr) {
        final JsonParser jp = new JsonParser();
        final JsonElement je = jp.parse(inStr);
        return je;
    }
    
    private byte[] read(final InputStream is) throws Exception {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final byte[] data = new byte[63];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }
    
    private String getUUID() {
        final String iUID = UUID.randomUUID().toString();
        return iUID;
    }
}