package com.cuetrans.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ReportDownloadServlet extends HttpServlet
{
  private static final long serialVersionUID = 1L;

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    try
    {
      HttpSession session = request.getSession(false);
      String filePath = session.getAttribute("FilePath").toString();

      System.out.println("file path" + filePath);

      URL url = new URL(filePath);
      InputStream inStream = url.openStream();

      String downloadFileNamePathTmp = session.getAttribute("OutFileName").toString();

      File downloadFileObj = new File(downloadFileNamePathTmp);
      String downloadFileName = downloadFileObj.getName().toUpperCase();

      System.out.println("Download File Name : " + downloadFileName);

      ServletContext context = request.getSession().getServletContext();

      String mimeType = "";

      System.out.println("Index of XLS: " + downloadFileName.indexOf(".XLS"));

      if (downloadFileName.indexOf(".XLS") >= 0)
      {
        mimeType = "APPLICATION/XLS";
      }
      else
      {
        mimeType = "APPLICATION/PDF";
      }
      System.out.println("MIME type: " + mimeType);

      response.setContentType(mimeType);

      String headerKey = "Content-Disposition";
      String headerValue = String.format("inline; filename=\"%s\"", new Object[] { downloadFileName });
      response.setHeader(headerKey, headerValue);

      OutputStream outStream = response.getOutputStream();

      byte[] buffer = new byte[4096];
      int bytesRead = -1;

      while ((bytesRead = inStream.read(buffer)) != -1) {
        outStream.write(buffer, 0, bytesRead);
      }

      inStream.close();
      outStream.close();
    }
    catch (FileNotFoundException localFileNotFoundException)
    {
    }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
  }
}