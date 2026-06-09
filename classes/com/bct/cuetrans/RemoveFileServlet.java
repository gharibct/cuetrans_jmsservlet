package com.bct.cuetrans;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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

public class RemoveFileServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        
    	System.out.println("RemoveFileServlet- > Start" );
    	
        final String entityName = request.getParameter("entityName");
        final String fName = request.getParameter("fileName");
        final String isServerStore  = request.getParameter("isServerStore");
        final String strStoreageTransNo = request.getParameter("strStoreageTransNo");
        
        final String fileName = fName ; 
        
        System.out.println("RemoveFileServlet- > entity name:" + entityName);
        System.out.println("RemoveFileServlet- >fName:" + fName);
        System.out.println("RemoveFileServlet- >isServerStore:" + isServerStore);
        System.out.println("RemoveFileServlet- >strStoreageTransNo:" + strStoreageTransNo);
        
        byte[] tempFileContent = null ; 
        
       if(isServerStore != null && isServerStore.equalsIgnoreCase("Y")) 
       {
    	   String  documentFilePath = PropertyUtility.getPropertyValues("ServerFileUploadPath", this.getClass().getClassLoader()); 
    	   
    	   File file = new File (documentFilePath+"//"+strStoreageTransNo+"//"+fName);
    	   
           System.out.println("RemoveFileServlet- >Download File Path <====>>>" +documentFilePath+"\\"+strStoreageTransNo+"\\"+fName + "<isServerStore>"+isServerStore );
           
           try  
           {
        	   if(file.delete())
        	   {
        		   System.out.println("RemoveFileServlet- >File Removed :" + fileName); 
        	   }
        	   else
        	   {
        		   System.out.println("RemoveFileServlet- >File not available"); 
        	   }
           }
           catch (Exception e)
           { 
        	    e.printStackTrace();
           }
       }
       
	   System.out.println("RemoveFileServlet- >fileName:" + fileName);
	   
	   System.out.println("RemoveFileServlet- >File Removed  at client successfully");
	   
	   final RequestDispatcher rd = request.getRequestDispatcher("/index.html");
	   rd.forward((ServletRequest)request, (ServletResponse)response);
			
    }
    
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
}
