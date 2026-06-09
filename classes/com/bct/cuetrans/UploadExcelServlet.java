package com.bct.cuetrans;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;


@WebServlet({"/UploadExcelServlet"})
public class UploadExcelServlet extends HttpServlet
{
  private static final long serialVersionUID = 1L;
  private ServletFileUpload uploader = null;
  JSONObject responseData = new JSONObject();
  ArrayList<HashMap<String, String>> GridData = new ArrayList();
  String filePath;
  String fileName;
  File file1;

  public void init()
    throws ServletException
  {
    DiskFileItemFactory fileFactory = new DiskFileItemFactory();
    File filesDir = (File)getServletContext().getAttribute(
      "FILES_DIR_FILE");
    fileFactory.setRepository(filesDir);
    this.uploader = new ServletFileUpload(fileFactory);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    if (!ServletFileUpload.isMultipartContent(request)) {
      throw new ServletException(
        "Content type is not multipart/form-data");
    }
    //request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");
    System.out.println("Excel UTF-81");
    
    PrintWriter out = response.getWriter();
    
    try
    {
    	
    	
      List fileItemsList = this.uploader.parseRequest(request);
      Iterator fileItemsIterator = fileItemsList.iterator();
      while (fileItemsIterator.hasNext())
      {
        FileItem fileItem = (FileItem)fileItemsIterator.next();
        System.out.println("FieldName=" + fileItem.getFieldName());
        System.out.println("FileName=" + fileItem.getName());
        System.out.println("ContentType=" + fileItem.getContentType());
        System.out.println("Size in bytes=" + fileItem.getSize());

        this.filePath = getPropertyValues("FileUploadPath");
        System.out.println("File Path :" + this.filePath);
        this.fileName = fileItem.getName();
        File file = new File(this.filePath);
        if (!file.exists())
        {
          try
          {
            file.mkdirs();

            this.file1 = new File(this.filePath + this.fileName);
            System.out.println("File" + this.file1);
            fileItem.write(this.file1);
          }
          catch (IOException e)
          {
            e.printStackTrace();
          }
        }
        else
        {
          this.file1 = new File(this.filePath + this.fileName);
          fileItem.write(this.file1);
        }
      }
    }
    catch (FileUploadException e)
    {
      this.responseData.put("Failure", "File Uploading Failed...");
    }
    catch (Exception e)
    {
      this.responseData.put("Failure", 
        "Error Occurred while uploading...");
    }
    try
    {
      this.GridData = importExcel(this.filePath, this.fileName);

      this.responseData.put("fileName", this.GridData);
      this.file1.delete();
      System.out.println("Grid data" + this.responseData.get("fileName"));
    }
    catch (Exception e)
    {
      this.responseData.put("Failure", e.getMessage());
    }
    try
    {
    	
      
      response.getWriter().write(this.responseData.toString());
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    doGet(request, response);
  }

  public ArrayList<HashMap<String, String>> importExcel(String filePath, String fileName)
  {
    ArrayList retData = new ArrayList();
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    String sourceFilePath = filePath + fileName;

    FileInputStream fileInputStream = null;

    List excelData = new ArrayList();
    List indexData = new ArrayList();
    try
    {
      fileInputStream = new FileInputStream(sourceFilePath);

      HSSFWorkbook excelWorkBook = new HSSFWorkbook(fileInputStream);
      
      excelWorkBook.setMissingCellPolicy(MissingCellPolicy.RETURN_BLANK_AS_NULL);
      
      List sheetNames = new ArrayList();
      for (int i = 0; i < excelWorkBook.getNumberOfSheets(); i++) {
        sheetNames.add(excelWorkBook.getSheetName(i));
      }
      HSSFSheet indexSheet = excelWorkBook.getSheetAt(0);
      HSSFSheet excelSheet = excelWorkBook.getSheetAt(1);
      HSSFRow startingRow = indexSheet.getRow(0);
      HSSFRow endingCloumn = indexSheet.getRow(1);
      Iterator startingCells = startingRow.cellIterator();
      Iterator endingCells = endingCloumn.cellIterator();
      List startRowNum = new ArrayList();
      List endCloNum = new ArrayList();
      while ((startingCells.hasNext()) && (endingCells.hasNext()))
      {
        HSSFCell headCell = (HSSFCell)startingCells.next();
        HSSFCell colTypeCell = (HSSFCell)endingCells.next();
        startRowNum.add(headCell);
        endCloNum.add(colTypeCell);
      }
      double startRowInt = Double.parseDouble(startRowNum.get(1).toString());
      double endCloInt = Double.parseDouble(endCloNum.get(1).toString());

      HSSFRow headerRow = excelSheet.getRow(0);
      HSSFRow colTypeRow = excelSheet.getRow(1);

      Iterator headerCells = headerRow.cellIterator();
      Iterator colTypeCells = colTypeRow.cellIterator();

      List headerCellData = new ArrayList();
      List colTypeData = new ArrayList();
      while ((headerCells.hasNext()) && (colTypeCells.hasNext()))
      {
        HSSFCell headCell = (HSSFCell)headerCells.next();
        HSSFCell colTypeCell = (HSSFCell)colTypeCells.next();
        headerCellData.add(headCell);
        colTypeData.add(colTypeCell.getRichStringCellValue().toString());
      }
      indexData.add(headerCellData);

      HSSFSheet sheet =  excelSheet;
      //Iterator rows = excelSheet.rowIterator();
      Integer cloNumInt = Integer.valueOf(0);
      
      int rowPos =1;
      int totalRows = excelSheet.getPhysicalNumberOfRows();
      
      while (rowPos < totalRows)
      {
    	  System.out.println(rowPos +"<>"+totalRows );
    	  
        HSSFRow row = (HSSFRow)excelSheet.getRow(rowPos) ;
        
        rowPos++;
        Integer getRowNum = Integer.valueOf(row.getRowNum());
        
        if (Double.parseDouble(getRowNum.toString()) < startRowInt)
          continue;
        
        List cellData = new ArrayList();
        
        int cellPos = 0 ; 
        
        while (cellPos < endCloInt )
        {
        	System.out.println(cellPos +"<>"+endCloInt );
        	
        	
          if (Double.parseDouble(cloNumInt.toString()) > endCloInt) {
            break;
          }
          
          HSSFCell cell =  row.getCell(cellPos,MissingCellPolicy.CREATE_NULL_AS_BLANK); 
          cellData.add(cell);
          cellPos++;
          
        }
        
        excelData.add(cellData);
        
        
      }

      ArrayList tmpKeys = new ArrayList();

      List firstRow = (List)indexData.get(0);
      for (int cellNum = 0; cellNum < firstRow.size(); cellNum++)
      {
        HSSFCell cell = (HSSFCell)firstRow.get(cellNum);

        int cellType = cell.getCellType();
        if (cellType == 0)
        {
          if (DateUtil.isCellDateFormatted(cell))
            tmpKeys.add(cellNum, Integer.valueOf(cell.getDateCellValue().getDate()));
          else {
            tmpKeys.add(cellNum, Double.valueOf(cell.getNumericCellValue()));
          }
        }
        else if (cellType == 1)
          tmpKeys.add(cellNum, cell.getStringCellValue().toString());
        else {
          tmpKeys.add(cellNum, Integer.valueOf(cell.getDateCellValue().getDate()));
        }
      }
      System.out.println("No of Rows>" + excelData.size());
      for (int rowNum = 0; rowNum < excelData.size(); rowNum++)
      {
        List list = (List)excelData.get(rowNum); 
        HashMap tmpData = new HashMap();

        for (int cellNum = 0; cellNum < list.size(); cellNum++)
        {
          HSSFCell cell = (HSSFCell)list.get(cellNum);
          String cellType = colTypeData.get(cellNum).toString();
          System.out.println("CellType" + cellType);
          if (cellType.equalsIgnoreCase("date"))
          {
            String dt = cell.getStringCellValue();
            if (checkformat(dt))
              tmpData.put(tmpKeys.get(cellNum).toString(), dt);
            else {
              tmpData.put("RowNumber" + rowNum + "CloumnNumber" + cellNum, "Date Format is invalid");
            }
          }
          else if (cellType.equalsIgnoreCase("Number"))
          {
            System.out.println("number type data");
            String nu = cell.getStringCellValue();
            if (isNumeric(nu))
              tmpData.put(tmpKeys.get(cellNum).toString(), nu);
            else {
              tmpData.put("RowNumber" + rowNum + "CloumnNumber" + cellNum, "Number Format is invalid");
            }
          }
          else if (cellType.equalsIgnoreCase("String"))
          {
            tmpData.put(tmpKeys.get(cellNum).toString(), cell
              .getStringCellValue().toString());
          }
          else
          {
            tmpData.put("RowNumber" + rowNum + "CloumnNumber" + cellNum, "Format is invalid");
          }
        }
        tmpData.put("FileName", fileName);
        retData.add(rowNum, tmpData);
        System.out.println("retData:" + retData.get(rowNum));
      }
    }
    catch (IOException e) {
        e.printStackTrace();
        retData = null;
        if (fileInputStream != null) {
          try {
            fileInputStream.close();
          } catch (IOException e1) {
            e1.printStackTrace();
          }

        }

        if (fileInputStream != null) {
          try {
            fileInputStream.close();
          } catch (IOException e1) {
            e1.printStackTrace();
          }

        }

        if (fileInputStream != null)
          try {
            fileInputStream.close();
          } catch (IOException e1) {
            e1.printStackTrace();
          }
      }
      catch (Exception e2)
      {
        e2.printStackTrace();
        retData = null;
        if (fileInputStream != null) {
          try {
            fileInputStream.close();
          } catch (IOException e) {
            e.printStackTrace();
          }

        }

        if (fileInputStream != null)
          try {
            fileInputStream.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
      }
      finally
      {
        if (fileInputStream != null) {
          try {
            fileInputStream.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
      return retData;
    }

  private String getPropertyValues(String key)
    throws IOException
  {
    Properties prop = new Properties();
    String propFileName = "./config.properties";
    InputStream inputStream = getClass().getClassLoader()
      .getResourceAsStream(propFileName);
    prop.load(inputStream);
    if (inputStream == null) {
      throw new FileNotFoundException("Property File" + propFileName + 
        "Not Found");
    }
    String value = prop.getProperty(key);
    return value;
  }

  public boolean isNumeric(String str)
  {
    try
    {
    	double d = Double.parseDouble(str);
    }
    catch (NumberFormatException nfe)
    {
      double d;
      return false;
    }
    return true;
  }

  public boolean checkformat(String str)
  {
    boolean checkformat = true;
    if (str.matches("([0-9]{2})-([0-9]{2})-([0-9]{4})"))
      checkformat = true;
    else {
      checkformat = false;
    }
    return checkformat;
  }
}