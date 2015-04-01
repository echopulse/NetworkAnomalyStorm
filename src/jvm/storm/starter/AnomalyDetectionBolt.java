package storm.starter;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.jblas.DoubleMatrix;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import java.util.Map;

/**
 * Applies anomaly detection algorithm to incoming eigenvector stream
 */
public class AnomalyDetectionBolt implements IRichBolt {

    OutputCollector _collector;
    DoubleMatrix windowMatrix = null;
    int windowSize = 3;
    double firstMoment = 0.0042;
    double secondMoment = 0.0000636;
    double deltaTime = 1;
    double cumulativeProb = 0.05;

    public AnomalyDetectionBolt(int newWindowSize, double newDeltaTime, double newPc)
    {
        windowSize = newWindowSize;
        deltaTime = newDeltaTime;
        cumulativeProb = newPc;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        _collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {

        DoubleMatrix inputVector = (DoubleMatrix) tuple.getValue(0);

        //initializes windowMatrix if null
        if(windowMatrix == null)
        {
            windowMatrix = inputVector;
        }
        else
        {
            if (windowMatrix.columns < windowSize)
            {
                //append new vectors to windowMatrix
                windowMatrix = DoubleMatrix.concatHorizontally(windowMatrix, inputVector);
            }
            else
            {
                //window moves forward, vector position 0 gets deleted, vector in last position is newest vector
                DoubleMatrix newMatrix = windowMatrix.getColumn(1);
                for(int i = 2; i < windowSize; i++)
                {
                    newMatrix = DoubleMatrix.concatHorizontally(newMatrix, windowMatrix.getColumn(i));
                }
                newMatrix = DoubleMatrix.concatHorizontally(newMatrix, inputVector);


               //get L2 normalized principal left singular vector: r(t-1)
                DoubleMatrix plsv = MatrixUtilities.getPLSV(windowMatrix);

                windowMatrix = newMatrix;

                //z(t) = 1 - r(t-1)^T x u(t) where u(t) is the incoming eigenvector
                double dissimilarity = 1 - (plsv.transpose().mmul(inputVector).get(0));

                int matrixID = tuple.getInteger(1); //needed in the GraphBolt

                detectAnomaly(firstMoment, secondMoment, dissimilarity, matrixID);
            }
        }

        _collector.ack(tuple);
    }

    private void detectAnomaly(double oldFirstMoment, double oldSecondMoment, double dissimilarity, int matrixID)
    {
        double beta = deltaTime / (windowSize * deltaTime);
        double firstMoment = ((1 - beta) * oldFirstMoment) + (beta*dissimilarity);
        double secondMoment = ((1 - beta) * oldSecondMoment) + (beta*Math.pow(dissimilarity, 2));
        double nMinusOne = (2 * Math.pow(firstMoment, 2)) / (secondMoment - Math.pow(firstMoment, 2));

        //gets new Chi^2 curve for the current tick.
        ChiSquaredDistribution chi = new ChiSquaredDistribution(nMinusOne);

        double anomalyThreshold = chi.inverseCumulativeProbability(cumulativeProb);

        boolean isChiAnomaly = dissimilarity > anomalyThreshold;

        System.out.println(dissimilarity + ", " + anomalyThreshold + ", " + isChiAnomaly + ", " + nMinusOne);

        //updates moments values
        this.firstMoment = firstMoment;
        this.secondMoment = secondMoment;

        _collector.emit("AnomaliesStream", new Values(dissimilarity, anomalyThreshold, isChiAnomaly, matrixID));

    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

        outputFieldsDeclarer.declareStream("AnomaliesStream", new Fields("dissimilarity", "threshold", "isAnomaly", "matrixID"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
