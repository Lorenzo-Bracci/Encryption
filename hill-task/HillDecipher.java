import java.io.File;
import java.util.*;
import java.io.FileWriter;
import org.jscience.mathematics.vector.*;
import org.jscience.mathematics.number.*;

public class HillDecipher{
  public static void main(String args[]) throws Exception{
    //args[0] modulo
  //args[1] blocksize
  //args[2] keyfile
  //args[3] inputfile(with integers)
  //args[4] outputfile(with integers)
  //first step: creating matrices and vectors(we will use several vectors obviusly) to have random access to data and have faster matrix multiplication
  //second step: matrix multiplication
  //third step: write to file output of matrix multiplication
int modulo = Integer.parseInt(args[0]);
  int length = Integer.parseInt(args[1]);
  File keyFile = new File(args[2]);
  File inputFile = new File(args[3]);
FileWriter outputFile = new FileWriter(args[4]);
int[][] key = new int[length][length];
int[] input = new int[length];//we store the values of the input file
ModuloInteger.setModulus(LargeInteger.valueOf((long) modulo));
ModuloInteger[][] checkMatrix = new ModuloInteger[length][length];
try{
  Scanner lengthScanner = new Scanner(inputFile);//used to count the length of the file to know where the padding is
Scanner scan = new Scanner(keyFile);
Scanner inputScan = new Scanner(inputFile);
int fileLength = 0;
while(lengthScanner.hasNextInt()){
int num = lengthScanner.nextInt();
fileLength++;
}
int blocks = fileLength/length;//check how many blocks you have for padding
int currentBlock = 1;//used to keep track of how many blocks we encountered so far
int inputCounter = 0;//used to keep track of where we are in array when we are scanning the input
int row = 0;//used to keep track of current row
int column = 0;//used to keep track of current column
  while(scan.hasNextInt()){//we read the key and store it in a matrix, if there is some error it is caught there
    long value = (long) scan.nextInt();
    if(value < 0 || value > (modulo-1)){
      System.out.println("Input wrong, at least a value is outsude of the correct range(0-25)");
      System.exit(1);
    }
checkMatrix[row][column] = ModuloInteger.valueOf(LargeInteger.valueOf(value));
    column++;
    if(column == length){//go over to new row
row++;
column = 0;
  }
}
//HERE I INSERT CODE TO INVERT THE KEY TO GET THE DECRYPTION KEY
DenseMatrix<ModuloInteger> keyMatrix = DenseMatrix.valueOf(checkMatrix);
DenseMatrix<ModuloInteger> inverseKey = keyMatrix.inverse();
for(int i = 0; i < length; i++){//looping to retrive the inverse matrix and copy it as an array of arrays
for(int j = 0; j < length; j++){
ModuloInteger currentVal = inverseKey.get(i, j);
key[i][j] = (int) currentVal.longValue();
}
}
while(inputScan.hasNextInt()){
  int val = inputScan.nextInt();
  if(val < 0 || val > (modulo-1)){
    System.out.println("Input wrong, at least a value is outsude of the correct range(0-25)");
    System.exit(1);
  }
  input[inputCounter] = val;
  inputCounter++;
  if(inputCounter == length && currentBlock != blocks){//we flush the array and write to the output file
   int[] result = matrixMul(key, input, modulo);//perform decryption
    inputCounter = 0;//reset counter to 0
    currentBlock++;//increment the value of the current block to be able to know when we are done
    for(int i = 0; i < length; i++)
    outputFile.write(new Integer(result[i]).toString() + " ");
  }
}
boolean paddingFound = false;
int[] result2 = matrixMul(key, input, modulo);//perform decryption of the last block(padding block)
for(int i = 0; i < length; i++){//the code in this for loop is used to remove the padding
int val = result2[i];
int j = i;
if(val == (length - i)){//check if it can be padding
while(j != length){//make sure that it is padding
if(result2[j] != val)
break;
j++;
}
if(j == length)
paddingFound = true;
}
if(!paddingFound){
outputFile.write(new Integer(val).toString() + " ");
}else{
  break;
}
}//here finishes the part of code that deals with padding
outputFile.close();
}catch(Exception ex){
  System.out.println("Input files are not correct(a possible error is vector or matrix size)");
System.exit(1);
}
  }
  private static int[] matrixMul(int[][] matr, int[] vect, int modulo) throws Exception{//in our case matrix multiplication takes a matrix and a vector
int[] result = new int[vect.length];//initialize vector used to write solution in
for(int i = 0; i < matr.length; i++)
  for(int j = 0; j < matr[i].length; j++)
result[i] = add(result[i], mult(matr[i][j], vect[j], modulo), modulo);//compute row in a loop using modular arithmetic
return result;
  }
  private static int add(int a, int b, int modulo){//addition with modular arithmetic
    return ((a+b) % modulo);
  }
    private static int mult(int a, int b, int modulo){//multiplication with modular arithmetic
      return ((a*b) % modulo);
    }
}
