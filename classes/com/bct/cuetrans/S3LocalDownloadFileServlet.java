package com.bct.cuetrans;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;

public class S3LocalDownloadFileServlet {

    private static final String BUCKET_NAME = "awsuser-cuetrans";
    private static final String BASE_FOLDER = "uploads/";

    public static void main(String[] args) throws Exception {

        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion("us-east-1")
                .build();

        String key = "76350011-7b79-4c1c-94c7-b9170239052b_PodImage1.jpg";

        S3Object s3Object = s3.getObject(BUCKET_NAME, BASE_FOLDER + key);

        InputStream in = s3Object.getObjectContent();

        Files.copy(in,
                Paths.get("C:\\Users\\rr140011\\Downloads\\Downloaded_PodImage1.jpg"),
                StandardCopyOption.REPLACE_EXISTING);

        in.close();

        System.out.println("File downloaded successfully.");
    }
}