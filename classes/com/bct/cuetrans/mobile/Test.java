package com.bct.cuetrans.mobile;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.Gson;
import com.bct.bpms.encoder.Base64; 
//import com.bct.cuetrans.CopyFolder;
import com.bct.cuetrans.FileManager;
import com.google.gson.GsonBuilder;
import com.bpms.core.util.expressionbuilder.ParseException;
import com.bpms.core.webservice.WebserviceUtility;
import com.bpms.engine.util.WebserviceAttachmentInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

public class Test   
{
	
	private static   String  SOURCE_FOLDER;
    private static   String  OUTPUT_FILE;
    private static List<String> fileList; 
    
	
	 public Test(final String outputoutFile, final String sourceFolder) {
		 Test.OUTPUT_FILE = outputoutFile;
		 Test.SOURCE_FOLDER = sourceFolder;
	        this.fileList = new ArrayList<String>();
	    }
	    
	 
  public static void main (String [] args) throws Exception 
  {
	  
	   /** SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-mm-dd");
        java.util.Date udob = sdf1.parse("2023-07-05");
        
        System.out.print(udob);*/
	  SimpleDateFormat formatter = new SimpleDateFormat("MMddyyyyHHmmss");  //("yyyy/MM/dd");
	  String dateInString = "12072018130200";
	  try
	  {
	  Date date = formatter.parse(dateInString);
	  
	  SimpleDateFormat formatter1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	  
	  System.out.println(formatter1.format(date));
	  }catch (Exception e) { 
	  e.printStackTrace();
	  }
	    /**DateTimeFormatter dtf 	= DateTimeFormatter.ofPattern("yyyy/MM/dd/ HH24:mm:ss");  
		LocalDateTime now 	  	= LocalDateTime.now();  
		String systemDateTime   = now.toString();
		System.out.println(now);
	    System.out.println(new Date((replaceAll(Arrays.asList('T', '.'), now.toString())).substring(0,19))); */
	  
	  /**String str = "/home/ec2-user/Web/apache-tomcat-8.5.53/webapps/Inventory/Reports/StockInwardReport.jasper";
	  
	  final int dot = str.lastIndexOf(46);
	  
      
      final int sep = str.lastIndexOf("/");
      System.out.println( str.substring(sep + 1, dot)); */
      
	  
	  
	  /**Test test = new Test ("D:\\Thiyagarajan\\DR-8003-002981" , "D:\\Thiyagarajan\\DR-8003-00298\\") ; 
	  
	   test.generateFileList(new File(SOURCE_FOLDER));
	   final byte[] buffer = new byte[1024];
       final String source = new File(Test.SOURCE_FOLDER).getName();
    //   FileOutputStream fos = null;
       FileOutputStream fWriter = null;
      
       try { 
          // fos = new FileOutputStream(OUTPUT_FILE);
          
           System.out.println("CopyFolder --> Output  : " + OUTPUT_FILE); 
           FileInputStream in = null;
           
           File theDir = new File("D:\\Thiyagarajan\\DR-8003-002981");
           if (!theDir.exists()){
               theDir.mkdir();
           } 
      
           
           
           for (final String file : fileList) { 
               System.out.println("CopyFolder --> File Added : " + file); 
             
            
               try {
            	   
            	     
                   fWriter = new FileOutputStream("D:\\Thiyagarajan\\DR-8003-002981\\"+file);

                   in = new FileInputStream(String.valueOf(Test.SOURCE_FOLDER) + File.separator + file); 
                   int len;
                   while ((len = in.read(buffer)) > 0) {
                	   fWriter.write(buffer, 0, len);
                   } 
               } 
               finally {
                   in.close();
                   
                   fWriter.close();  
               }
               in.close();
               
           }
           
           System.out.println("CopyFolder --> Folder successfully compressed");
       }
       catch (IOException ex) {
           ex.printStackTrace();
           
           
           return;
       }
       finally {
           
       } 
        */
	  
  }
  
  public static String replaceAll(List<Character> list, String inputString)
	{
		String outputString = "";
		
		try
		{
			for (int i = 0; i < inputString.length(); i++) {
		        Character c = new Character(inputString.charAt(i));
		        if (!list.contains(c))
		        	outputString += c;
		        else
		        	outputString += " ";
		    }
			
		}
		catch(Exception ex)
		{
			System.out.print("Exception occurred in ........CRISUtilities");
			ex.printStackTrace();
		}
		
		return outputString;

	}
	
  
  
  public void generateFileList(final File node) {
      if (node.isFile()) {
          this.fileList.add(this.generateZipEntry(node.toString()));
      }
      if (node.isDirectory()) {
          final String[] subNote = node.list();
          String[] array;
          for (int length = (array = subNote).length, i = 0; i < length; ++i) {
              final String filename = array[i];
              this.generateFileList(new File(node, filename));
          } 
      }
  }
  
  private String generateZipEntry(final String file) {
      return file.substring(Test.SOURCE_FOLDER.length(), file.length());
  }
  
}
