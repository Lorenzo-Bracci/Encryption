import java.io.File;
import java.util.*;
import java.io.PrintWriter;
import java.lang.*;
import java.security.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.Arrays;

public class Hiddec{
  //steps to perform:
  //1 => find a way to import the symmetric key encryption/decrytion algorithm
  //2 => find a way to import the MD5 hash function
//3 => read the key and transform it probably in an int, compute also the MD5 hash of the key
//4 => read the file and store it in an array of proper size
//6 => go block by block and try to decrypt it until a result is found matching the MD5 key
//7 => keep reading until another block matching the key is found, meanwhile store evrything that you found in an array for example (or any proper data sttructure)
//8 => decrypt the data, check for integrity (this is possible because an MD5 hash of the data is stored in the end of the blob), and output it to the proper output file
//static byte[] translation = {'a', 'b', 'c', 'd', 'e', 'f'};


private static byte[] decryptBlockCTR(byte[] block, byte[] key, byte[] counter) throws Exception{
  IvParameterSpec iv = new IvParameterSpec(counter);
  SecretKeySpec secKey = new SecretKeySpec(key, "AES");
  Cipher cipher = null;
  try{
  cipher = Cipher.getInstance("AES/CTR/NoPadding");
  cipher.init(Cipher.DECRYPT_MODE, secKey, iv);
}catch(Exception ex){
  System.out.println("An error has occurred during decrytion");
  System.exit(1);
}
  return cipher.doFinal(block);
}

private static String decryptMsgCTR(byte[] block, byte[] key, byte[] counter) throws Exception{
IvParameterSpec iv = new IvParameterSpec(counter);
SecretKeySpec secKey = new SecretKeySpec(key, "AES");
  Cipher cipher = null;
  try{
  cipher = Cipher.getInstance("AES/CTR/NoPadding");
  cipher.init(Cipher.DECRYPT_MODE, secKey, iv);
}catch(Exception ex){
  System.out.println("An error has occurred during decrytion");
  System.exit(1);
}
  byte[] temp = cipher.doFinal(block);
  byte[] candidateKeyHash = new byte[16];
  byte[] keyHash = computeHash(key);
  String secretMsg = "";
  boolean closingKeyHash = false;
  boolean closingDataHash = false;
  int currentPos = 16;//we skip the first 16 bytes because we have already made sure that these are the keyHash
while(currentPos < temp.length){
  for(int i = 0; i < 16; i++)//copy a decryted block in an array
  candidateKeyHash[i] = temp[currentPos + i];
  if(Arrays.equals(keyHash, candidateKeyHash)){
    closingKeyHash = true;
    for(int i = 0; i < 16; i++)//copy a decryted block in an array(should be message hash)
    candidateKeyHash[i] = temp[currentPos + 16 + i];
    if(Arrays.equals(candidateKeyHash, computeHash(secretMsg.getBytes()))){
      closingDataHash = true;
    }
    break;
  }else{
  secretMsg = secretMsg + new String(candidateKeyHash);
  }
  currentPos = currentPos + 16;
}

if(closingKeyHash == false)//make sure that there is a terminating H(k)
return "No terminating H(k) has been found";
if(closingDataHash == false)//make sure that the hash of what we find match the data hash, and make sure that there is even if a data hash
return "Data integrity has not been properly confirmed with data hash";
return secretMsg;
}

private static byte[] decryptBlock(byte[] block, SecretKeySpec secKey) throws Exception{
  Cipher cipher = null;
  try{
  cipher = Cipher.getInstance("AES/ECB/NoPadding");
  cipher.init(Cipher.DECRYPT_MODE, secKey);
}catch(Exception ex){
  System.out.println("An error has occurred during decrytion");
  System.exit(1);
}
  return cipher.doFinal(block);
}


//private static byte[] decryptBlockCTR
private static String findMessageCTR(String file, byte[] key, byte[] counter) throws Exception{
  File inputFile = null;
    InputStream fis = null;
    try{
      inputFile = new File(file);
      fis = new FileInputStream(inputFile);
    }catch(Exception ex){
      System.out.println("An error has occurred while reading the file containing the encrypted message");
      System.exit(1);
    }
  //  SecretKeySpec secKey = new SecretKeySpec(key, "AES");
    String secretMessage = "";
    byte[] keyHash = computeHash(key);//store key hash in memory
  boolean done = false;
  int currentPos = 0;
  while(!done && currentPos < ((int) inputFile.length())){//we decrypt 16 bytes at a time
  currentPos = currentPos + 16;
  byte[] bytes = new byte[16];
  fis.read(bytes);
  if(Arrays.equals(keyHash, decryptBlockCTR(bytes, key, counter))){
    byte[] restOfFile = new byte[(int) inputFile.length() - currentPos];
    fis.read(restOfFile);
    byte[] attempt = new byte[restOfFile.length + 16];
    for(int i = 0; i < 16; i++)//copy initial key hash vector into attempt array
    attempt[i] = bytes[i];
    for(int i = 0; i < restOfFile.length; i++)//copy rest of the file into attempt array
    attempt[i + 16] = restOfFile[i];
    secretMessage = decryptMsgCTR(attempt, key, counter);
    if(!(secretMessage.equals("Data integrity has not been properly confirmed with data hash")))
    break;
  }

  }
          fis.close();
          if(secretMessage.equals(""))//make sure that we actually found a message, otherwise specify that no message has been found
          return "No secret message has been found";
          return secretMessage;
}

private static String findMessage(String file, byte[] key) throws Exception{
File inputFile = null;
  InputStream fis = null;
  try{
    inputFile = new File(file);
    fis = new FileInputStream(inputFile);
  }catch(Exception ex){
    System.out.println("An error has occurred while reading the file containing the encrypted message");
    System.exit(1);
  }
  SecretKeySpec secKey = new SecretKeySpec(key, "AES");
  String secretMessage = "";
  byte[] keyHash = computeHash(key);//store key hash in memory
boolean done = false;
boolean closingKeyHash = false;
boolean closingDataHash = false;
int currentPos = 0;
while(!done && currentPos < ((int) inputFile.length())){//we decrypt 16 bytes at a time
currentPos = currentPos + 16;
byte[] bytes = new byte[16];
fis.read(bytes);
if(Arrays.equals(keyHash, decryptBlock(bytes, secKey))){
  while(true){
    fis.read(bytes);//read 16 bytes  in the array
    byte[] decryptedBytes = decryptBlock(bytes, secKey);
    if(Arrays.equals(keyHash, decryptedBytes)){
      closingKeyHash = true;//make sure that we find the end
            fis.read(bytes);//read what should be the data hash
            byte[] decryptedDataHash = decryptBlock(bytes, secKey);
            if(Arrays.equals(computeHash(secretMessage.getBytes()), decryptedDataHash))//check if there is matching hash of the data after the closing H(key)
            closingDataHash = true;//assert that the data is correct
    break;
    }
  secretMessage = secretMessage + new String(decryptedBytes);
}
done = true;//make sure that we find the end
}

}
        fis.close();
        if(secretMessage.equals(""))//make sure that we actually found a message, otherwise specify that no message has been found
        return "No secret message has been found";
        if(closingKeyHash == false)//make sure that there is a terminating H(k)
        return "No terminating H(k) has been found";
        if(closingDataHash == false)//make sure that the hash of what we find match the data hash, and make sure that there is even if a data hash
        return "Data integrity has not been properly confirmed with data hash";
        return secretMessage;
}

private static byte[] computeHash(byte[] arr) throws Exception{
  MessageDigest md = MessageDigest.getInstance("MD5");
   md.update(arr);
      return md.digest();
}

private static byte[] hexaToDecimal(String str) {
  byte[] hexa = new byte[str.length() / 2];
  for (int i = 0; i < hexa.length; i++) {
   int index = i * 2;
   int j = Integer.parseInt(str.substring(index, index + 2), 16);//read the string in base 16
   hexa[i] = (byte) j;
}
return hexa;
}

/*private static byte[] getKey(String file) throws Exception{
File keyFile = null;
  Scanner scan = null;
  try{
    keyFile = new File(file);
    scan = new Scanner(keyFile);
  }catch(Exception ex){
    System.out.println("An error has occurred while reading the key file");
    System.exit(1);
  }
  String hexaString = scan.nextLine();
  scan.close();
return hexaToDecimal(hexaString);
}*/

public static void main(String[] args){
//we will take the following arguments:
//--key=KEY is an hexadecimal string
//--input=INPUT
//--ctr=CTR is the initial value for the counter in AES-128-CTR mode. Implies that AES-128-CTR mode should be used for encryption (otherwise AES-128-ECB)
//--output=OUTPUT is the output file

Hashtable<String, String> arguments = new Hashtable<String, String>();
try{
for(int i = 0; i < args.length; i++){//code to parse arguments
String[] parsedArgs = args[i].split("=");
arguments.put(parsedArgs[0], parsedArgs[1]);
}
byte[] key = hexaToDecimal(arguments.get("--key"));//successful get the key array
PrintWriter output = new PrintWriter(new File(arguments.get("--output")));
String secretMsg = "";
if(arguments.containsKey("--ctr")){
secretMsg = findMessageCTR(arguments.get("--input"), key, hexaToDecimal(arguments.get("--ctr")));
}else{
secretMsg = findMessage(arguments.get("--input"), key);
}
if(secretMsg.equals("No secret message has been found") || secretMsg.equals("No terminating H(k) has been found") || secretMsg.equals("Data integrity has not been properly confirmed with data hash")){
  System.out.println(secretMsg);//if the output of the function that computes a secret message is a an error message than we print it to the terminal
}else{
  output.print(secretMsg);//print the secret message to the output file
}
output.close();
}catch(Exception ex){
  System.out.println("An error has occurred while parsing the arguments");
  System.exit(1);
}

}
}

/*"No secret message has been found";
if(closingKeyHash == false)//make sure that there is a terminating H(k)
return "No terminating H(k) has been found";
if(closingDataHash == false)//make sure that the hash of what we find match the data hash, and make sure that there is even if a data hash
return "Data integrity has not been properly confirmed with data hash"*/
