package com.bct.cuetrans.mobile;
 
import com.bpms.core.webservice.WebserviceUtility;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray; 
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.xml.internal.ws.client.sei.ResponseBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


@Path("/mobile") 

public class MobileFileDownloadServlet extends HttpServlet
{

	 @GET 
	 @Path("/mobDownloadFile")
	 @Produces("application/pdf") 
	 public Response getDownloadFile () throws Exception 
	 {
		 
		 System.out.println("Loading MobileFileDownloadServlet.. ");
		 File file = new File ("/home/opc/FuelTransPlatFormCloud/CuecentGoCloud/webapps/FuelTrans/temp/OLA_ERD.pdf") ;
		 FileInputStream fileInputStream = new FileInputStream(file);
		 javax.ws.rs.core.Response.ResponseBuilder responseBuilder = javax.ws.rs.core.Response.ok((Object) fileInputStream);
		 responseBuilder.type("application/pdf");
		 responseBuilder.header("Content-Disposition", "filename=test.pdf");
		 return responseBuilder.build();
		    
	 }
}