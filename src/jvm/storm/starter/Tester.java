package storm.starter;

import org.jblas.*;
import org.jblas.util.Functions;

import java.util.ArrayList;
import java.util.List;

public class Tester {

    public static void main(String[] args)
    {
        String a = "FLOW,138.37.95.15,105,41,005056bd48c5,333300010002,0x86dd,500,500,fe80:0000:0000:0000:8900:6bd4:95bf:9813,ff02:0000:0000:0000:0000:0000:0001:0002,17,0x00,1,546,547,0x10,181,159,1024";
        String b = "FLOW,138.37.95.24,296,2,001aa034ac8d,0022192f3c15,0x0800,500,500,138.37.88.54,138.37.88.249,6,0x00,64,1232,24561,0x10,70,52,1024";
        String c = "FLOW,138.37.95.25,290,1073741823,005056bd5001,c81f66b16814,0x0806,503,503,-,-,0,0x00,0,0,0,0x00,68,60,1024";
        String d = "FLOW,138.37.95.6,46,27,d4bed99ae5d7,333300010002,0x86dd,0,0,fe80:0000:0000:0000:5caa:dff0:81e1:d4d8,ff02:0000:0000:0000:0000:0000:0001:0002,17,0x00,1,546,547,0x00,182,174,1024";

        String[] arr = new String[4];
        arr[0] = a;
        arr[1] = b;
        arr[2] = c;
        arr[3] = d;

        //execute(arr);

        DoubleMatrix eigenVector = findEigenvector();

        //windowMatrixTester();

        DoubleMatrix principalLeftSingularVector = svdTester();

        System.out.println("This should equal 1?: " + eigenVector.transpose().mmul(principalLeftSingularVector).toString());



    }

    private static DoubleMatrix svdTester()
    {
        double[][] d = new double[][]{
                {3,1,1},
                {-1,3,1},
                {1,1,3}
        };

        DoubleMatrix mat = new DoubleMatrix(d);
        DoubleMatrix[] svd = Singular.fullSVD(mat);

        for(int i =0; i < svd.length; i++)
        {
            System.out.println("svd["+i+"]: "+svd[i]);
        }

        System.out.println("////");

        DoubleMatrix singluarValues = Singular.SVDValues(mat);
        System.out.println("singular Values: " + singluarValues);

        int maxColVal = getMaxSingularValueColumnIndex(singluarValues);
        DoubleMatrix principalLeftSingularVectorOfUt = svd[0].getColumn(maxColVal);

        System.out.println("pLeftSingularVector: " + principalLeftSingularVectorOfUt);
        return principalLeftSingularVectorOfUt;


    }

