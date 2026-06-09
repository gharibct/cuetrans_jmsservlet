package com.bct.bpms.cuetrans;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class GenerateFile {
	private Connection conn = null;
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("config");
	private static final String LS = System.getProperty("line.separator");
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		GenerateFile generateFile = new GenerateFile();

	}
	Logger logger = Logger.getLogger("GenerateFile"); 
	private FileHandler fh;

	  public GenerateFile() throws Exception {
	    try {
	      this.fh = new FileHandler("MyLogFile.log", true);
	      this.logger.addHandler(this.fh);
	      SimpleFormatter formatter = new SimpleFormatter();
	      this.fh.setFormatter(formatter);
	    }
	    catch (Exception e)
	    {
	      this.logger.info(e.getMessage());
	      e.printStackTrace();
	    }

	    GenerateXmlFile();
	  }
	  public void GenerateXmlFile() throws Exception
	  {
		FileOutputStream fos = null;
	    fos = new FileOutputStream(new File("SFTP_Log.txt"), true);
	    GenerateXML dao = null;
	    try {
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	  }
	  public void GenerateXML() throws Exception {
			
		    String url = getString("JDBCURL", "");
		    String userId = getString("DBUSERNAME", "");
		    String passWord = getString("DBPASSWORD", "");

		    try {
		    	Class.forName("oracle.jdbc.driver.OracleDriver");
		      this.conn = DriverManager.getConnection(url, userId, passWord);
		      this.conn.setAutoCommit(false);
		    } catch (SQLException e) {
		      e.printStackTrace();
		    } 
		  }
	  public static String getString(String key, String defVal) {
		    try {
		      return RESOURCE_BUNDLE.getString(key);
		    } catch (MissingResourceException missingResourceException) {
		      return defVal;
		    } 
		  }
		  
		  public static void log(FileOutputStream fos, String s) {
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		    String d = sdf.format(new Date());
		    try {
		      fos.write((String.valueOf(d) + ":  " + s + LS).getBytes());
		    } catch (IOException e) {
		      e.printStackTrace();
		    } 
		  }

}
