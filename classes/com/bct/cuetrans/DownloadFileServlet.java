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
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
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
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;
import java.io.InputStream;

public class DownloadFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	JSONObject responseData = new JSONObject();

	private static String AWS_REGION = "eu-central-1";
	private static String BUCKET_NAME = "s3bucket-pdo-cuetrans-demo";
	private static String BASE_FOLDER = "uploads/";

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession(false);
		System.out.println("Inside doGet of DownloadFileServlet");
		if (session != null) {
			System.out.println("entity name:" + request.getParameter("entityName"));
			String entityName = request.getParameter("entityName");
			String fName = request.getParameter("fileName");
			System.out.println("fName " + fName);
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
			System.out.println("Before FastAPIFlag: ");
			// Check FastAPI flag
			String fastAPIFlag = PropertyUtility.getPropertyValues(
					"FastAPIFlag", getClass().getClassLoader());

			// -------- FASTAPI DOWNLOAD --------
			System.out.println("FastAPIFlag: " + fastAPIFlag);
			if (fastAPIFlag != null && "Y".equalsIgnoreCase(fastAPIFlag)) {
				downloadFromFastAPI(fileName, response);
				return;
			}
			
			// -------- S3 DOWNLOAD --------
			if ("S3".equalsIgnoreCase(uploadfilePath)) {
				downloadFromS3(fName, response);
				return;
			}

			String filePath = "";
			System.out.println("file_attachment_id " + fileName);
			if (uploadfilePath.equalsIgnoreCase("LOCAL")) {
				filePath = PropertyUtility.getPropertyValues("filePath", this
						.getClass().getClassLoader());
			} else {
				filePath = this.getServletContext().getRealPath("/");
				System.out.println("filePath ====" + filePath);
			}

			filePath = filePath + entityName + "/";
			System.out.println("filePath " + filePath);
			System.out.println("fileName " + fileName);
			System.out.println("Before calling the invokeEbPacFlow");
			String tenantId = PropertyUtility.getPropertyValues("tenantId",
					this.getClass().getClassLoader());
			Map resultMap = this.invokeEbPacFlow(tenantId, fileName, filePath);
			System.out.println("After calling the invokeEbPacFlow");
			ServletContext ctx = this.getServletContext();
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=\""
					+ resultMap.get("fileName") + "\"");
			ServletOutputStream os = response.getOutputStream();
			os.write((byte[]) resultMap.get("fileContent"));
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

	private void downloadFromFastAPI(String fileAttachmentId, HttpServletResponse response)
			throws IOException, ServletException {
		
		String port = PropertyUtility.getPropertyValues("PortRest", getClass()
				.getClassLoader());
		
		String ipAddress = PropertyUtility.getPropertyValues("IPAddressRest",
				getClass().getClassLoader());
		
		long timeout = Long.parseLong(PropertyUtility.getPropertyValues(
				"TimeOut", getClass().getClassLoader()));
		
		// Build URL
		String urlString = "http://" + ipAddress + ":" + port + "/downloadFile";
		
		System.out.println("FastAPI URL: " + urlString);
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Accept", "application/json");
		conn.setConnectTimeout((int) timeout);
		conn.setReadTimeout((int) timeout);
		
		// Build JSON request body
		JSONObject requestJson = new JSONObject();
		requestJson.put("file_attachment_id", fileAttachmentId);
		String jsonInput = requestJson.toString();
		System.out.println("FastAPI Request Body: " + jsonInput);
		
		try (OutputStream os = conn.getOutputStream()) {
			byte[] input = jsonInput.getBytes("UTF-8");
			os.write(input, 0, input.length);
		}
		
		int responseCode = conn.getResponseCode();
		System.out.println("Response Code: " + responseCode);
		
		if (responseCode < 200 || responseCode >= 300) {
			// Read error response
			StringBuilder errorResponse = new StringBuilder();
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(conn.getErrorStream(), "UTF-8"))) {
				String line;
				while ((line = reader.readLine()) != null) {
					errorResponse.append(line);
				}
			}
			conn.disconnect();
			throw new IOException("FastAPI download failed. HTTP Status: " + responseCode + 
								" Response: " + errorResponse.toString());
		}
		
		// Read the response as a JSON
		StringBuilder responseBody = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				responseBody.append(line);
			}
		}
		conn.disconnect();
		
		// System.out.println("Response Body: " + responseBody.toString());
		
		// Parse JSON response
		JSONObject jsonResponse = JSONObject.fromObject(responseBody.toString());
		
		if (!jsonResponse.containsKey("fileContent") || !jsonResponse.containsKey("fileName")) {
			throw new ServletException("Invalid response from FastAPI: Missing file content or filename");
		}
		
		String fileContentBase64 = jsonResponse.getString("fileContent");
		String fileName = jsonResponse.getString("fileName");
		String contentType = jsonResponse.containsKey("contentType") ? 
							jsonResponse.getString("contentType") : "application/octet-stream";
		
		// Decode base64 content
		byte[] fileContent = Base64.decode(fileContentBase64);
		
		// Set response headers
		response.setContentType(contentType);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		response.setContentLength(fileContent.length);
		
		// Write file content to response
		ServletOutputStream os = response.getOutputStream();
		os.write(fileContent);
		os.flush();
		os.close();
		
		System.out.println("File downloaded from FastAPI successfully: " + fileName);
	}

	private void downloadFromS3(String key, HttpServletResponse response)
			throws IOException {
		
		BUCKET_NAME = PropertyUtility.getPropertyValues("S3BucketName",
				getClass().getClassLoader());
		AWS_REGION = PropertyUtility.getPropertyValues("S3BucketRegion",
				getClass().getClassLoader());
		System.out.println("BUCKET_NAME " + BUCKET_NAME);
		System.out.println("AWS_REGION " + AWS_REGION);
		System.out.println("BASE_FOLDER " + BASE_FOLDER);
		System.out.println("key " + key);
		
		AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(AWS_REGION).build();
		System.out.println("s3");
		S3Object s3Object = s3.getObject(BUCKET_NAME, BASE_FOLDER + key);
		System.out.println("s3Object");
		response.setContentType(s3Object.getObjectMetadata().getContentType());

		response.setHeader("Content-Disposition", "attachment; filename=\""
				+ key + "\"");

		InputStream in = s3Object.getObjectContent();
		ServletOutputStream out = response.getOutputStream();

		byte[] buffer = new byte[8192];
		int bytesRead;

		while ((bytesRead = in.read(buffer)) != -1) {
			out.write(buffer, 0, bytesRead);
		}

		in.close();
		out.flush();
		out.close();
	}

	private Map<String, Object> invokeEbPacFlow(String tenantId,
			String file_attachment_id, String filePath) {
		HashMap inputMap = new HashMap();
		inputMap.put("file_attachment_id", file_attachment_id);
		WebserviceAttachmentInfo.tenantId = tenantId;
		Map resultMap = null;
		byte[] tempFileContent = (byte[]) null;

		try {
			String port = PropertyUtility.getPropertyValues("Port", this
					.getClass().getClassLoader());
			String ipAddress = PropertyUtility.getPropertyValues("IPAddress",
					this.getClass().getClassLoader());
			long iTimeOut = Long.parseLong(PropertyUtility.getPropertyValues(
					"TimeOut", this.getClass().getClassLoader()));
			System.out.println("Before calling the executeWorkFlow");
			HashMap outputMap = WebserviceUtility.executeWorkFlow(
					"selectFileContent", ipAddress, port, inputMap);
			System.out.println("After calling the executeWorkFlow");
			if (outputMap != null) {
				resultMap = new HashMap();
				String ja = (String) outputMap.get("contentArr");
				Gson gson = (new GsonBuilder()).create();
				JsonArray je = getJsonValue(ja, gson).getAsJsonArray();
				JsonObject jo = je.get(0).getAsJsonObject();
				String blobContent = jo.get("gocloud_binary_fileContent")
						.getAsString();
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

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}
}