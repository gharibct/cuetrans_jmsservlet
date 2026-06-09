package com.bct.cuetrans;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.bpms.core.webservice.WebserviceUtility;
import com.bpms.engine.util.WebserviceAttachmentInfo;
import com.cuetrans.utils.PropertyUtility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
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

		HttpSession session = request.getSession(false);
		if (session == null) {
			responseData.put("Failure", "Session expired");
			out.write(responseData.toString());
			return;
		}

		if (!ServletFileUpload.isMultipartContent(request)) {
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

				if ("LOCAL".equalsIgnoreCase(uploadfilePath)) {
					handleLocalUpload(originalName, responseData);
				} else if ("app".equalsIgnoreCase(uploadfilePath)) {
					handleAppWorkflowUpload(fileItem, originalName, extension,
							responseData);
				} else {
					handleS3Upload(fileItem, originalName, responseData);
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
}