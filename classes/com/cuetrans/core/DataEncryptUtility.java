package com.cuetrans.core;
import com.cuetrans.core.DataEncryptUtility;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class DataEncryptUtility {
  public static SecretKey getSecretEncryptionKey(String password) throws Exception {
    byte[] key = password.getBytes("UTF-8");
    MessageDigest sha = MessageDigest.getInstance("SHA-1");
    key = sha.digest(key);
    key = Arrays.copyOf(key, 16);
    SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
    return secretKeySpec;
  }
  
  public static byte[] encryptText(String plainText, SecretKey secKey) throws Exception {
    Cipher aesCipher = Cipher.getInstance("AES");
    aesCipher.init(1, secKey);
    byte[] byteCipherText = aesCipher.doFinal(plainText.getBytes());
    return byteCipherText;
  }
  
  public static String decryptText(byte[] byteCipherText, SecretKey secKey) throws Exception {
    System.out.println("inside decrypt text");
    Cipher aesCipher = Cipher.getInstance("AES");
    aesCipher.init(2, secKey);
    byte[] bytePlainText = aesCipher.doFinal(byteCipherText);
    String plainText = new String(bytePlainText);
    return plainText;
  }
  
  public static void main(String[] ar) {
    String password = "C1U245T@#%R*)^";
    try {
      SecretKey encryptKey = getSecretEncryptionKey(password);
      byte[] userId = encryptText("pdouser", encryptKey);
      String base64Enc = DatatypeConverter.printBase64Binary(userId);
      System.out.println("encoded-----" + base64Enc);
      byte[] base64Decod = DatatypeConverter.parseBase64Binary(base64Enc);
      System.out.println("base64 decoded  " + base64Decod);
      byte[] userPwd = encryptText("tenant@123", encryptKey);
      System.out.println("base64 encoded password" + DatatypeConverter.printBase64Binary(userPwd));
      System.out.println("Encrypted Id" + userId + "\n" + "Encrypted Pwd" + userPwd);
      System.out.println("Decrypted userid" + decryptText(base64Decod, encryptKey));
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
}
