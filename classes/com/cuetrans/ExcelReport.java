package com.cuetrans;


import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;

import com.bpms.core.properties.ResourcePropertyManager;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ExcelReport {
	public static void csvToXLSX(String csvFileAddress, String xlsxFileAddress, String sheetName) {
	    try {
	        HSSFWorkbook workBook = new HSSFWorkbook();
	        HSSFSheet sheet = workBook.createSheet(sheetName);
	        HSSFCellStyle style = workBook.createCellStyle();
	        HSSFFont font = workBook.createFont();
	        font.setFontName(HSSFFont.FONT_ARIAL);
	        font.setFontHeightInPoints((short)10);
	        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	        style.setFont(font);                 
	        String currentLine=null;
	        int RowNum=0;
	        BufferedReader br = new BufferedReader(new FileReader(csvFileAddress));
	        while ((currentLine = br.readLine()) != null) {
	            String str[] = currentLine.split(",");
	            HSSFRow currentRow=sheet.createRow(RowNum);
	            for(int i=0;i<str.length;i++)
	            {
	                currentRow.createCell(i).setCellValue(str[i]);
	                if(RowNum==0)
	                {
	                	currentRow.getCell(i).setCellStyle(style);
	                }
	            }
	          
	            RowNum++;
	        }

	        FileOutputStream fileOutputStream =  new FileOutputStream(xlsxFileAddress);
	        workBook.write(fileOutputStream);
	        fileOutputStream.close();
	        System.out.println("Excel Report Done!");
	    } catch (Exception ex) {
	        System.out.println(ex.getMessage()+"Exception in CSV to XLS Conversion");
	    }
	}
	
	
	
}
