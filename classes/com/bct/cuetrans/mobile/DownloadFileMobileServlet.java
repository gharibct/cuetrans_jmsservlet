package com.bct.cuetrans.mobile;

import com.bpms.core.webservice.WebserviceUtility;
import com.cuetrans.utils.PropertyUtility;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;

@WebServlet({"/DownloadFileMobileServlet"})
public class DownloadFileMobileServlet extends HttpServlet
{
  private static final long serialVersionUID = 1L;
  JSONObject responseData = new JSONObject();

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    System.out.println("First");
    String port = PropertyUtility.getPropertyValues("Port", getClass().getClassLoader());
    String ipAddress = PropertyUtility.getPropertyValues("IPAddress", getClass().getClassLoader());
    InputStream is = request.getInputStream();
    String req_json = "";
    try {
      System.out.println("just before reading....");
      req_json = new String(read(is));
    } catch (Exception exp) {
      throw new ServletException("Could not read the http request.", exp);
    }
    System.out.println("Do post");

    System.out.println("DownLoadFileMobileServlet >> doGet() >> Request from Mobile >> \n" + req_json);

    JsonParser parser = new JsonParser();
    JsonObject json = (JsonObject)parser.parse(req_json);
    JSONObject responseJson = new JSONObject();
    JsonArray jsonArray = json.getAsJsonArray("mobile_image_reference_ids");

    for (int i = 0; i < jsonArray.size(); i++) {
      HashMap outputMap = null;
      String file_attachment_id = jsonArray.get(i).toString();
      HashMap inputMap = new HashMap();
      inputMap.put("file_attachment_id", file_attachment_id);
      outputMap = WebserviceUtility.executeWorkFlow("selectFileContent", ipAddress, port, inputMap);
      if (outputMap != null) {
        String ja = (String)outputMap.get("contentArr");
        Gson gson = new GsonBuilder().create();
        JsonArray je = getJsonValue(ja, gson).getAsJsonArray();
        JsonObject jo = je.get(0).getAsJsonObject();
        String blobContent = jo.get("gocloud_binary_fileContent").getAsString();
        responseJson.put(file_attachment_id, blobContent);
        this.responseData.put("Success", "File Uploaded Successfully");
      }
    }
    try {
      this.responseData.put("mobile_image_reference_ids", responseJson);
      response.getWriter().write(this.responseData.toString());
    } catch (IOException e) {
      e.printStackTrace();
      this.responseData.put("Failure", "Failed, cannot be downloaded the file.");
    }
  }

  private static JsonElement getJsonValue(String inStr, Gson gson) {
    JsonParser jp = new JsonParser();
    JsonElement je = jp.parse(inStr);
    return je;
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    doGet(request, response);
  }

  private byte[] read(InputStream is) throws Exception {
    int buffer_len = 1024;
    byte[] buffer = new byte[buffer_len];
    int read_len = -1;
    int off = 0;
    int len = buffer.length;
    List<byte[]> byte_arr = new ArrayList();

    while ((read_len = is.read(buffer, off, len)) != -1) {
      if (read_len + off >= buffer_len) {
        byte_arr.add(buffer);
        off = 0;
        buffer = new byte[buffer_len];
        len = buffer_len;
      }
      else
      {
        off += read_len;
        len -= off;
      }
    }
    int total_len = byte_arr.size() * buffer_len + off;
    if (total_len < 0) {
      throw new Exception("Nothing in the stream");
    }
    byte[] total_data = new byte[total_len];
    int offset = 0;
    for (byte[] b : byte_arr)
    {
      System.arraycopy(b, 0, total_data, offset, b.length);
      offset += b.length;
    }
    System.arraycopy(buffer, 0, total_data, offset, off);
    return total_data;
  }
}