package com.bct.cuetrans;

import java.io.IOException;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream; 
import java.io.FileOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ZipFolder 
{
    private List<String> fileList; 
    private static String OUTPUT_ZIP_FILE; 
    private static String SOURCE_FOLDER;
    
    public ZipFolder(final String outputZipFile, final String sourceFolder) {
        ZipFolder.OUTPUT_ZIP_FILE = outputZipFile;
        ZipFolder.SOURCE_FOLDER = sourceFolder;
        this.fileList = new ArrayList<String>();
    }
    
    public void zipIt(final String zipFile) {
        final byte[] buffer = new byte[1024];
        final String source = new File(ZipFolder.SOURCE_FOLDER).getName();
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);
            System.out.println("ZipFolder --> Output to Zip : " + zipFile); 
            FileInputStream in = null;
            for (final String file : this.fileList) { 
                System.out.println("ZipFolder --> File Added : " + file); 
                final ZipEntry ze = new ZipEntry(String.valueOf(source) + File.separator + file);
                zos.putNextEntry(ze);
                try {
                    in = new FileInputStream(String.valueOf(ZipFolder.SOURCE_FOLDER) + File.separator + file);
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }
                finally {
                    in.close();
                }
               // in.close();
            }
            zos.closeEntry();
            System.out.println("ZipFolder --> Folder successfully compressed");
        }
        catch (IOException ex) {
            ex.printStackTrace();
            try {
                zos.close();
                fos.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        finally {
            try {
                zos.close();
                fos.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        } 
        try {
            zos.close();
            fos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
      
    public void generateFileList(final File node) {
        if (node.isFile()) {
            this.fileList.add(this.generateZipEntry(node.toString()));
        }
        if (node.isDirectory()) {
            final String[] subNote = node.list();
            String[] array;
            for (int length = (array = subNote).length, i = 0; i < length; ++i) {
                final String filename = array[i];
                this.generateFileList(new File(node, filename));
            } 
        }
    }
    
    private String generateZipEntry(final String file) {
        return file.substring(ZipFolder.SOURCE_FOLDER.length(), file.length());
    }
}