package com.cuetrans.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import sun.jdbc.rowset.CachedRowSet;

public class HashMapJSONParser
{
  public static Map convertJSONObjectToHashMap(JSONObject jsonObject)
  {
    Map map = new HashMap();
    map = JSONToHashMap(jsonObject);
    return map;
  }

  public static JSONObject convertHashMapToJSONObject(Map map) throws IOException
  {
    JSONObject jsonObject = new JSONObject();
    jsonObject = HashMapToJsonObject(map);
    return jsonObject;
  }

  private static JSONArray CachedRowSetToJsonAray(CachedRowSet rowSet) throws IOException
  {
    JSONArray array = new JSONArray();
    try
    {
      rowSet.beforeFirst();
      ResultSetMetaData metaData = rowSet.getMetaData();

      int rowSetSize = rowSet.size();
      int columnCount = metaData.getColumnCount();
      List columnNames = new ArrayList();
      
      List columnTypes = new ArrayList();
      
      for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
        columnNames.add(metaData.getColumnLabel(columnIndex));
        columnTypes.add(metaData.getColumnType(columnIndex));
      }

      Clob clobj ;
      
      for (int rowIndex = 0; rowIndex < rowSetSize; rowIndex++) {
        rowSet.next();
        JSONObject record = new JSONObject();
        for (int colNameIndex = 0; colNameIndex < columnNames.size(); colNameIndex++) {
          String columnName = (String)columnNames.get(colNameIndex); 
          
          System.out.println("Grid Column Types <->"+ (Integer) columnTypes.get(colNameIndex));
          if ( (Integer) columnTypes.get(colNameIndex) == Types.CLOB )
          {
        	   clobj = (Clob)rowSet.getClob(columnName);
        	   record.accumulate(columnName, clobj.getSubString(0, (int) clobj.length()-1));   
        	  
          }
          else
          {
           record.accumulate(columnName, rowSet.getObject(columnName));
          }
        }  
        array.add(record); 
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return array;
  }

  private static JSONArray ArrayListToJsonArray(List list) throws IOException {
    JSONArray array = new JSONArray();
    for (int index = 0; index < list.size(); index++) {
      Object value = list.get(index);
      if ((value instanceof CachedRowSet))
        array.add(CachedRowSetToJsonAray((CachedRowSet)value));
      else if ((value instanceof ArrayList))
        array.add(ArrayListToJsonArray((List)value));
      else if ((value instanceof Map))
        array.add(HashMapToJsonObject((Map)value));
      else {
        array.add(value.toString());
      }
    }
    return array;
  }

  private static JSONObject HashMapToJsonObject(Map<Object, Object> map) throws IOException {
    JSONObject jsonObject = new JSONObject();
    Iterator iterator = map.keySet().iterator();
    ArrayList mapKeys = new ArrayList();
    while (iterator.hasNext()) {
      mapKeys.add((String)iterator.next());
    }
    for (int keyIndex = 0; keyIndex < mapKeys.size(); keyIndex++) {
      String key = (String)mapKeys.get(keyIndex);
      Object value = map.get(key);

      if ((value instanceof CachedRowSet))
        jsonObject.accumulate(key, CachedRowSetToJsonAray((CachedRowSet)value));
      else if ((value instanceof ArrayList))
        jsonObject.accumulate(key, ArrayListToJsonArray((List)value));
      else if ((value instanceof HashMap))
        jsonObject.accumulate(key, HashMapToJsonObject((Map)value));
      else {
        jsonObject.accumulate(key, value);
      }
    }
    return jsonObject;
  }

  private static Map JSONToHashMap(JSONObject input)
  {
    Map result = new HashMap();
    Set keySet = input.keySet();

    Iterator iterator = keySet.iterator();
    while (iterator.hasNext()) {
      String key = (String)iterator.next();
      Object value = input.get(key);
      if ((value instanceof JSONObject))
        result.put(key, JSONToHashMap((JSONObject)value));
      else if ((value instanceof JSONArray))
        result.put(key, JSONArrayToArrayList((JSONArray)value));
      else {
        result.put(key, value);
      }
    }
    return result;
  }

  private static ArrayList<Object> JSONArrayToArrayList(JSONArray jsonArray) {
    ArrayList arrayList = new ArrayList();
    for (int index = 0; index < jsonArray.size(); index++) {
      Object value = jsonArray.get(index);
      if ((value instanceof JSONObject))
        arrayList.add(JSONToHashMap((JSONObject)value));
      else if ((value instanceof JSONArray))
        arrayList.add(JSONArrayToArrayList((JSONArray)value));
      else {
        arrayList.add(value);
      }
    }
    return arrayList;
  }

  public static void changeNullStringsToNull(JsonElement je) {
    if ((je instanceof JsonObject)) {
      changeNullStringsToNull((JsonObject)je);
    } else if ((je instanceof JsonArray)) {
      JsonArray ja = (JsonArray)je;
      int size = ja.size();
      for (int i = 0; i < size; i++)
        changeNullStringsToNull(ja.get(i));
    }
  }

  private static void changeNullStringsToNull(JsonObject jo)
  {
    JsonElement je = null;
    ArrayList<String> nullMembers = new ArrayList<String>();
    for (Map.Entry entry : jo.entrySet()) {
      je = (JsonElement)entry.getValue();
      if (je.isJsonPrimitive()) {
        if ((je.isJsonNull()) || 
          (!"null".equalsIgnoreCase(je.getAsString()))) continue;
        nullMembers.add((String)entry.getKey());
      }
      else
      {
        changeNullStringsToNull(je);
      }
    }

    for (String nullMember : nullMembers) {
      jo.remove(nullMember);
      jo.add(nullMember, JsonNull.INSTANCE);
    }
  }
}