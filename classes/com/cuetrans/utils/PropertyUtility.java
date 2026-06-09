package com.cuetrans.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtility {
	public static String getPropertyValues(String key, ClassLoader classLoader) throws IOException {
		Properties prop = new Properties();
		String propFileName = "./config.properties";
		InputStream inputStream = classLoader.getResourceAsStream(propFileName);
		prop.load(inputStream);
		if (inputStream == null) {
			throw new FileNotFoundException("Property File" + propFileName + "Not Found");
		}
		String value = prop.getProperty(key);
		return value;
	}
}