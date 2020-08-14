import java.io.File;
import java.util.*;
import java.io.FileWriter;
import java.util.Date;
import org.jscience.mathematics.vector.*;
import org.jscience.mathematics.number.*;


public class HillKeys{
  public static void main(String args[]) throws Exception{
    //args[0] modulo
  //args[1] blocksize
  //args[2] keyfile
  int modulo = 0;
    int length = 0;
    FileWriter keyFile = new FileWriter(args[2]);
    try{
      modulo =  Integer.parseInt(args[0]);
      length = Integer.parseInt(args[1]);
    }catch(Exception ex){//input validation
      System.out.println("The modulo/length given in the input is incorrect");
    System.exit(1);
    }
    if(modulo < 1 || length < 1){//input validation
      System.out.println("The modulo/length given in the input is incorrect");
    System.exit(1);
    }
    try{
    int[][] matrix = keyGenerator(modulo, length, 0);//the last parameter is used to make sure that we do not spend a lot of time trying matrices with the same values
      for(int i = 0; i < length; i++){//fill the key file with random numbers between 0 and modulo
      for(int j = 0; j < length ; j++){
      keyFile.write(new Integer(matrix[i][j]).toString() + " ");
      }
      keyFile.write("\n");
}
keyFile.close();
}catch(Exception ex){
  System.out.println("An error has occurred while writing to the output file");
System.exit(1);
}
    }
    private static int[][] keyGenerator(int modulo, int length, int offset) throws Exception{
      Date date = new Date();
      int[][] matrix = new int[length][length];
      ModuloInteger.setModulus(LargeInteger.valueOf((long) modulo));
      ModuloInteger[][] checkMatrix = new ModuloInteger[length][length];
        int previous = (int) date.getTime();//used as the seed for the prng
        previous = Math.abs(previous);
        previous = previous + offset;//make sure that we do not repeat ourself
        for(int i = 0; i < length; i++){//fill the key file with random numbers between 0 and modulo
        for(int j = 0; j < length ; j++){
          int rand = randomGenerator(previous);
          int result = rand % modulo;
          checkMatrix[i][j] = ModuloInteger.valueOf(LargeInteger.valueOf((long) result));
        matrix[i][j] = result;
        previous = rand;
        }
  }
  DenseMatrix<ModuloInteger> finalMatrix = DenseMatrix.valueOf(checkMatrix);
  try{
  if((finalMatrix.determinant()).longValue() == 0){
  return keyGenerator(modulo, length, offset + 10);//the offset is used to make sure that we do not retry to make the same matrix
  }else{
  return matrix;
  }
}catch(Exception ex){//if we have a number without multiplicative inverse we have to recompute another matrix
  return keyGenerator(modulo, length, offset + 10);
}
    }
private static int randomGenerator(int previous){
return Math.abs(((966*previous) % 9949));//9949 is prime and 966 is a primitive root of 9949 therefore we should have an uniformely distributed sequence
    }
}
