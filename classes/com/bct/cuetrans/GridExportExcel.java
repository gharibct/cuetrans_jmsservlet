package com.bct.cuetrans;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class GridExportExcel extends HttpServlet
{
  private static final long serialVersionUID = 1L;

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    OutputStream out = null;
    int cellvalue = 0;
    try
    {
      String workFlowName = request.getParameter("workFlowName");
      String workFlowParams = request.getParameter("workFlowParams");
      String processType = request.getParameter("processType");
      JSONObject inputObject = JSONObject.fromObject(workFlowParams);

      String headerCloumns = "";
      String header = "";
      String DataName = "";
      Date date = new Date(); 
      ArrayList<String> headerList = new ArrayList<String>();

      String grid_defn = inputObject.getString("grid_defn");
      String grid_content = inputObject.getString("gridid");
      System.out.println("ExportToExcel");

      if (grid_defn.contains("["))
        grid_defn = grid_defn.replace("[", "");
      if (grid_defn.contains("]")) {
        grid_defn = grid_defn.replace("]", "");
      }
      if (grid_content.contains("["))
        grid_content = grid_content.replace("[", "");
      if (grid_content.contains("]")) {
        grid_content = grid_content.replace("]", "");
      }
      System.out.println("Grid Defn: " + grid_defn);
      System.out.println("Grid Content: " + grid_content);

      String[] headerValue = grid_defn.split("},");
      String[] CloumnValue = grid_content.split("},");

      Map<Integer, ArrayList<String>> data = new TreeMap<Integer, ArrayList<String>>();
      ArrayList obj = new ArrayList();

      int i = 0;

      for (String hdVlaue : headerValue) {
        hdVlaue = hdVlaue + "}";
        inputObject = JSONObject.fromObject(hdVlaue);

        if ((!inputObject.containsKey("imageURL")))
        {
         if ((!inputObject.containsKey("xlsHidden")) || (inputObject.containsKey("xlsHidden") && !inputObject.getString("xlsHidden").equalsIgnoreCase("true")))
         {
	          headerCloumns = inputObject.getString("columnname");
	          obj.add(headerCloumns);
	          header = inputObject.getString("dataname"); 
	          headerList.add(header);
         }
        }
        i++;
      }

      System.out.println("Header List " + headerList.toString());

      data.put(Integer.valueOf(1), obj);
      ArrayList objValue = new ArrayList();
      i = 0;
      int j = 1;
      for (String clValue : CloumnValue) {
        clValue = clValue + "}";
        j++;
        inputObject = JSONObject.fromObject(clValue);
        for (String value : headerList) {
          if (inputObject.has(value))
          {
            DataName = inputObject.getString(value);
          }
          else
          {
            DataName = "";
          }
          objValue.add(DataName);
        }

        data.put(Integer.valueOf(j), objValue);
        objValue = new ArrayList();
      }

      String UID = new Timestamp(date.getTime()).toString();
      UID.replaceAll("\\s", "");

      HSSFWorkbook workbook = new HSSFWorkbook();

      Object sheet = workbook.createSheet("Export Data");

      Set<Integer> keyset = data.keySet();
      int rownum = cellvalue;
      HSSFCellStyle style = workbook.createCellStyle();
      HSSFFont font = workbook.createFont();
      font.setFontName(HSSFFont.FONT_ARIAL);
      font.setFontHeightInPoints((short)10);
  //    font.setBold(true);
      style.setFont(font);
      for (Integer key : keyset)
      {
        Row row = ((Sheet)sheet).createRow(rownum++);
        ArrayList<String> objArr = data.get(key);
        int cellnum = 0;
        for (String obj1 : objArr) 
        {
          Cell cell = row.createCell(cellnum++);

          cell.setCellValue(obj1);
        }

        if (rownum == 1) {
          for (int k = 0; k < cellnum; k++) {
            row.getCell(k).setCellStyle(style);
          }

        }

      }

      response.setContentType("application/vnd.ms-excel");
      response.setHeader("Content-Disposition", "attachment; filename=" + UID + ".xls");
      ServletOutputStream os = response.getOutputStream();
      workbook.write(os);
      os.flush();
      os.close();
      RequestDispatcher rd = request.getRequestDispatcher("/index.html");
      rd.forward(request, response);
    }
    catch (Exception e)
    {
      System.out.println(e.toString());
      System.out.println(e.getMessage());
      throw new ServletException("Exception in Excel Sample Servlet", e);
    }
    finally {
      if (out != null)
        out.close();
    }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    doGet(request, response);
  }

  private String getPropertyValues(String key) throws IOException
  {
    Properties prop = new Properties();
    String propFileName = "./config.properties";
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
    prop.load(inputStream);
    if (inputStream == null)
    {
      throw new FileNotFoundException("Property File" + propFileName + "Not Found");
    }
    String value = prop.getProperty(key);
    return value;
  }
}