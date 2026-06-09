package com.bct.cuetrans;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class S3UploadFileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ServletFileUpload uploader;
    private AmazonS3 s3;

    private static final String AWS_REGION = "us-east-1";
    private static final String BUCKET_NAME = "awsuser-cuetrans";
    private static final String BASE_FOLDER = "uploads/";

    // Allowed file extensions
    private static final List<String> ALLOWED_EXTENSIONS =
            Arrays.asList(
                    "jpg","jpeg","png","gif","webp",
                    "pdf","doc","docx","xls","xlsx","txt"
            );

    // 20MB max
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    public void init() throws ServletException {

        DiskFileItemFactory factory = new DiskFileItemFactory();
        uploader = new ServletFileUpload(factory);

        uploader.setFileSizeMax(MAX_FILE_SIZE);
        uploader.setSizeMax(MAX_FILE_SIZE + (5 * 1024 * 1024));

        s3 = AmazonS3ClientBuilder.standard()
                .withRegion(AWS_REGION)
                .build();  // Works local + EC2
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        JSONObject json = new JSONObject();
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(true);
        if (session == null) {
            json.put("Failure", "Session expired");
            out.write(json.toString());
            return;
        }

        if (!ServletFileUpload.isMultipartContent(request)) {
            json.put("Failure", "Invalid request");
            out.write(json.toString());
            return;
        }

        try {

            @SuppressWarnings("unchecked")
            List<FileItem> items = uploader.parseRequest(request);
            Iterator<FileItem> it = items.iterator();

            while (it.hasNext()) {

                FileItem item = it.next();
                if (item.isFormField()) continue;

                // Validate file size
                if (item.getSize() <= 0 || item.getSize() > MAX_FILE_SIZE) {
                    throw new ServletException("Invalid file size");
                }

                String originalName =
                        FilenameUtils.getName(item.getName());

                String extension =
                        FilenameUtils.getExtension(originalName).toLowerCase();

                // Validate extension
                if (!ALLOWED_EXTENSIONS.contains(extension)) {
                    throw new ServletException("File type not allowed");
                }

                // Validate content type basic check
                String contentType = item.getContentType();
                if (contentType == null || contentType.trim().isEmpty()) {
                    throw new ServletException("Invalid content type");
                }

                // Organized folder structure by date
                String dateFolder =
                        new SimpleDateFormat("yyyy/MM/dd").format(new Date());

                String key = BASE_FOLDER 
                        + UUID.randomUUID().toString()
                        + "_" + originalName;

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(item.getSize());
                metadata.setContentType(contentType);

                InputStream in = item.getInputStream();
                try {
                    s3.putObject(new PutObjectRequest(
                            BUCKET_NAME,
                            key,
                            in,
                            metadata
                    ));
                } finally {
                    in.close();
                }
                	
                json.put("Success", "File uploaded successfully");
                String fileName = UUID.randomUUID().toString()
                + "_" + originalName;
                json.put("fileName", fileName);
            }

        } catch (Exception e) {
            e.printStackTrace();
            json.put("Failure", e.getMessage());
        }

        out.write(json.toString());
        out.flush();
    }

    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }
}