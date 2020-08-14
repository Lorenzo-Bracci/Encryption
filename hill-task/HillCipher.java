import java.io.File;
import java.util.*;
import java.io.FileWriter;

public class HillCipher{
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
//try{
Scanner scan = new Scanner(keyFile);
Scanner inputScan = new Scanner(inputFile);
int inputCounter = 0;//used to keep track of where we are in array when we are scanning the input
int row = 0;//used to keep track of current row
int column = 0;//used to keep track of current column
  while(scan.hasNextInt()){//we read the key and store it in a matrix, if there is some error it is caught there
    int value = scan.nextInt();
    if(value < 0 || value > (modulo-1)){
      System.out.println("Input wrong, at least a value is outsude of the correct range");
      System.exit(1);
    }
  key[row][column] = value;
    column++;
    if(column == length){//go over to new row
row++;
column = 0;
  }
}
while(inputScan.hasNextInt()){
  int val = inputScan.nextInt();
  if(val < 0 || val > (modulo-1)){
    System.out.println("Input wrong, at least a value is outsude of the correct range");
    System.exit(1);
  }
  input[inputCounter] = val;
  inputCounter++;
  if(inputCounter == length){//we flush the array and write to the output file
   int[] result = matrixMul(key, input, modulo);//perform encryption
    inputCounter = 0;//reset counter to 0
    for(int i = 0; i < length; i++)
    outputFile.write(new Integer(result[i]).toString() + " ");
  }
}
for(int i = inputCounter; i < length; i++){//deal with padding
input[i] = length - inputCounter;
  }
  int[] result2 = matrixMul(key, input, modulo);//perform encryption of padding
  for(int i = 0; i < length; i++)
  outputFile.write(new Integer(result2[i]).toString() + " ");
outputFile.close();
//}catch(Exception ex){
  //System.out.println("Input files are not correct(a possible error is vector or matrix size)");
//System.exit(1);
//}
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
