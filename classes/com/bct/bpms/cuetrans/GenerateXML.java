package com.bct.bpms.cuetrans;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.json.JSONObject;
import org.w3c.dom.Document;

public class GenerateXML {
  private Connection conn = null;
  
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("config");
  
  private static String PRIVATE_KEY_LOCATION = "";
  
  private static String USER = "";
  
  private static String HOST = "";
  
  private static int PORT = 0;
  
  private static final String LS = System.getProperty("line.separator");
  
  private static URLConnection httpConn;
 
  static {
	    try {
	      Class.forName(getString("pdodbdriver", ""));
	    } catch (Exception e) {
	      e.printStackTrace();
	    } 
	    disableSslVerification();
	  }
  private static void disableSslVerification() {
    try {
      TrustManager[] trustAllCerts = { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
              System.out.println("&&&&&&&&&inside accepted issuers&&&&&&");
              return null;
            }
            
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
              System.out.println("&&&&&&&&&check Client trusted &&&&&&");
            }
            
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
              System.out.println("&&&&&&&&&check Server trusted &&&&&&");
            }
          } };
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      HostnameVerifier allHostsValid = new HostnameVerifier() {
          public boolean verify(String hostname, SSLSession session) {
            System.out.println("*****host name***" + hostname);
            return true;
          }
        };
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
      System.out.println("--------inside ssl verify-----------");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (KeyManagementException e) {
      e.printStackTrace();
    } 
  }
  
  public GenerateXML() throws Exception {
	
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
  
  public void finalize() {
    try {
      this.conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    } 
  }
  
  public String getHdrList() throws Exception {
    Document doc = null;
    ResultSet rs = null;
    ResultSet rs1 = null;
    Statement stmt1 = null;
    Statement stmt2 = null;
    File hdrFile = null;
    File dtlFile = null;
    String hdrFilePath = null;
    String retString = null;
    try {
      String updateSqlHdr = "update T_PDO_CHLHDR set read_status=1 where read_status=0 or read_status is null";
      stmt1 = this.conn.createStatement();
      stmt2 = this.conn.createStatement();
      String sqlHdr = "select * from T_PDO_CHLHDR where read_status=0 or read_status is null";
      String sqlDtl = "select * from T_PDO_CHLITM where read_status=0 or read_status is null";
      String updateSqlDtl = "update T_PDO_CHLITM set read_status=1 where read_status=0 or read_status is null";
      rs = stmt1.executeQuery(sqlHdr);
      String xmlRoot = "results";
      String rowElement = "hdrrow";
      hdrFilePath = String.valueOf(getString("outputhdrfilepath", "")) + 
        System.currentTimeMillis() + ".xml";
      String dtlFilePath = String.valueOf(getString("outputhdrfilepath", "")) + "_itm" + 
        System.currentTimeMillis() + ".xml";
      boolean hdrFileGenerated = false;
      if (!rs.isBeforeFirst()) {
        retString = String.valueOf(ErrorConstants.NO_DATA_AVAILABLE.id);
        return retString;
      } 
      doc = JDBCUtil.toDocument(rs, xmlRoot, rowElement);
      retString = getDocToString(doc, "ISO-8859-1");
    } catch (Exception e) {
      throw e;
    } finally {
      try {
        if (rs != null)
          rs.close(); 
      } catch (SQLException e) {
        e.printStackTrace();
      } 
      try {
        if (stmt1 != null)
          stmt1.close(); 
      } catch (SQLException e) {
        e.printStackTrace();
      } 
    } 
    try {
      if (rs != null)
        rs.close(); 
    } catch (SQLException e) {
      e.printStackTrace();
    } 
    try {
      if (stmt1 != null)
        stmt1.close(); 
    } catch (SQLException e) {
      e.printStackTrace();
    } 
    return retString;
  }
  
  private String getDocToString(Document doc, String encoding) {
    String output = null;
    try {
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty("encoding", "ISO-8859-1");
      StringWriter writer = new StringWriter();
      transformer.transform(new DOMSource(doc), new StreamResult(writer));
      output = writer.getBuffer().toString().replaceAll("\n|\r", "");
    } catch (Exception e) {
      System.out.println("ERROR in getDocToString -- ");
      e.printStackTrace();
    } 
    return output;
  }
  
  public String getItemList() throws Exception {
    ResultSet rs = null;
    Statement stmt1 = null;
    String retString = null;
    try {
      stmt1 = this.conn.createStatement();
      String sqlDtl = "select * from T_PDO_CHLITM where read_status=0 or read_status is null";
      String updateSqlDtl = "update T_PDO_CHLITM set read_status=1 where read_status=0 or read_status is null";
      String xmlRoot = "Item";
      String rowElement = "row";
      rs = stmt1.executeQuery(sqlDtl);
      if (!rs.isBeforeFirst()) {
        retString = String.valueOf(ErrorConstants.NO_DATA_AVAILABLE.id);
        return retString;
      } 
      Document doc = JDBCUtil.toDocument(rs, xmlRoot, rowElement);
      retString = getDocToString(doc, "ISO-8859-1");
    } catch (Exception e) {
      throw e;
    } finally {
      try {
        if (rs != null)
          rs.close(); 
      } catch (SQLException e) {
        e.printStackTrace();
      } 
      try {
        if (stmt1 != null)
          stmt1.close(); 
      } catch (SQLException e) {
        e.printStackTrace();
      } 
    } 
    try {
      if (rs != null)
        rs.close(); 
    } catch (SQLException e) {
      e.printStackTrace();
    } 
    try {
      if (stmt1 != null)
        stmt1.close(); 
    } catch (SQLException e) {
      e.printStackTrace();
    } 
    return retString;
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
  
  public static void log(FileOutputStream fos, Throwable th) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    String d = sdf.format(new Date());
    String str = "null";
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(baos);
      th.printStackTrace(ps);
      ps.close();
      str = new String(baos.toByteArray());
    } catch (Exception exp) {
      exp.printStackTrace();
    } 
    try {
      fos.write((String.valueOf(d) + ":  " + LS + str + LS).getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public static URLConnection sendPostRequest(String requestURL, JSONObject reqParams) throws IOException {
    URL url = new URL(requestURL);
    String proxy_ip = getString("PROXY_IP", null);
    int proxy_port = Integer.parseInt(getString("PROXY_PORT", null));
    System.out.println("Proxy ip: " + proxy_ip + "; proxy_port: " + proxy_port);
    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy_ip, proxy_port));
    httpConn = url.openConnection(proxy);
    httpConn.setUseCaches(false);
    httpConn.setDoOutput(true);
    StringBuffer wfParams = new StringBuffer();
   /* OutputStreamWriter writer = new OutputStreamWriter(
        httpConn.getOutputStream());*/
    System.out.println("wfparams        " + wfParams);
    wfParams.append(URLEncoder.encode("workFlowName", "UTF-8"));
    wfParams.append("=").append(
        URLEncoder.encode("PdoHdrService", "UTF-8"));
    wfParams.append("&");
    wfParams.append(URLEncoder.encode("workFlowParams", "UTF-8"));
    wfParams.append("=").append(
        URLEncoder.encode(reqParams.toString(), "UTF-8"));
    System.out.println("wfparams        " + wfParams.toString());
    /*
    writer.write(wfParams.toString());
    writer.flush();
    */
    return httpConn;
  }
  
  public static String[] readMultipleLinesRespone() throws IOException {
    InputStream inputStream = null;
    if (httpConn != null) {
      inputStream = httpConn.getInputStream();
    } else {
      throw new IOException("Connection is not established.");
    } 
    BufferedReader reader = new BufferedReader(new InputStreamReader(
          inputStream));
    ArrayList<String> response = new ArrayList<String>();
    String line = "";
    while ((line = reader.readLine()) != null)
      response.add(line); 
    reader.close();
    return response.<String>toArray(new String[0]);
  }
  
  public static void main(String[] argv) throws Exception {
    FileOutputStream fos = null;
    fos = new FileOutputStream(new File("SFTP_Log.txt"), true);
    GenerateXML dao = null;
    try {
      dao = new GenerateXML();
      
      String hdrXmlString = dao.getHdrList();
      if (hdrXmlString.equals(
          String.valueOf(ErrorConstants.NO_DATA_AVAILABLE.id))) {
        dao.conn.rollback();
        log(fos, ErrorConstants.NO_DATA_AVAILABLE.message);
        System.exit(ErrorConstants.NO_DATA_AVAILABLE.id);
      } 
      String itemXmlString = dao.getItemList();
      if (itemXmlString.equals(
          String.valueOf(ErrorConstants.NO_DATA_AVAILABLE.id))) {
        dao.conn.rollback();
        log(fos, ErrorConstants.NO_DATA_AVAILABLE.message);
        System.exit(ErrorConstants.NO_DATA_AVAILABLE.id);
      } 
      String reqURL = getString("requestURL", "");
      String userId = getString("userId", "");
      String pwd = getString("password", "");
      JSONObject jsonObj = new JSONObject();
      jsonObj.put("strUserId", userId);
      jsonObj.put("strPassword", pwd);
      jsonObj.put("hdrXmlString", hdrXmlString);
      jsonObj.put("itemXmlString", itemXmlString);
      jsonObj.put("strTenantId", "cuecent_tenant");
      jsonObj.put("methodName", "PdoHdrService");
      System.out.println("hdrXMLString        " + hdrXmlString);
      System.out.println("ItemXMLString        " + itemXmlString);
      
      httpConn = sendPostRequest(reqURL, jsonObj);
      /*
      String[] response = readMultipleLinesRespone();
      byte b;
      int i;
      String[] arrayOfString1;
      for (i = (arrayOfString1 = response).length, b = 0; b < i; ) {
        String line = arrayOfString1[b];
        System.out.println(line);
        b++;
      }
      */
    } catch (Exception e) {
      log(fos, e);
      System.exit(1);
    } 
    dao.conn.commit();
    fos.close();
  }
}