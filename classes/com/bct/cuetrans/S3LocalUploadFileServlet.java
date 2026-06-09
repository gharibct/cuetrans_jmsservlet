package com.bct.cuetrans;

import java.io.File;
import java.util.UUID;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService; 
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder; 
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;

public class S3LocalUploadFileServlet {
	 	private static final String BUCKET_NAME = "awsuser-cuetrans";
	    private static final String BASE_FOLDER = "uploads/";
    public static void main(String[] args) {

        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion("us-east-1")
                .build();
        System.out.println("S3 client created. Bucket exists? " + s3.doesBucketExistV2("awsuser-cuetrans"));
        AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder.standard() .withRegion("us-east-1") .build(); 
        GetCallerIdentityResult identity = sts.getCallerIdentity(new GetCallerIdentityRequest());
        System.out.println("Account: " + identity.getAccount()); 
        System.out.println("User ARN: " + identity.getArn());
        
        String bucketName = "awsuser-cuetrans";
        String key = BASE_FOLDER + UUID.randomUUID().toString() + "_"
		+ "PodImage1.jpg";


        File file = new File("C:\\Users\\rr140011\\Downloads\\PodImage.jpg");

        s3.putObject(new PutObjectRequest(bucketName, key, file));

        System.out.println("File uploaded successfully.");
    }
}