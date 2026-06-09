package com.bct.bpms.cuetrans;

public enum ErrorConstants {
  SUCCESS(0, "Success"),
  EXCEPTION(2, "Something happened"),
  DELETE_LOCAL_FILES_FAILED(3, "Local files not deleted"),
  FILE_UPLOAD_FAILED(1, "File Uploading Failed"),
  XML_GENERATION_FAILED(4, "XML File generation failed"),
  PRIVATE_KEY_EMPTY(5, "Private Key Empty or Null"),
  FILE_UPLOAD_SUCCESS(6, "File Uploaded Successfully"),
  UNABLE_TO_CREATE_SESSION(7, "Session could not be created"),
  UNABLE_TO_CONNECT(8, "Could not be connected to SFTP Server"),
  UNABLE_TO_OPEN_CHANNEL(9, "Could not open SFTP Channel"),
  File_NOT_FOUND(10, "File Not Found"),
  SECRET_KEY_UNAVAILABLE(11, "Secret Key is unavailable"),
  ENCRYPTION_FAILED(12, "File Encryption Failed"),
  FILE_DECRYPT_EXCEPTION(-1, "File Decryption Failed"),
  NO_DATA_AVAILABLE(13, "XML not generated. No data available"),
  IO_EXCEPTION(14, "IO EXCEPTION OCCURRED");
  
  public final int id;
  
  public final String message;
  
  ErrorConstants(int id, String message) {
    this.id = id;
    this.message = message;
  }
}
