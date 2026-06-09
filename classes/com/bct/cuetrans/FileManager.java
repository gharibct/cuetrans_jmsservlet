package com.bct.cuetrans;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileManager {

	public static File createFile(String directory, String epochTime, String fileName) throws IOException {

		File homeDIR = new File(directory);

		if(!homeDIR.exists()) {
			throw new FileNotFoundException("Unable to find the homeDIR "+homeDIR);
		} 

		if(!homeDIR.isDirectory()) { 
			throw new FileNotFoundException("The homeDIR "+homeDIR+" is not a directory");
		}

		File folder = new File(homeDIR, epochTime+"");

		if(folder.exists()) {
			if(!folder.isDirectory()) {
				throw new FileNotFoundException("The subfolder "+folder+" is not a directory");
			}
		} else {
			if(!folder.mkdir()) {
				throw new FileNotFoundException("Unable to create subfolder "+folder);
			}
		}

		File csvFile = null;
		
		if (fileName != null && !fileName.trim().equals("") && !fileName.trim().equalsIgnoreCase("null"))
		{ 
		
		 csvFile = new File(folder, fileName);

		/*if(csvFile.exists()) {
			throw new FileNotFoundException("The csvfile "+csvFile+" already exists");
		}*/

			if(!csvFile.exists() && !csvFile.createNewFile()) {
				throw new FileNotFoundException("Unable to create file "+csvFile);
			}
		}

		return csvFile;
	}
}
