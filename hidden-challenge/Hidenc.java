import java.io.File;
import java.util.*;
import java.io.FileWriter;
import java.lang.*;
import java.security.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.crypto.spec.IvParameterSpec;

public class Hidenc{

  private static byte[] computeHash(byte[] arr) throws Exception{
    MessageDigest md = MessageDigest.getInstance("MD5");
     md.update(arr);
        return md.digest();
  }

  private static byte[] encryptBlock(byte[] block, SecretKeySpec key) throws Exception{
    Cipher cipher = null;
    try{
    cipher = Cipher.getInstance("AES/ECB/NoPadding");
    cipher.init(Cipher.ENCRYPT_MODE, key);
  }catch(Exception ex){
    System.out.println("An error has occurred during encrytion");
    System.exit(1);
  }
    return cipher.doFinal(block);
  }

  private static byte[] encryptBlockCTR(byte[] block, byte[] key, byte[] counter) throws Exception{
    IvParameterSpec iv = new IvParameterSpec(counter);
    SecretKeySpec secKey = new SecretKeySpec(key, "AES");
    Cipher cipher = null;
    try{
    cipher = Cipher.getInstance("AES/CTR/NoPadding");
    cipher.init(Cipher.ENCRYPT_MODE, secKey, iv);
  }catch(Exception ex){
    System.out.println("An error has occurred during encrytion");
    System.exit(1);
  }
    return cipher.doFinal(block);
  }

private static byte[] createBlobCTR(String file, byte[] key, byte[] counter) throws Exception{
  File inputFile = null;
    InputStream fis = null;
    try{
      inputFile = new File(file);
      fis = new FileInputStream(inputFile);
    }catch(Exception ex){
      System.out.println("An error has occurred while reading the file that we want to encrypt");
      System.exit(1);
    }
  //  SecretKeySpec secKey = new SecretKeySpec(key, "AES");
    byte[] bytes = new byte[(int) inputFile.length()];
    byte[] keyHash = computeHash(key);//store key hash in memory
    fis.read(bytes);//store the file containing the data to encrypt into a byte array
    fis.close();
    byte[] msgHash = computeHash(bytes);//store the hash of the message
    int blobSize = bytes.length + 48;//the size of the resulting byte array is equal to the size of the data to encrypt + (48 = 16*3) the size of the three hashes to be compputed
byte[] blob = new byte[blobSize];//initialize array to store result, i.e the blob
for(int i = 0; i < keyHash.length; i++)//copy the encrypted keyHash inside the beginning of the blob
blob[i] = keyHash[i];

for(int i = 0; i < bytes.length; i ++){//add message to blob
blob[i + 16] = bytes[i];
}


for(int i = 0; i < keyHash.length; i++)//copy the encrypted keyHash in the blob after the initial keyHash the encrypted message
blob[i + 16 + bytes.length] = keyHash[i];

for(int i = 0; i < msgHash.length; i++)//copy the encrypted message Hash in the blob after the initial keyHash the encrypted message and the closing keyHash
blob[i + 32 + bytes.length] = msgHash[i];

return encryptBlockCTR(blob, key, counter);
}

  private static byte[] hexaToDecimal(String str) {//transform hexa to decimal
    byte[] hexa = new byte[str.length() / 2];
    for (int i = 0; i < hexa.length; i++) {
     int index = i * 2;
     int j = Integer.parseInt(str.substring(index, index + 2), 16);//read the string in base 16
     hexa[i] = (byte) j;
  }
  return hexa;
  }

