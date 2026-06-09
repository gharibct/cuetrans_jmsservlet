package com.cuetrans.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

public class RestUtils {

	/**
	 * 
	 * @param exception
	 * @return
	 */
	public static Response getResponse(Exception exception) {

		ResponseBuilder responseBuilder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
		responseBuilder.type("text/html");
		responseBuilder.entity("<h1>Exception has occured  Refer log for more details</h1><br>The failure message is "
				+ exception.getMessage());
		responseBuilder.header("Access-Control-Allow-Origin", "*");
		return responseBuilder.build();
	}

	/**
	 * 
	 * @param status
	 * @param message
	 * @param contentType
	 * @return
	 */
	public static Response getReponse(Response.Status status, String message, String contentType) {
		Response.ResponseBuilder responseBuilder = Response.status(status);
		responseBuilder.type(contentType);
		responseBuilder.entity(message);
		responseBuilder.header("Access-Control-Allow-Origin", "*");

		return responseBuilder.build();
	}

	/**
	 * 
	 * @param url
	 * @param urlParameters
	 * @return
	 * @throws Exception
	 */
	public String sendPostHTTP(String url, String urlParameters) throws Exception {
		StringBuffer response = null;
		try {
			URL obj = new URL(url);

			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");

			// System.out.println(urlParameters);
			// System.out.println(url);

			// Send post request
			if (urlParameters != null) {
				con.setDoOutput(true);
				DataOutputStream wr = new DataOutputStream(con.getOutputStream());
				wr.writeBytes(urlParameters);
				wr.flush();
				wr.close();
			}

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// print result
		return response.toString();

	}

	/**
	 * To call HTTP - Get method
	 * 
	 * @param requestURL
	 * @return
	 */
	public String sendGetHttp(String requestURL) {
		String output = null;
		try {
			URL url = new URL(requestURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			conn.disconnect();

		} catch (Exception e) {

			e.printStackTrace();

		}
		return output;

	}

}
