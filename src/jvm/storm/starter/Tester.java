package storm.starter;

import org.jblas.ComplexDouble;
import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;
import org.jblas.Eigen;

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

        findEigenvector();


    }

    private static void findEigenvector()
    {
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


        DoubleMatrix matrix = new DoubleMatrix(dThree);
        DoubleMatrix matrix2 = new DoubleMatrix(dFive);

        ComplexDoubleMatrix eigenvalues = Eigen.eigenvalues(matrix);
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

        System.out.println("normalisedPrincipalEigenvector = " + normalised(principalEigenvector2));

    }

    //Code from http://www.markhneedham.com/blog/2013/08/05/javajblas-calculating-eigenvector-centrality-of-an-adjacency-matrix/
    private static List<Double> getPrincipalEigenvector(DoubleMatrix matrix) {
        int maxIndex = getMaxIndex(matrix);
        ComplexDoubleMatrix eigenVectors = Eigen.eigenvectors(matrix)[0];
        return getEigenVector(eigenVectors, maxIndex);
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
        return values;
    }

    private static List<Double> normalised(List<Double> principalEigenvector) {
        double total = sum(principalEigenvector);
        List<Double> normalisedValues = new ArrayList<Double>();
        for (Double aDouble : principalEigenvector) {
            normalisedValues.add(aDouble / total);
        }
        return normalisedValues;
    }

    private static double sum(List<Double> principalEigenvector) {
        double total = 0;
        for (Double aDouble : principalEigenvector) {
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
