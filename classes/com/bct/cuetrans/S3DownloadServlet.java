package com.bct.cuetrans;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;

public class S3DownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private AmazonS3 s3;

    private static final String AWS_REGION = "us-east-1";
    private static final String BUCKET_NAME = "awsuser-cuetrans";

    public void init() throws ServletException {
        s3 = AmazonS3ClientBuilder.standard()
                .withRegion(AWS_REGION)
                .build();
    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(true);
        if (session == null) {
            response.getWriter().write("Session expired");
            return;
        }

        String key = request.getParameter("key");
        if (key == null || key.trim().isEmpty()) {
            response.getWriter().write("Invalid file key");
            return;
        }

        S3Object s3Object = s3.getObject(BUCKET_NAME, key);

        response.setContentType(s3Object.getObjectMetadata().getContentType());
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + key.substring(key.lastIndexOf("/") + 1) + "\"");

        InputStream in = s3Object.getObjectContent();
        OutputStream out = response.getOutputStream();

        byte[] buffer = new byte[8192];
        int length;

        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }

        in.close();
        out.flush();
    }
}