  /*private static byte[] getKey(String file) throws Exception{//read the key into a byte array
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

private static byte[] createBlob(String file, byte[] key) throws Exception{
  File inputFile = null;
    InputStream fis = null;
    try{
      inputFile = new File(file);
     fis = new FileInputStream(inputFile);
    }catch(Exception ex){
      System.out.println("An error has occurred while reading the file that we want to encrypt");
      System.exit(1);
    }
    SecretKeySpec secKey = new SecretKeySpec(key, "AES");
    byte[] bytes = new byte[(int) inputFile.length()];
    byte[] keyHash = computeHash(key);//store key hash in memory
    fis.read(bytes);//store the file containing the data to encrypt into a byte array
    fis.close();
    byte[] encryptedMsgHash = encryptBlock(computeHash(bytes), secKey);//store the encrypted hash of the message
    int blobSize = bytes.length + 48;//the size of the resulting byte array is equal to the size of the data to encrypt + (48 = 16*3) the size of the three hashes to be compputed
byte[] blob = new byte[blobSize];//initialize array to store result, i.e the blob
byte[] encryptedKeyHash = encryptBlock(keyHash, secKey);//encrypt the key hash
for(int i = 0; i < encryptedKeyHash.length; i++)//copy the encrypted keyHash inside the beginning of the blob
blob[i] = encryptedKeyHash[i];

for(int i = 0; i < bytes.length; i += 16){
  byte[] encryptedBlock = encryptBlock(Arrays.copyOfRange(bytes, i, (i + 16)), secKey);//store an encrypted 16 bytes block from the the message (suitable size for AES 128)
for(int j = 0; j < encryptedBlock.length; j++){//copy the encrypted block into the return blob array and into another byte array used later to compute the hash
blob[i + j + 16] = encryptedBlock[j];//add encrypted message to blob
}
}

for(int i = 0; i < encryptedKeyHash.length; i++)//copy the encrypted keyHash in the blob after the initial keyHash the encrypted message
blob[i + 16 + bytes.length] = encryptedKeyHash[i];

for(int i = 0; i < encryptedMsgHash.length; i++)//copy the encrypted message Hash in the blob after the initial keyHash the encrypted message and the closing keyHash
blob[i + 32 + bytes.length] = encryptedMsgHash[i];

return blob;
}

  public static void main(String[] args){
    //we will take the following arguments:
    //--key=KEY is an hexadecimal string
    //--ctr=CTR is the initial value for the counter in AES-128-CTR mode. Implies that AES-128-CTR mode should be used for encryption (otherwise AES-128-ECB)
    //--input=INPUT is the secret message to encrypt and hide
    //--output=OUTPUT is the output file where we should put the file containing the encrypted information
    //--offset=NUM Place the blob in the file at an offset of NUM bytes into the container file. If no offset is given, Hidenc generates it by random.
    //--size=SIZE The total size of the output file should be SIZE bytes. This implies that the container file should be generated automatically. In other words, there is no template
    //--template=TEMPLATE Use the file TEMPLATE as a template for the container in which the blob should be stored. The output file should have exactly the same size the the template.
    //ONE OF SIZE AND TEMPLATE MUST BE INDICATED(BUT NOT BOTH)

    Hashtable<String, String> arguments = new Hashtable<String, String>();
    try{
      for(int i = 0; i < args.length; i++){//code to parse arguments
      String[] parsedArgs = args[i].split("=");
      arguments.put(parsedArgs[0], parsedArgs[1]);
      }

    byte[] key = hexaToDecimal(arguments.get("--key"));//successful get the key array

Random rand = new Random(System.currentTimeMillis());

int size = 0;
File template = null;
File inputFile = new File(arguments.get("--input"));
int msgLength = (int) inputFile.length();

byte[] blob = new byte[msgLength + 48];//initialize blob
if(arguments.containsKey("--ctr")){//in this if else statement we compute the blob using the chosen AES mode
 blob = createBlobCTR(arguments.get("--input"), key, hexaToDecimal(arguments.get("--ctr")));
}else{
 blob = createBlob(arguments.get("--input"), key);
}

if(arguments.containsKey("--size")){//used to initialize size
  size = Integer.parseInt(arguments.get("--size"));
}else{
  template = new File(arguments.get("--template"));
  size = (int) template.length();
}

int offset = 0;
if(arguments.containsKey("--offset")){
  offset = Integer.parseInt(arguments.get("--offset"));//this chould be computed after that we know the size
  if(offset > (size - blob.length)){
    System.out.println("The offset is too large, the blob cannot be inserted after the end of the container file");
    System.exit(1);
  }
}else{
offset = (rand.nextInt() % (size - blob.length));//we randomly inilialize an offset which should not exceed the size of the file - blob length
}

SecureRandom random = new SecureRandom();//initialize pseudo random number generator
byte[] randomBytes = new byte[offset];
byte[] closingRandomBytes = new byte[size - blob.length - offset];//bytes to add after the blob
if(arguments.containsKey("--size")){
  random.nextBytes(randomBytes);
  random.nextBytes(closingRandomBytes);
}else{
  byte[] substitutedByBlob = new byte[blob.length];//used to read bytes that will be substituted by the blob
  InputStream  fis = new FileInputStream(template);
fis.read(randomBytes);
fis.read(substitutedByBlob);
fis.read(closingRandomBytes);
fis.close();
}



File outputFile = new File(arguments.get("--output"));

byte[] finalResult = new byte[size];//here we store all the bytes that need to be written to the output file
for(int i = 0; i < offset; i++)
finalResult[i] = randomBytes[i];
for(int i = 0; i < blob.length; i++)
finalResult[i + offset] = blob[i];
for(int i = 0; i < closingRandomBytes.length; i++)
finalResult[i + offset + blob.length] = closingRandomBytes[i];
Files.write(outputFile.toPath(), finalResult);//write random data based on the offset
}catch(Exception ex){
  System.out.println("An error has occurred while parsing the arguments");
  System.exit(1);
}
}

}
