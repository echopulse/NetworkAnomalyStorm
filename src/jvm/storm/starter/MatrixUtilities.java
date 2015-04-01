package storm.starter;

import org.jblas.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pseudo-static class containing Tools for managing matrix data
 */
public final class MatrixUtilities {

    private MatrixUtilities()
    {}

    //returns L2-normalized principal left singular vector of a matrix
    public static DoubleMatrix getPLSV(DoubleMatrix mat)
    {
        DoubleMatrix[] svd = Singular.fullSVD(mat);
        DoubleMatrix singluarValues = Singular.SVDValues(mat);
        int maxColVal = getMaxSingularValueColumnIndex(singluarValues);
        DoubleMatrix PLSV = svd[0].getColumn(maxColVal);
        return normalizeLevel2(PLSV);
    }

    //returns column index containing max value
    private static int getMaxSingularValueColumnIndex(DoubleMatrix mat)
    {
        int maxIndex = 0;
        double[] doubleMatrix = mat.toArray();
        for (int i = 0; i < doubleMatrix.length; i++){
            double elementValue = doubleMatrix[i];
            if (elementValue > doubleMatrix[maxIndex]){
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    //Code based on: http://www.markhneedham.com/blog/2013/08/05/javajblas-calculating-eigenvector-centrality-of-an-adjacency-matrix/

    //returns normalized (L2) principal eigenvector
    public static DoubleMatrix getPrincipalEigenvector(DoubleMatrix matrix){
        int maxIndex = getMaxEigenValueIndex(matrix);
        ComplexDoubleMatrix eigenVectors = Eigen.eigenvectors(matrix)[0]; //eigenvectors stored as columns in the matrix
        return normalizeLevel2(getEigenVector(eigenVectors, maxIndex));
    }

    //returns column index corresponding to max eigenvalue
    private static int getMaxEigenValueIndex(DoubleMatrix matrix) {
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

    //returns absolute value eigenvector in a complex matrix at column index
    private static DoubleMatrix getEigenVector(ComplexDoubleMatrix eigenvector, int columnId)
    {
        ComplexDoubleMatrix column = eigenvector.getColumn(columnId);
        DoubleMatrix vector = new DoubleMatrix(eigenvector.rows);
        int i = 0;
        for(ComplexDouble value : column.toArray())
        {
            vector.put(i++, value.abs());
        }
        return vector;
    }

    //normalizes vectors into unit vectors (L1 norm)
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

    //normalizes vectors using euclidian norm (L2 norm)
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

    //returns matrix with values ln(i+1)
    public static DoubleMatrix naturalLogMatrix(DoubleMatrix mat)
    {
        for (int i = 0; i < mat.length; i++){
            mat.put(i, Math.log(mat.get(i) + 1));
        }
        return mat;
    }

    //rounds a double to n places
    public static double roundDouble(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    //sets all values in matrix diagonal to value
    public static DoubleMatrix setDiagonal(DoubleMatrix matrix, double value)
    {
        for(int i = 0; i < matrix.rows; i++)
        {
            matrix.put(i,i,value);
        }
        return matrix;
    }

}
