package com.bct.cuetrans.mobile;
 
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


import net.sf.json.JSONNull;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;  
 
public class NullTest 
{
    public static void main(final String[] args) { 
    	 
    	//System.out.println(Math.exp(5));
                  
    	//String s = "Le totem et les informations y figurant étaient-ils en bon état et propres?";
    	
    	
    	JSONObject inputObject = JSONObject.fromObject("{\"strChkListCode\":null,\"strChkListName\":\"check\",\"strStatus\":null}");
    	
    	System.out.println(">"+JSONToHashMap(inputObject));
    	//System.out.println(s);
    	
    	String uploadfilePath = "LOCAL";
    	
    	final File file = new File(uploadfilePath);
    	
    	
    	//System.out.println("Aboslute->"+file.getAbsolutePath());
    	//final byte fileBytes =(byte) o; 
    	
       /* try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            final String userName = "fueltrans_app";
            final String password = "fueltrans_app";
            final String url = "jdbc:oracle:thin:@20.54.81.60:1521:mobappdb";
            System.out.println("Printing Parameters ...............");
            System.out.println("URL : " + url);
            System.out.println("User Name : " + userName);
            System.out.println("Password : " + password);
            
            final  Connection con  = DriverManager.getConnection(url, userName, password);  
            
            while (true) {
                
                System.out.println("SUCCESS1 ...");
                final Statement stmt = con.creaNullTestatement();
                final ResultSet rs = stmt.executeQuery("select 'x' from dual");
                while (rs.next()) { 
                    System.out.println(rs.getString(1));
                }
               
                Thread.sleep(300000L);
                System.out.println(new Date());
            }
           // con.close();
              
            
        }
        catch (Exception e) {
               
            e.printStackTrace();
            System.out.println(e);
        }
        finally  
        { 
               /*try
               {
                   con.close();
               }  
               catch (Exception e)
               {
                               
               }*/
    	
    	System.out.println(NullTest.getUUID());
        }
    
    private static String getUUID()
        {
          String iUID = UUID.randomUUID().toString();
          return iUID;
        }
    
    
    private static Map JSONToHashMap(final JSONObject input) {
        final Map result = new HashMap();
        final Set keySet = input.keySet();
       
        for (final Object key :  keySet) {
            final Object value =   input.get(key);
            
            if ( value instanceof JSONObject && ((JSONObject)value).isNullObject())
            {
            	result.put(key, null);
            }
            else if (value instanceof JSONObject ) {
                result.put(key, JSONToHashMap((JSONObject)value));
            }
            else if (value instanceof JSONArray) {
                result.put(key, JSONArrayToArrayList((JSONArray)value));
            }
            else {
                result.put(key, value);
            }
            
            
        }
        return result;
    }
    
    
    private static ArrayList<Object> JSONArrayToArrayList(final JSONArray jsonArray) {
        final ArrayList<Object> arrayList = new ArrayList<Object>();
        for (int index = 0; index < jsonArray.size(); ++index) {
            final Object value = jsonArray.get(index);
            if (value instanceof JSONObject) {
                arrayList.add(JSONToHashMap((JSONObject)value));
            }
            else if (value instanceof JSONArray) {
                arrayList.add(JSONArrayToArrayList((JSONArray)value));
            }
            else {
                arrayList.add(value);
            }
        }
        return arrayList;
    }
    
}
