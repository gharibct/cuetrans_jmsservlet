package com.cuetrans;

import com.bpms.core.properties.ResourcePropertyManager;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

public class DbReportFill
{
  Connection connection; 
 
  JasperPrint jasPrint;

  public String generateReport(String InputFileName, String OutFileName, String guid, String conn, String tenantId)
  {
    HashMap tmp = new HashMap();
    tmp.put("guid", guid);
    String user = ResourcePropertyManager.replaceTenantProperty(tenantId, "@{user}");
    String pwd = ResourcePropertyManager.replaceTenantProperty(tenantId, "@{password}");
    String oracleURL = ResourcePropertyManager.replaceTenantProperty(tenantId, "@{conn}");    
    try
    {
      Class.forName("oracle.jdbc.driver.OracleDriver");
      this.connection = DriverManager.getConnection(oracleURL, user, pwd);
      this.connection.setAutoCommit(false);      
      
      System.out.println("connection" + this.connection);
      System.out.println("Filling report...");

      this.jasPrint = JasperFillManager.fillReport(InputFileName, tmp, this.connection);
      System.out.println("Done!");

      System.out.println("ReportGenerated");
      this.connection.close();

      JasperExportManager.exportReportToPdfFile(this.jasPrint, OutFileName);
    }
    catch (JRException e)
    {
      e.printStackTrace();
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    System.out.println(OutFileName);
    return OutFileName;
  }
}