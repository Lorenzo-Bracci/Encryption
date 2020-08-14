import org.jscience.mathematics.vector.*;
import org.jscience.mathematics.number.*;
import org.jscience.mathematics.structure.*;

public class JsciTest{
  public static void main(String[] args){
  //  Matrix<ModuloInteger> IDENTITY = Matrix.valueOf(
    //          DenseVector.valueOf(ModuloInteger.ONE, ModuloInteger.ONE), ModuloInteger.ZERO);
    long num = 28;
    long mod = 26;
  LargeInteger val = LargeInteger.valueOf(num);
LargeInteger mod2 = LargeInteger.valueOf(mod);
  ModuloInteger.setModulus(mod2);
ModuloInteger first = ModuloInteger.valueOf(val);
ModuloInteger second = ModuloInteger.valueOf(val);
ModuloInteger third = ModuloInteger.valueOf(val);
ModuloInteger fourth = ModuloInteger.valueOf(val);
ModuloInteger[][] matrix = {
  {first, second},
{third, fourth}
};
DenseMatrix<ModuloInteger> IDENTITY = DenseMatrix.valueOf(matrix);
System.out.println(first.longValue());
  }
}
