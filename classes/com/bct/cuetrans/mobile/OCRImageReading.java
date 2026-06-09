package com.bct.cuetrans.mobile;

import java.io.File;
import net.sourceforge.tess4j.*;

public class OCRImageReading   
{
	 public static void main (String [] args) throws Exception 
	  {
		 // creating an object of class Tesseract  
	        Tesseract tesseract = new Tesseract( ) ;  
	        try {  
	            // this includes the path of tessdata inside the extracted folder  
	            tesseract.setDatapath( "D:\\OCR\\Oomco" ) ;  
	            // specifying the image that has to be read  
	            String text = tesseract.doOCR( new File( "D:\\OCR\\InputFiles\\OomcoTripSlip.pdf" ) ) ;    
	            // printing the text corresponding to the image interpreted   
	            System.out.print( text ) ;   
	        }  
	        catch ( TesseractException e ) {  
	            e.printStackTrace( ) ;  
	        }  
	  }

} 