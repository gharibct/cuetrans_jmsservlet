package com.bct.cuetrans;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.bpms.core.webservice.WebserviceUtility;
import com.bpms.engine.util.WebserviceAttachmentInfo;
import com.cuetrans.utils.PropertyUtility;
import java.util.Properties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import net.sf.json.JSONObject;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

public class UploadFileServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private ServletFileUpload uploader; 
	private AmazonS3 s3;

	private static String AWS_REGION = "eu-central-1";
	private static String BUCKET_NAME = "s3bucket-pdo-cuetrans-demo";
	private static final String BASE_FOLDER = "uploads/";

	public void init() throws ServletException {

		DiskFileItemFactory factory = new DiskFileItemFactory();
		File filesDir = (File) getServletContext().getAttribute(
				"FILES_DIR_FILE");
		factory.setRepository(filesDir);

		uploader = new ServletFileUpload(factory);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		JSONObject responseData = new JSONObject();
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		
		System.out.println("Inside doPost");

		String fastAPIFlag = PropertyUtility.getPropertyValues(
				"FastAPIFlag", getClass().getClassLoader());

		System.out.println("Fast API Flag: " + fastAPIFlag);
		HttpSession session = request.getSession(false);
		if (session == null) {
			System.out.println("File Upload Session Expired");
			responseData.put("Failure", "Session expired");
			out.write(responseData.toString());
			return;
		}

		if (!ServletFileUpload.isMultipartContent(request)) {
			System.out.println("File Upload - Invalid request type");
			responseData.put("Failure", "Invalid request type");
			out.write(responseData.toString());
			return;
		}

		try {

			List<FileItem> items = uploader.parseRequest(request);
			String uploadfilePath = request.getParameter("filePath");

			for (FileItem fileItem : items) {

				if (fileItem.isFormField())
					continue;

				String originalName = FilenameUtils.getName(fileItem.getName());
				String extension = FilenameUtils.getExtension(originalName)
						.toLowerCase();

				validateFile(fileItem, extension);

				// Fix: Check for null before calling equalsIgnoreCase
				if (fastAPIFlag != null && "Y".equalsIgnoreCase(fastAPIFlag)) {
					System.out.println("Calling fileUploadFastAPI with originalName: " + originalName + " and extension: " + extension);
					fileUploadFastAPI(fileItem, originalName, extension, responseData);
				} else {
					if ("LOCAL".equalsIgnoreCase(uploadfilePath)) {
						handleLocalUpload(originalName, responseData);
					} else if ("app".equalsIgnoreCase(uploadfilePath)) {
						handleAppWorkflowUpload(fileItem, originalName, extension,
								responseData);
					} else {
						handleS3Upload(fileItem, originalName, responseData);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			responseData.put("Failure", e.getMessage());
		}

		out.write(responseData.toString());
		out.flush();
	}

	private void validateFile(FileItem fileItem, String extension)
			throws Exception {

		int maxSize = Integer.parseInt(PropertyUtility.getPropertyValues(
				"MaxUploadFileSize", getClass().getClassLoader()));

		String fileExtensions = PropertyUtility.getPropertyValues(
				"AllowedFileExt", getClass().getClassLoader());

		List<String> extensionList = Arrays.asList(fileExtensions.split(","));

		if (!extensionList.contains(extension)) {
			throw new ServletException("Invalid File extension");
		}

		if (fileItem.getSize() / 1024 / 1024 > maxSize) {
			throw new ServletException("File size cannot be more than "
					+ maxSize + " MB");
		}
	}

	private void handleLocalUpload(String fileName, JSONObject responseData) {

		responseData.put("fileName", fileName);
		responseData.put("Success", "Local File Uploaded Successfully");
	}

	private void handleAppWorkflowUpload(FileItem fileItem, String fileName,
			String ext, JSONObject responseData) throws Exception {

		byte[] fileBytes = fileItem.get();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("fileName_id", UUID.randomUUID().toString());
		map.put("fileName", fileName);
		map.put("extension", ext);
		map.put("fileContent", fileBytes);

		List<Map<String, Object>> listObj = new ArrayList<Map<String, Object>>();
		listObj.add(map);

		HashMap<String, Object> inputVar = new HashMap();
		inputVar.put("arrList", listObj);

		String tenantId = PropertyUtility.getPropertyValues("tenantId",
				getClass().getClassLoader());

		WebserviceAttachmentInfo.tenantId = tenantId;

		String port = PropertyUtility.getPropertyValues("Port", getClass()
				.getClassLoader());

		String ipAddress = PropertyUtility.getPropertyValues("IPAddress",
				getClass().getClassLoader());

		long timeout = Long.parseLong(PropertyUtility.getPropertyValues(
				"TimeOut", getClass().getClassLoader()));

		Map result = WebserviceUtility.executeWorkFlow("fileQuery2", ipAddress,
				port, inputVar, timeout);

		responseData.put("fileName", result.get("finalStr") + "_" + fileName);

		responseData.put("Success", "File Uploaded Successfully");
	}

	private void handleS3Upload(FileItem fileItem, String originalName,
			JSONObject responseData) throws Exception {
		// Create S3 client once
		BUCKET_NAME = PropertyUtility.getPropertyValues("S3BucketName",
				getClass().getClassLoader());
		AWS_REGION = PropertyUtility.getPropertyValues("S3BucketRegion",
				getClass().getClassLoader());
		String guid= UUID.randomUUID().toString();
		s3 = AmazonS3ClientBuilder.standard().withRegion(AWS_REGION).build();
		String key = BASE_FOLDER + guid + "_"
				+ originalName;

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(fileItem.getSize());
		metadata.setContentType(fileItem.getContentType());

		InputStream in = fileItem.getInputStream();
		try {
			s3.putObject(new PutObjectRequest(BUCKET_NAME, key, in, metadata));
		} finally {
			in.close();
		}
		String fileName = guid
        + "_" + originalName;
		responseData.put("fileName", fileName);
		responseData.put("Success", "S3 File Uploaded Successfully");
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	private void fileUploadFastAPI(FileItem fileItem, String fileName,
								String ext, JSONObject responseData) throws Exception {
		
		byte[] fileBytes = fileItem.get();
		System.out.println("Calling fileUploadFastAPI with fileName: " + fileName + " and extension: " + ext);
		// Only get tenantId if needed for other purposes
		String tenantId = PropertyUtility.getPropertyValues("tenantId",
				getClass().getClassLoader());
		
		String port = PropertyUtility.getPropertyValues("PortRest", getClass()
				.getClassLoader());
		
		String ipAddress = PropertyUtility.getPropertyValues("IPAddressRest",
				getClass().getClassLoader());
		
		long timeout = Long.parseLong(PropertyUtility.getPropertyValues(
				"TimeOut", getClass().getClassLoader()));
		
		// Build URL with query parameters
		String urlString = "http://" + ipAddress + ":" + port + "/uploadFile";
		System.out.println("FastAPI URL: " + urlString);
		// Get optional parameters - provide defaults if not found
		// String userName = PropertyUtility.getPropertyValues("userName",
		// 		getClass().getClassLoader());
		// String fileType = PropertyUtility.getPropertyValues("fileType",
		// 		getClass().getClassLoader());
		
		// boolean hasQueryParam = false;
		// if (userName != null && !userName.trim().isEmpty()) {
		// 	urlString += "?userName=" + URLEncoder.encode(userName, "UTF-8");
		// 	hasQueryParam = true;
		// }
		// if (fileType != null && !fileType.trim().isEmpty()) {
		// 	if (!hasQueryParam) {
		// 		urlString += "?fileType=" + URLEncoder.encode(fileType, "UTF-8");
		// 	} else {
		// 		urlString += "&fileType=" + URLEncoder.encode(fileType, "UTF-8");
		// 	}
		// }
		
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		
		// Set multipart boundary
		String boundary = "----JavaMultipartBoundary" + System.currentTimeMillis();
		conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		conn.setRequestProperty("Accept", "application/json");
		conn.setConnectTimeout((int) timeout);
		conn.setReadTimeout((int) timeout);
		
		try (OutputStream outputStream = conn.getOutputStream();
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)) {
			
			// Add filePath as form field if available
			String filePath = PropertyUtility.getPropertyValues("filePath",
					getClass().getClassLoader());
			if (filePath != null && !filePath.trim().isEmpty()) {
				writer.append("--" + boundary).append("\r\n");
				writer.append("Content-Disposition: form-data; name=\"filePath\"").append("\r\n");
				writer.append("Content-Type: text/plain; charset=UTF-8").append("\r\n");
				writer.append("\r\n");
				writer.append(filePath).append("\r\n");
				writer.flush();
			}
			
			// Add file content
			writer.append("--" + boundary).append("\r\n");
			writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"").append("\r\n");
			writer.append("Content-Type: " + fileItem.getContentType()).append("\r\n");
			writer.append("\r\n");
			writer.flush();
			
			// Write file content as binary
			outputStream.write(fileBytes);
			outputStream.flush();
			writer.append("\r\n");
			writer.flush();
			
			// End of multipart
			writer.append("--" + boundary + "--").append("\r\n");
			writer.flush();
		}
		
		int responseCode = conn.getResponseCode();
		
		// Read response
		StringBuilder response = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream(), "UTF-8"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
		}
		
		conn.disconnect();
		
		if (responseCode < 200 || responseCode >= 300) {
			throw new IOException("FastAPI call failed. HTTP Status: " + responseCode + " Response: " + response.toString());
		}
		
		// Parse response
		JSONObject jsonResponse = JSONObject.fromObject(response.toString());
		if (jsonResponse.containsKey("fileName")) {
			responseData.put("fileName", jsonResponse.getString("fileName"));
		}
		responseData.put("Success", "File Uploaded Successfully to FastAPI");
		System.out.println("FastAPI Response Completed Successfully");
	}


}