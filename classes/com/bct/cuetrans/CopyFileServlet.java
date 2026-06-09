package com.bct.cuetrans;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;

import com.bct.bpms.encoder.Base64;
import com.bpms.core.webservice.WebserviceUtility;
import com.bpms.engine.util.WebserviceAttachmentInfo;
import com.cuetrans.utils.PropertyUtility;
 
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.util.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServlet;

public class CopyFileServlet extends HttpServlet
{
    private static   String  SOURCE_FOLDER;
    private static   String  OUTPUT_FILE;
    private static List<String> fileList = new ArrayList<String>(); 
    
    
    private static final long serialVersionUID = 1L;
    
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        System.out.println("CopyFileServlet --> Start");
        
        final String entityName = request.getParameter("entityName");
        final String isServerStore         = request.getParameter("isServerStore"); 
        final String strStoreageTransNo    = request.getParameter("strStoreageTransNo");
        final String strStoreageRefTransNo = request.getParameter("strStoreageRefTransNo");
        final String fileName              = request.getParameter("fileName");
        final String uploadfilePath        = request.getParameter("filePath");
        String filePath = "";
        
        System.out.println("CopyFileServlet --> filePath " + filePath);
	    System.out.println("CopyFileServlet --> fileName " + fileName);
	        
        byte[] tempFileContent = null ; 
        
		String  documentFilePath = PropertyUtility.getPropertyValues("ServerFileUploadPath", this.getClass().getClassLoader()); 
		
		 
		SOURCE_FOLDER = documentFilePath+"//"+strStoreageRefTransNo;
		 
	    
	    OUTPUT_FILE   = documentFilePath+"//"+ strStoreageTransNo ;
	   
	    System.out.println("CopyFileServlet --> SOURCE_FOLDER " + SOURCE_FOLDER +"//"+fileName );
	    System.out.println("CopyFileServlet --> OUTPUT_FILE " + OUTPUT_FILE +"//"+ fileName );
	    
	    
	    if (strStoreageRefTransNo != null && !strStoreageRefTransNo.trim().equals("")) 
    	{
    		
	    FileManager.createFile(documentFilePath, strStoreageTransNo ,fileName);
	    
	    if ( fileName != null && !fileName.trim().equals("") && !fileName.trim().equalsIgnoreCase("null"))
	    {
	   	    File file = new File (SOURCE_FOLDER+"//"+fileName);
	   	    
	   	    FileOutputStream fWriter =  new FileOutputStream(OUTPUT_FILE+"/"+fileName);
	   	    
        /** This block used to store the download files in the server */
        /*START*/
        if(isServerStore != null && isServerStore.equalsIgnoreCase("Y")) 
        {
        	
    	    try  
    		   {
    	         FileInputStream fileInput = new FileInputStream(file);
    	        
    	         byte[] dataBuffer = IOUtils.toByteArray(fileInput);
    	         
      		     fWriter.write(dataBuffer);
      		     fWriter.close();

      		     fileInput.close(); 
      		    
      		    } catch (FileNotFoundException e) 
    		    { 
      		    	if (fileName == null || fileName.equals("")) 
    	        	{
    	   	            throw new ServletException("File Name can't be null or empty");
    	   	        }
      		        if (uploadfilePath.equalsIgnoreCase("LOCAL"))
  	   	            {
  	   	             filePath = PropertyUtility.getPropertyValues("filePath", this.getClass().getClassLoader());
  	   	            }
      		        else
  	   	            {
  	   	             filePath = this.getServletContext().getRealPath("/");
  	   	             System.out.println("CopyFileServlet --> filePath ====" + filePath);
  	   	            }
      		        
      		       filePath = String.valueOf(filePath) + entityName + "/";
  	   	           System.out.println("CopyFileServlet --> Before calling the invokeEbPacFlow");    	   	        
  	   	           tempFileContent = this.invokeEbPacFlow("cuecent_tenant", fileName, filePath);
  	   	        
	  	   	      try
	 	   	      {
	  	   	    	  if (tempFileContent != null)
	  	   	    	  {
		       		      fWriter.write(tempFileContent);
		       		      fWriter.close();
	  	   	    	  }
	       		   } catch (IOException e1) 
	 	   	      { 
	       		     e1.printStackTrace();
	       		  }
	 	   	     
      		     }
    		    finally
    		    {
    			  
    		    }
             }
          }
    	}
         final RequestDispatcher rd = request.getRequestDispatcher("/index.html");
         rd.forward((ServletRequest)request, (ServletResponse)response);
        
    }
    
    private byte[] invokeEbPacFlow(final String tenantId, final String file_attachment_id, final String filePath) {
    	
        final String dwnloadFilePath = null;
        final HashMap inputMap = new HashMap();
        inputMap.put("file_attachment_id", file_attachment_id);
        WebserviceAttachmentInfo.tenantId = tenantId;
        byte[] tempFileContent = null;
        try {
        
        	final String port = PropertyUtility.getPropertyValues("Port", this.getClass().getClassLoader());
            final String ipAddress = PropertyUtility.getPropertyValues("IPAddress", this.getClass().getClassLoader());
            final long iTimeOut = Long.parseLong(PropertyUtility.getPropertyValues("TimeOut", this.getClass().getClassLoader()));
            
            System.out.println("CopyFileServlet --> Before calling the executeWorkFlow");
            final HashMap outputMap = WebserviceUtility.executeWorkFlow("selectFileContent", ipAddress, port, inputMap);
            
            System.out.println("CopyFileServlet --> After calling the executeWorkFlow");
            System.out.println("CopyFileServlet --> outputMap=====" + outputMap);
            
            if (outputMap != null) {
            	
                final String ja = (String)outputMap.get("contentArr");
                final Gson gson = new GsonBuilder().create();
                final JsonArray je = getJsonValue(ja, gson).getAsJsonArray();
                final JsonObject jo = je.get(0).getAsJsonObject();
                final String blobContent = jo.get("gocloud_binary_fileContent").getAsString();
                final String fileName = jo.get("FILE_NAME").getAsString();
                tempFileContent = Base64.decode(blobContent);
                
            } 
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return tempFileContent;
    }
    
    private static JsonElement getJsonValue(final String inStr, final Gson gson) {
        final JsonParser jp = new JsonParser();
        final JsonElement je = jp.parse(inStr);
        return je;
    }
 
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
}
