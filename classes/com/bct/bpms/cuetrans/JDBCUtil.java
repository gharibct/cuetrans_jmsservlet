package com.bct.bpms.cuetrans;

import java.io.IOException;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class JDBCUtil {
  public static String toXML(ResultSet rs, String root, String rowElement) throws SQLException {
    ResultSetMetaData rsmd = rs.getMetaData();
    int colCount = rsmd.getColumnCount();
    StringBuffer xml = new StringBuffer();
    xml.append("<" + root + ">");
    while (rs.next()) {
      xml.append("<" + rowElement + ">");
      for (int i = 1; i <= colCount; i++) {
        String columnName = rsmd.getColumnName(i);
        Object value = rs.getObject(i);
        xml.append("<" + columnName + ">");
        if (value != null)
          xml.append(value.toString().trim()); 
        xml.append("</" + columnName + ">");
      } 
      xml.append("</" + rowElement + ">");
    } 
    xml.append("</" + root + ">");
    return xml.toString();
  }
  
  public static Document toDocument(ResultSet rs, String xmlroot, String rowelement) throws ParserConfigurationException, SQLException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();
    Element results = doc.createElement(xmlroot);
    doc.appendChild(results);
    ResultSetMetaData rsmd = rs.getMetaData();
    int colCount = rsmd.getColumnCount();
    while (rs.next()) {
      Element row = doc.createElement(rowelement);
      results.appendChild(row);
      for (int i = 1; i <= colCount; i++) {
        String columnName = rsmd.getColumnName(i);
        Object value = rs.getObject(i);
        Element node = doc.createElement(columnName);
        if (value != null)
          node.appendChild(doc.createTextNode(value.toString())); 
        row.appendChild(node);
      } 
    } 
    return doc;
  }
  
  public static Document toDoc(ResultSet rs) throws SQLException, FactoryConfigurationError, ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    String xml = toXML(rs, "root", "row");
    StringReader reader = new StringReader(xml);
    InputSource source = new InputSource(reader);
    return builder.parse(source);
  }
}
