package storm.starter;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.jblas.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Tester {


    private static double firstMoment = 0.25;
    private static double secondMoment = 0.175;


    public static void main(String[] args)
    {
        /*String a = "FLOW,138.37.95.15,105,41,005056bd48c5,333300010002,0x86dd,500,500,fe80:0000:0000:0000:8900:6bd4:95bf:9813,ff02:0000:0000:0000:0000:0000:0001:0002,17,0x00,1,546,547,0x10,181,159,1024";
        String b = "FLOW,138.37.95.24,296,2,001aa034ac8d,0022192f3c15,0x0800,500,500,138.37.88.54,138.37.88.249,6,0x00,64,1232,24561,0x10,70,52,1024";
        String c = "FLOW,138.37.95.25,290,1073741823,005056bd5001,c81f66b16814,0x0806,503,503,-,-,0,0x00,0,0,0,0x00,68,60,1024";
        String d = "FLOW,138.37.95.6,46,27,d4bed99ae5d7,333300010002,0x86dd,0,0,fe80:0000:0000:0000:5caa:dff0:81e1:d4d8,ff02:0000:0000:0000:0000:0000:0001:0002,17,0x00,1,546,547,0x00,182,174,1024";

        String[] arr = new String[4];
        arr[0] = a;
        arr[1] = b;
        arr[2] = c;
        arr[3] = d;

        execute(arr);*/

        /*double[][] t1 = new double[][]{
                {3,-1,1},
                {-1,3,1},
                {1,1,3}
        };
        double[][] t2 = new double[][]{
                {2,-1,1},
                {-1,2,1},
                {1,1,2}
        };
        double[][] t3 = new double[][]{
                {2,0,1},
                {0,2,1},
                {1,1,2}
        };



        DoubleMatrix m1 = new DoubleMatrix(t1);
        DoubleMatrix m2 = new DoubleMatrix(t2);
        DoubleMatrix m3 = new DoubleMatrix(t3);


        DoubleMatrix e1 = new DoubleMatrix(new double[] {2,0,1});
        e1 = normalizeLevel2(e1);
        System.out.println("normalized e1:"  + e1);
        DoubleMatrix e2 = findEigenvector(m2);
        DoubleMatrix e3 = findEigenvector(m3);

        DoubleMatrix vectorMatrix = e1.concatHorizontally(e1, e1);
        vectorMatrix = vectorMatrix.concatHorizontally(vectorMatrix,e1);
        System.out.println("vector matrix U(t): " + vectorMatrix);
        System.out.println("rows: " + vectorMatrix.rows + " cols: " + vectorMatrix.columns);
        System.out.println();

        //principal left singular vector
        DoubleMatrix PLSV = svdTester(vectorMatrix);
        PLSV = normalizeLevel2(PLSV);
        System.out.println("normalized plsv: " + PLSV);
        System.out.println("transpose(r(t-1)) x u(t): " + PLSV.transpose().mmul(e1).toString());

        //windowMatrixTester();*/

        //treeMapTest();
        int count = 0;
        int anomalyCount = 0;
        int notAnomalyCount = 0;

        for(int i = 0; i < 1000; i++) {
            //System.out.println(i);
            if(anomalyDetectionTest(0.05, 5, 1, firstMoment, secondMoment, 0.01))
                anomalyCount++;
            else
                notAnomalyCount++;
        }
        System.out.println("anomalies: " + anomalyCount);
        System.out.println("not anomalies: " + notAnomalyCount);

    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private static boolean anomalyDetectionTest(double pc, int windowSize, double deltaTime, double oldFirstMoment, double oldSecondMoment, double dissimilarity)
    {
        double beta = deltaTime / windowSize;
        //double beta = 1;
        double newFirstMoment = ((1 - beta)*oldFirstMoment) + (beta*dissimilarity);
        double newSecondMoment = ((1 - beta)*oldSecondMoment) + (beta*Math.pow(dissimilarity, 2));
        double nMinusOne = (2 * Math.pow(newFirstMoment, 2)) / (newSecondMoment - Math.pow(newFirstMoment, 2));
        double sigma = round((newSecondMoment - Math.pow(newFirstMoment, 2))/(2*newFirstMoment), 5);


        double shape = round(nMinusOne / 2, 5);
        double scale = round(2 * sigma, 5);

        GammaDistribution gamma = new GammaDistribution(shape, scale);

        double zth = gamma.inverseCumulativeProbability(pc);
        boolean test = (pc == gamma.cumulativeProbability(0, zth));


        firstMoment = round(newFirstMoment, 5);
        secondMoment = round(newSecondMoment, 5);

        /*System.out.println("beta: " + beta);
        System.out.println("newFirstMoment: " + newFirstMoment);
        System.out.println("newSecondMoment: " + newSecondMoment);
        System.out.println("nMinusOne: " + nMinusOne);
        System.out.println("sigma: " + sigma);
        System.out.println("shape: " + shape);
        System.out.println("scale: " + scale);
        System.out.println("zth: " + zth);
        System.out.println("pc: " + pc + ", " + round(gamma.cumulativeProbability(0, zth), 5));
        System.out.println("difference: " + (dissimilarity - zth));
        System.out.println();*/

        System.out.println(beta + ", " + newFirstMoment + ", " + newSecondMoment + ", " + sigma + ", " + shape + ", " + scale + ", " + zth + ", " + (dissimilarity - zth));
        return (dissimilarity > zth);

        //System.out.println((dissimilarity > zth)? "anomaly" : "not anomaly");*/

    }


    private static void treeMapTest()
    {
        Map<String, Integer> IPtoIDMap = new HashMap<String, Integer>();
        IPtoIDMap.put("a", 0);
        IPtoIDMap.put("b", 1);
        IPtoIDMap.put("c", 2);
        IPtoIDMap.put("d", 3);

        Map<Integer, String> IDtoIPMap = new TreeMap<Integer, String>();
        for (Map.Entry<String, Integer> entry : IPtoIDMap.entrySet()) {
            IDtoIPMap.put(entry.getValue(), entry.getKey());
        }

        com.google.common.collect.Multiset<String> bag = com.google.common.collect.HashMultiset.create();
        //bag.add("x");
        //bag.add("y");
        //bag.add("x");
        bag.add("z");
        bag.add("z");
        bag.add("z");

        //bag.remove("a", bag.count("a"));
        Multiset<String> highCountFirst = Multisets.copyHighestCountFirst(bag);

        System.out.println(IPtoIDMap);
        System.out.println(IDtoIPMap);
        System.out.println(bag);

        double[][] t3 = new double[][]{
                {2,     0,      0.2,    0},
                {0,     2,      0,      0.1},
                {0.2,   0,      0.5,    0.2},
                {0,     0.1,    0.2,    0.4}
        };
        DoubleMatrix mat = new DoubleMatrix(t3);
        System.out.println(mat);

        for (int i = 0; i < mat.length; i++){
            if(mat.get(i) > 1)
                mat.put(i, (double) Math.log(mat.get(i)));
        }

        System.out.println(mat);


       //sets column and row i to zeros if their sum is below a threshold
        int replaceThreshold = 1;
        Iterator<Multiset.Entry<String>> bagIterator = highCountFirst.entrySet().iterator();

        DoubleMatrix rowSums = mat.rowSums();
        for(int i = 0; i < rowSums.getRows(); i++)
        {
            if(bagIterator.hasNext() && rowSums.get(i) < replaceThreshold)
            {
                //clear row and columns in dependency matrix
                DoubleMatrix zeroVector = DoubleMatrix.zeros(rowSums.getRows());
                mat.putRow(i, zeroVector);
                mat.putColumn(i, zeroVector);

                //remap index to new IP
                IPtoIDMap.remove(IDtoIPMap.remove(i));

                //get highest counted not included
                String candidate = bagIterator.next().getElement();
                bag.remove(candidate, bag.count(candidate));

                //replace values in IPtoIDMap and IDtoIPMap
                IPtoIDMap.put(candidate, i);
                IDtoIPMap.put(i, candidate);

            }
        }

        System.out.println("END");
        System.out.println(IPtoIDMap);
        System.out.println(IDtoIPMap);
        System.out.println(bag);
        System.out.println(mat);



       // ValueComparator bvc =  new ValueComparator(map);

        /*Iterable<Multiset.Entry<String>> entriesSortedByCount = Multisets.copyHighestCountFirst(bag).entrySet();
        Iterator iterator = entriesSortedByCount.iterator();
        int i = 0;
        while(iterator.hasNext() && i < 10)
        {
            i++;
            Multiset.Entry<String> entry = (Multiset.Entry<String>)iterator.next();
            System.out.println(entry.getElement() + " count: " + entry.getCount());
        }*/





    }

    //returns principal left singular vector
    private static DoubleMatrix svdTester(DoubleMatrix mat)
    {
        DoubleMatrix[] svd = Singular.fullSVD(mat);

        System.out.println("Singular Value Decomposition");
        for(int i =0; i < svd.length; i++)
        {
            System.out.println("svd["+i+"]: "+svd[i]);
        }
        System.out.println();

        DoubleMatrix singluarValues = Singular.SVDValues(mat);
        System.out.println("singular Values: " + singluarValues);

        int maxColVal = getMaxSingularValueColumnIndex(singluarValues);
        DoubleMatrix PLSV = svd[0].getColumn(maxColVal);

        System.out.println("PLSV: " + PLSV);
        System.out.println("rows: " + PLSV.rows + " cols:" + PLSV.columns);
        return PLSV;


    }

    //returns column index containing max value
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

    private static DoubleMatrix findEigenvector(DoubleMatrix matrix)
    {
        DoubleMatrix vector = getPrincipalEigenvector(matrix);
        DoubleMatrix normVector = normalizeLevel1(vector);
        System.out.println("normalized principal eigenvector (sum of values = 1): " + normVector);
        System.out.println("has rows = " + normVector.rows);
        System.out.println("has columns = " + normVector.columns);
        System.out.println();
        return normVector;

    }

    //Mixture of code from and based on http://www.markhneedham.com/blog/2013/08/05/javajblas-calculating-eigenvector-centrality-of-an-adjacency-matrix/
    private static DoubleMatrix getPrincipalEigenvector(DoubleMatrix matrix){
        int maxIndex = getMaxIndex(matrix);
        ComplexDoubleMatrix eigenVectors = Eigen.eigenvectors(matrix)[0]; //eigenvectors stored as columns in the matrix
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

    //returns eigenvector with absolute values
    private static DoubleMatrix getEigenVector(ComplexDoubleMatrix eigenvector, int columnId)
    {
        ComplexDoubleMatrix column = eigenvector.getColumn(columnId);
        System.out.println("complexDMatrix column: " + column);
        DoubleMatrix vector = new DoubleMatrix(eigenvector.rows);
        int i = 0;

        for(ComplexDouble value : column.toArray())
        {
            vector.put(i++, value.abs());
        }
        System.out.println("absolute value of column: " + vector);
        return vector;
    }

    //normalizes vectors into unit vectors
    private static DoubleMatrix normalizeLevel1(DoubleMatrix principalEigenvector)
    {
        double total = vectorSum(principalEigenvector);
        DoubleMatrix normalizedValues = new DoubleMatrix(principalEigenvector.rows);
        int i = 0;
        for (Double aDouble : principalEigenvector.toArray()) {
            normalizedValues.put(i++, (aDouble / total));
        }
        return normalizedValues;
    }

    private static DoubleMatrix normalizeLevel2(DoubleMatrix principalEigenvector)
    {
        double sum = 0;
        for (int i = 0; i < principalEigenvector.length; i++){
            double value = principalEigenvector.toArray()[i];
            value = value * value;
            sum += value;
        }
        double norm = Math.sqrt(sum);

        DoubleMatrix normalizedValues = new DoubleMatrix(principalEigenvector.rows);
        int i =0;
        for (Double aDouble : principalEigenvector.toArray()) {
            normalizedValues.put(i++, Math.abs(aDouble / norm));
        }
        return normalizedValues;
    }

    //returns sum of all elements in a vector
    private static double vectorSum(DoubleMatrix principalEigenvector){
        double total = 0;
        for (Double aDouble : principalEigenvector.toArray()) {
            total += aDouble;
        }
        return total;
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
