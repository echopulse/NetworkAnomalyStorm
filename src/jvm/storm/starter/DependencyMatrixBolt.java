package storm.starter;

import backtype.storm.Config;
import backtype.storm.Constants;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.jblas.ComplexDouble;
import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;
import org.jblas.Eigen;

import java.util.*;

/**
 * Created by fil on 22/01/15.
 */
public class DependencyMatrixBolt implements IRichBolt {

    OutputCollector _collector;
    private int tickFrequency = 0;
    private double decayWeight = 0;
    private int tickCount = 0;
    private int trainingTime = 0;
    private int lastMapValue = 0;
    private Map<String, Integer> IPtoIDMap = new HashMap<String, Integer>();
    //private Map<Integer, String> IDtoIPMap;
    private DoubleMatrix dependencyMatrix;


    public DependencyMatrixBolt(int newTickFrequency, double newDecayVar, int newTrainingTime)
    {
        tickFrequency = newTickFrequency;
        decayWeight = newDecayVar;
        trainingTime = newTrainingTime;
    }

    private boolean isTickTuple(Tuple tuple){
        return tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID) &&
                tuple.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID);
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {

        _collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {

        if(isTickTuple(tuple))
        {
            if(tickCount > trainingTime)
            {
                //decay dependency matrix values
                dependencyMatrix = dependencyMatrix.mul(1 - decayWeight);

                //finding and emitting principal eigen vector
                DoubleMatrix principalEigenvector = normalized(getPrincipalEigenvector(dependencyMatrix));
                _collector.emit("EigenVector", new Values(principalEigenvector));
            }
            else
            {
                tickCount++;
            }
        }
        else
        {
            String srcIP = tuple.getString(0);
            String dstIP = tuple.getString(1);

            //map dimensions grow
            if(tickCount <= trainingTime)
            {
                incrementIDMap(srcIP, dstIP);
            }
            //map dimensions do not grow, matrix is built, values are updated
            else
            {
                if(dependencyMatrix == null)
                {
                    dependencyMatrix = new DoubleMatrix(lastMapValue,lastMapValue).fill(0);
                }

                //update values inside dependency matrix where they exist
                Integer srcID = IPtoIDMap.get(srcIP);
                Integer dstID = IPtoIDMap.get(dstIP);

                if(srcID != null && dstID != null) {

                    //TODO consider using ln to normalize values
                    double countVal = dependencyMatrix.get(srcID, dstID);
                    countVal++;
                    dependencyMatrix.put(srcID, dstID, countVal);
                    dependencyMatrix.put(dstID, srcID, countVal);
                }
                else
                {
                    //TODO IPs not found -> emit as anomaly?
                }

            }
        }

        _collector.ack(tuple);
    }

    //Assigns IPs a unique int value in the IPtoIDMap HashMap for later use in the dependency matrix
    private void incrementIDMap(String srcIP, String dstIP)
    {
        if(!srcIP.equals(dstIP)) {
            if(IPtoIDMap.get(srcIP) == null)
            {
                IPtoIDMap.put(srcIP, lastMapValue++);
            }
            if(IPtoIDMap.get(dstIP) == null)
            {
                IPtoIDMap.put(dstIP, lastMapValue++);
            }
        }
        else
        {
            if(IPtoIDMap.get(srcIP) == null)
            {
                IPtoIDMap.put(srcIP, lastMapValue++);
            }
        }
    }

    //Builds dependency matrix filled with 0s
    /*private void buildDependencyMatrix()
    {
        //creates reverse map from ID to IP
        *//*IDtoIPMap = new TreeMap<Integer, String>();
        for (Map.Entry<String, Integer> entry : IPtoIDMap.entrySet()) {
            IDtoIPMap.put(entry.getValue(), entry.getKey());
        }*//*

        dependencyMatrix = new DoubleMatrix(lastMapValue,lastMapValue).fill(0);
        dependencyMatrix.fill(0);
    }*/

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        //single output
        outputFieldsDeclarer.declare(new Fields("eVector"));

        //multiple output
        //outputFieldsDeclarer.declareStream("EigenVector", new Fields("eVector"));
        //outputFieldsDeclarer.declareStream("Anomalies", new Fields("srcSubnet", "dstSubnet"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        Config conf = new Config();
        conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, tickFrequency);
        return conf;
    }

    //Following code is based on http://www.markhneedham.com/blog/2013/08/05/javajblas-calculating-eigenvector-centrality-of-an-adjacency-matrix/

    private DoubleMatrix getPrincipalEigenvector(DoubleMatrix matrix){
        int maxIndex = getMaxIndex(matrix);
        ComplexDoubleMatrix eigenVectors = Eigen.eigenvectors(matrix)[0];
        return getEigenVector(eigenVectors, maxIndex);
    }

    private int getMaxIndex(DoubleMatrix matrix) {
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

    private DoubleMatrix getEigenVector(ComplexDoubleMatrix eigenvector, int columnId)
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

    private DoubleMatrix normalized(DoubleMatrix principalEigenvector)
    {
        double total = sum(principalEigenvector);
        DoubleMatrix normalizedValues = new DoubleMatrix(principalEigenvector.rows);
        int i = 0;
        for (Double aDouble : principalEigenvector.toArray()) {
            normalizedValues.put(i++, (aDouble / total));
        }
        return normalizedValues;
    }

    private double sum(DoubleMatrix principalEigenvector){
        double total = 0;
        for (Double aDouble : principalEigenvector.toArray()) {
            total += aDouble;
        }
        return total;
    }
}