    private static int getMaxSingularValueColumnIndex(DoubleMatrix mat)
    {
        int maxIndex = 0;
        double[] doubleMatrix = mat.toArray();
        for (int i = 0; i < doubleMatrix.length; i++){
            double newnumber = doubleMatrix[i];
            if (newnumber > doubleMatrix[maxIndex]){
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private static void windowMatrixTester()
    {
        DoubleMatrix windowMatrix = null;
        DoubleMatrix v1 = new DoubleMatrix(new double[] {1,2,3});
        DoubleMatrix v2 = new DoubleMatrix(new double[] {4,5,6});
        DoubleMatrix v3 = new DoubleMatrix(new double[] {7,8,9});
        DoubleMatrix v4 = new DoubleMatrix(new double[] {10,11,12});
        ArrayList<DoubleMatrix> vectorList = new ArrayList<DoubleMatrix>();
        vectorList.add(v1);
        vectorList.add(v2);
        vectorList.add(v3);
        vectorList.add(v4);

        int windowSize = 3;

        for(DoubleMatrix vector : vectorList) {

            if (windowMatrix == null) {
                windowMatrix = vector;
            }
            else
            {
                //window matrix not yet filled
                if (windowMatrix.columns < windowSize) {
                    windowMatrix = windowMatrix.concatHorizontally(windowMatrix, vector);
                }
                else
                {
                    DoubleMatrix newMatrix = windowMatrix.getColumn(1);
                    for (int i = 2; i < windowSize; i++) {
                        newMatrix = newMatrix.concatHorizontally(newMatrix, windowMatrix.getColumn(i));
                    }
                    newMatrix = newMatrix.concatHorizontally(newMatrix, vector);
                    windowMatrix = newMatrix;

                    System.out.println(windowMatrix);
                }
            }
        }


    }

    private static DoubleMatrix findEigenvector()
    {
        double[][] d = new double[][]{
                {3,-1,1},
                {-1,3,1},
                {1,1,3}
        };

        double[][] dOne = new double[][]{
                {-1,2,2},
                {2,2,-1},
                {2,-1,2}
        };

        double[][] dThree = new double[][]{
                {3,2,4},
                {2,0,2},
                {4,2,3}
        };
        double[][] dTwo = new double[][]{
                {1,2},
                {4,3}
        };
        double[][] dFour = new double[][] {
                {1,1,0,0,1,0,0},
                {1,1,0,0,1,0,0},
                {0,0,1,1,1,0,0},
                {0,0,1,1,1,0,0},
                {1,1,1,1,1,1,1},
                {0,0,0,0,1,1,1},
                {0,0,0,0,1,1,1},
        };
        double[][] dFive = new double[][]{
                {4,2,4},
                {2,0,2},
                {4,2,3}
        };



        DoubleMatrix matrix3 = new DoubleMatrix(d);

        DoubleMatrix vector2 = getPrincipalEigenvector2(matrix3);
        System.out.println("normalisedPrincipalEigenvector2 = " + normalized(vector2));
        System.out.println("normalisedPrincipalEigenvector2 rows = " + normalized(vector2).rows);
        System.out.println("normalisedPrincipalEigenvector2 columns = " + normalized(vector2).columns);
//        List<Double> vector = getPrincipalEigenvector(matrix3);
//        System.out.println("normalisedPrincipalEigenvector = " + normalised(vector));
        System.out.println();
        return vector2;

//        DoubleMatrix matrix = new DoubleMatrix(dThree);
//        DoubleMatrix matrix2 = new DoubleMatrix(dFive);

        /*ComplexDoubleMatrix eigenvalues = Eigen.eigenvalues(matrix);
        for (ComplexDouble eigenvalue : eigenvalues.toArray()) {
            System.out.print(String.format("%.2f ", eigenvalue.real()));
        }
        System.out.println();

        List<Double> principalEigenvector = getPrincipalEigenvector(matrix);
        System.out.println("principalEigenvector = " + principalEigenvector);

        System.out.println("normalisedPrincipalEigenvector = " + normalised(principalEigenvector));

        System.out.println("////");

        ComplexDoubleMatrix eigenvalues2 = Eigen.eigenvalues(matrix2);
        for (ComplexDouble eigenvalue : eigenvalues2.toArray()) {
            System.out.print(String.format("%.2f ", eigenvalue.real()));
        }
        System.out.println();

        List<Double> principalEigenvector2 = getPrincipalEigenvector(matrix2);
        System.out.println("principalEigenvector = " + principalEigenvector2);

        System.out.println("normalisedPrincipalEigenvector = " + normalised(principalEigenvector2));*/

    }

    //Mixture of code from and based on http://www.markhneedham.com/blog/2013/08/05/javajblas-calculating-eigenvector-centrality-of-an-adjacency-matrix/
    private static List<Double> getPrincipalEigenvector(DoubleMatrix matrix) {
        int maxIndex = getMaxIndex(matrix);
        ComplexDoubleMatrix eigenVectors = Eigen.eigenvectors(matrix)[0];
        return getEigenVector(eigenVectors, maxIndex);
    }

    private static DoubleMatrix getPrincipalEigenvector2(DoubleMatrix matrix){
        int maxIndex = getMaxIndex(matrix);
        ComplexDoubleMatrix eigenVectors = Eigen.eigenvectors(matrix)[0];
        return getEigenVector2(eigenVectors, maxIndex);
    }

    private static int getMaxIndex(DoubleMatrix matrix) {
        ComplexDouble[] doubleMatrix = Eigen.eigenvalues(matrix).toArray();
        int maxIndex = 0;
        for (int i = 0; i < doubleMatrix.length; i++){
            double newnumber = doubleMatrix[i].abs();
            if ((newnumber > doubleMatrix[maxIndex].abs())){
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private static List<Double> getEigenVector(ComplexDoubleMatrix eigenvector, int columnId) {
        ComplexDoubleMatrix column = eigenvector.getColumn(columnId);

        List<Double> values = new ArrayList<Double>();
        for (ComplexDouble value : column.toArray()) {
            values.add(value.abs()  );
        }

//        System.out.println("vector: " + values);
//        System.out.println("complexDouble vector: " + eigenvector.toString());
//        System.out.println("/////");


        return values;
    }

    private static DoubleMatrix getEigenVector2(ComplexDoubleMatrix eigenvector, int columnId)
    {
        ComplexDoubleMatrix column = eigenvector.getColumn(columnId);
        DoubleMatrix vector = new DoubleMatrix(eigenvector.rows);
        int i = 0;

        for(ComplexDouble value : column.toArray())
        {
            vector.put(i++, value.abs());
        }

//        System.out.println("vector2: " + vector);
//        System.out.println("complexDouble vector2: " + eigenvector.toString());
//        System.out.println("/////");


        return vector;
    }

    private static List<Double> normalised(List<Double> principalEigenvector) {
        double total = sum(principalEigenvector);
        List<Double> normalisedValues = new ArrayList<Double>();
        for (Double aDouble : principalEigenvector) {
            normalisedValues.add(aDouble / total);
        }
        return normalisedValues;
    }

    private static DoubleMatrix normalized(DoubleMatrix principalEigenvector)
    {
        double total = sum2(principalEigenvector);
        DoubleMatrix normalizedValues = new DoubleMatrix(principalEigenvector.rows);
        int i = 0;
        for (Double aDouble : principalEigenvector.toArray()) {
            normalizedValues.put(i++, (aDouble / total));
        }
        return normalizedValues;
    }

    private static double sum(List<Double> principalEigenvector) {
        double total = 0;
        for (Double aDouble : principalEigenvector) {
            total += aDouble;
        }
        return total;
    }

    private static double sum2(DoubleMatrix principalEigenvector){
        double total = 0;
        for (Double aDouble : principalEigenvector.toArray()) {
            total += aDouble;
        }
        return total;
    }

    private static void execute(String[] arr) {

        for(String str : arr) {


            String[] elements = str.split(",");

            String srcIP = elements[9];
            String dstIP = elements[10];

            //clears empty cases
            if (srcIP.length() > 1 && dstIP.length() > 1) {
                //extract subnet
                String[] srcBytes = srcIP.split("\\.");
                String[] dstBytes = dstIP.split("\\.");


                String srcSubnet = "";
                String dstSubnet = "";

                //clears IPv6 cases
                if (srcBytes.length != 0 && dstBytes.length != 0) {
                    try {
                        int octets = 3;
                        for (int i = 0; i < octets; i++) {
                            srcSubnet += srcBytes[i] + ".";
                            dstSubnet += dstBytes[i] + ".";

                        }
                        srcSubnet = srcSubnet.substring(0, srcSubnet.length() - 1);
                        dstSubnet = dstSubnet.substring(0, dstSubnet.length() - 1);


                        System.out.println(srcSubnet + "," + dstSubnet);
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        System.out.println("out of bounds");
                    }
                } else
                    System.out.println("err,err");
            }
        }

    }

}
