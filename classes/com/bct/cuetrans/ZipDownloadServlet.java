package com.bct.cuetrans;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest; 
import com.cuetrans.utils.PropertyUtility;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServlet;

public class ZipDownloadServlet extends HttpServlet
{  
    private static final long serialVersionUID = 1L;
    
    private static   String  SOURCE_FOLDER;
    private static   String  OUTPUT_ZIP_FILE;
    
    
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
    {
    	try
    	{
        System.out.println("ZipDownloadServlet --> Start"); 
        final String entityName = request.getParameter("entityName"); 
        final String fName = request.getParameter("fileName");
        final String isServerStore  = request.getParameter("isServerStore"); 
        final String strStoreageTransNo = request.getParameter("strStoreageTransNo");
        
        final String fileName = fName ;   
        final String uploadfilePath = request.getParameter("filePath");  
        
        System.out.println("ZipDownloadServlet --> fileName " + fileName);
        System.out.println("ZipDownloadServlet <--> isServerStore " + isServerStore); 
        System.out.println("ZipDownloadServlet --> strStoreageTransNo " + strStoreageTransNo);
     
        /** This block used to store the download zip files in the server */
        /*START*/
       if(isServerStore != null && isServerStore.equalsIgnoreCase("Y")) 
       {
    	   try
    	   {
	    	    String  documentFilePath = PropertyUtility.getPropertyValues("ServerFileUploadPath", this.getClass().getClassLoader()); 
	         
		   		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("ddMMyyyy");   
				LocalDateTime currentTime = LocalDateTime.now(); 
				String folder = dateFormat.format(currentTime);
				
				SOURCE_FOLDER = documentFilePath+"//"+strStoreageTransNo;
				OUTPUT_ZIP_FILE = documentFilePath+"//"+ strStoreageTransNo +".zip";

	    	    System.out.println("ZipDownloadServlet -->OUTPUT_ZIP_FILE->>" +OUTPUT_ZIP_FILE );
	    	    System.out.println("ZipDownloadServlet -->SOURCE_FOLDER->>" +SOURCE_FOLDER );
	    	   			
				ZipFolder appZip = new ZipFolder(OUTPUT_ZIP_FILE ,SOURCE_FOLDER);
				appZip.generateFileList(new File(SOURCE_FOLDER));
				appZip.zipIt(OUTPUT_ZIP_FILE);
				
	            System.out.println("ZipDownloadServlet -->Folder successfully compressed");

           } catch (IOException ex) {
               ex.printStackTrace(); 
           } finally {
                
           }
    	
    	 
         byte[] tempFileContent = null ;
         
          File file = new File (OUTPUT_ZIP_FILE);
    	    
           try (FileInputStream fileInput = new FileInputStream(file)) {
        		     byte dataBuffer[] = new byte[(int)file.length()];
        		     fileInput.read(dataBuffer);
        		     fileInput.close();
        		     tempFileContent =  dataBuffer; 
        		 } catch (IOException e) {  
        		     e.printStackTrace(); 
        		 } 
             
	       response.setContentType("application/octet-stream");
	       response.setHeader("Content-Disposition", "attachment; filename=\""+ OUTPUT_ZIP_FILE);
	      
	       final ServletOutputStream os = response.getOutputStream(); 
	       os.write(tempFileContent);
	       os.flush();  
	       os.close();   
	       
	       if(file.delete())
    	   {
    		   System.out.println("ZipDownloadServlet- >File Removed :" + fileName); 
    	   }
    	   else
    	   { 
    		   System.out.println("ZipDownloadServlet- >File not available");  
    	   }
	       
        System.out.println("ZipDownloadServlet -->File Zip at client successfully"); 
        
        final RequestDispatcher rd = request.getRequestDispatcher("/index.html");
        rd.forward((ServletRequest)request, (ServletResponse)response);
      }
    }
    	catch (Exception e)
    	{ 
    		e.printStackTrace();
    	}
        
    }
 
    
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
    {
    	System.out.println("ZipDownloadServlet1"); 
        this.doGet(request, response);
    }
}
