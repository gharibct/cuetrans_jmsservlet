package com.cuetrans.utils;

import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import net.sf.json.JSONArray;
import javax.sql.rowset.CachedRowSet;

import java.util.HashMap;
import java.util.Map;
import net.sf.json.JSONObject;

public class TestHashMapJSONParser
{
    public static Map convertJSONObjectToHashMap(final JSONObject jsonObject) {
        Map map = new HashMap();
        map = JSONToHashMap(jsonObject);
        return map;
    }
    
    
    public static void main (String [] args )
	{
		
		
		
		
	}
    
    public static JSONObject convertHashMapToJSONObject(final Map map) {
        JSONObject jsonObject = new JSONObject();
        jsonObject = HashMapToJsonObject(map);
        return jsonObject;
    }
    
    private static JSONArray CachedRowSetToJsonAray(final CachedRowSet rowSet) {
        final JSONArray array = new JSONArray();
        try {
            rowSet.beforeFirst();
            final ResultSetMetaData metaData = rowSet.getMetaData();
            final int rowSetSize = rowSet.size();
            final int columnCount = metaData.getColumnCount();
            final List columnNames = new ArrayList();
            for (int columnIndex = 1; columnIndex <= columnCount; ++columnIndex) {
                columnNames.add(metaData.getColumnLabel(columnIndex));
            }
            for (int rowIndex = 0; rowIndex < rowSetSize; ++rowIndex) {
                rowSet.next();
                final JSONObject record = new JSONObject();
                for (int colNameIndex = 0; colNameIndex < columnNames.size(); ++colNameIndex) {
                    final String columnName = (String)columnNames.get(colNameIndex);
                    
                    System.out.println(columnName);
                    record.accumulate(columnName, rowSet.getObject(columnName));
                }
                array.add((Object)record);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return array;
    }
    
    private static JSONArray ArrayListToJsonArray(final List list) {
        final JSONArray array = new JSONArray();
        for (int index = 0; index < list.size(); ++index) {
            final Object value = list.get(index);
            if (value instanceof CachedRowSet) {
                array.add((Object)CachedRowSetToJsonAray((CachedRowSet)value));
            }
            else if (value instanceof ArrayList) {
                array.add((Object)ArrayListToJsonArray((List)value));
            }
            else if (value instanceof Map) {
                array.add((Object)HashMapToJsonObject((Map<Object, Object>)value));
            }
            else {
                array.add((Object)value.toString());
            }
        }
        return array;
    }
    
    private static JSONObject HashMapToJsonObject(final Map<Object, Object> map) {
        final JSONObject jsonObject = new JSONObject();
        final Iterator iterator = map.keySet().iterator();
        final ArrayList<String> mapKeys = new ArrayList<String>();
        while (iterator.hasNext()) {
            mapKeys.add((String)iterator.next());
        }
        for (int keyIndex = 0; keyIndex < mapKeys.size(); ++keyIndex) {
            final String key = mapKeys.get(keyIndex);
            final Object value = map.get(key);
            if (value instanceof CachedRowSet) {
                jsonObject.accumulate(key, (Object)CachedRowSetToJsonAray((CachedRowSet)value));
            }
            else if (value instanceof ArrayList) {
                jsonObject.accumulate(key, (Object)ArrayListToJsonArray((List)value));
            }
            else if (value instanceof HashMap) {
                jsonObject.accumulate(key, (Object)HashMapToJsonObject((Map<Object, Object>)value));
            }
            else {
                jsonObject.accumulate(key, value);
            }
        }
        return jsonObject;
    }
    
    private static Map JSONToHashMap(final JSONObject input) {
        final Map result = new HashMap();
        final Set keySet = input.keySet();
        for (final Object key :  keySet) {
            final Object value = input.get(key);
            

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

