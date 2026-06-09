package com.bct.cuetrans;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;

public class XLSParsing {
 
	public XLSParsing() {
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("deprecation") 
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			FileInputStream excelFile = new FileInputStream(new File("D:\\Move\\sample.xls"));
			HSSFWorkbook wb = new HSSFWorkbook(excelFile);
			wb.setMissingCellPolicy(MissingCellPolicy.RETURN_BLANK_AS_NULL);
			for (int k = 0; k < wb.getNumberOfSheets(); k++) {
                HSSFSheet sheet = wb.getSheetAt(k);
                int rows = sheet.getPhysicalNumberOfRows();
                System.out.println("Sheet " + k + " \"" + wb.getSheetName(k) + "\" has " + rows + " row(s).");
                for (int r = 0; r < rows; r++) {
                    HSSFRow row = sheet.getRow(r);
                    if (row == null) {
                        continue;
                    }

                    System.out.println("\nROW " + row.getRowNum() + " has " + row.getPhysicalNumberOfCells() + " cell(s).");
                    for (int c = 0; c < row.getLastCellNum(); c++) {
                        HSSFCell cell = row.getCell(c,MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        String value;

                        if (cell != null) {
                        	value = cell.getStringCellValue();
                            System.out.println("CELL col=" + cell.getColumnIndex() + " VALUE=" + value);
                        }
                    }
                }
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
