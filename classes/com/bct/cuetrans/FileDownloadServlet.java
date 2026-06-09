package com.bct.cuetrans;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class FileDownloadServlet extends HttpServlet
{
  private static final long serialVersionUID = 1L;

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    try
    {
      HttpSession session = request.getSession(false);
      String UID = request.getParameter("UID");
      String filePath = session.getAttribute(UID).toString();
      System.out.println("file path" + filePath);

      File downloadFile = new File(filePath);
      FileInputStream inStream = new FileInputStream(downloadFile);

      ServletContext context = request.getSession().getServletContext();

      String mimeType = context.getMimeType(filePath);
      if (mimeType == null)
      {
        mimeType = "application/octet-stream";
      }
      System.out.println("MIME type: " + mimeType);

      response.setContentType(mimeType);
      response.setContentLength((int)downloadFile.length());

      String headerKey = "Content-Disposition";
      String headerValue = String.format("attachment; filename=\"%s\"", new Object[] { downloadFile.getName() });
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