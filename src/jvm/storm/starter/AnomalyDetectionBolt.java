package storm.starter;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.jblas.DoubleMatrix;

import java.util.Map;

/**
 * Created by fil on 01/02/15.
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
    //Vectors are entering at tick speed defined in DependencyMatrixBolt so there is no need for it here.

        DoubleMatrix inputVector = (DoubleMatrix) tuple.getValue(0);
        //System.out.println("inputVector:" + inputVector);

        //initialize windowMatrix if null
        if(windowMatrix == null)
        {
            windowMatrix = inputVector;
        }
        else
        {
            if (windowMatrix.columns < windowSize)
            {
                //append new vectors to windowMatrix
                windowMatrix = windowMatrix.concatHorizontally(windowMatrix, inputVector);
            }
            else
            {
                //window moves forward, vector position 0 gets deleted, vector in last position is latest vector
                DoubleMatrix newMatrix = windowMatrix.getColumn(1);
                for(int i = 2; i < windowSize; i++)
                {
                    newMatrix = newMatrix.concatHorizontally(newMatrix, windowMatrix.getColumn(i));
                }
                newMatrix = newMatrix.concatHorizontally(newMatrix, inputVector);


               //get L2 normalized principal left singular vector
                DoubleMatrix plsv = MatrixUtilities.getPLSV(windowMatrix);

                windowMatrix = newMatrix;
                //z(t) = 1 - r(t-1)^T x u(t)
                //_collector.emit("plsv", new Values(plsv.transpose().mmul(inputVector).toString()));
                double dissimilarity = 1 - (plsv.transpose().mmul(inputVector).get(0));

                //Anomaly Detection Module
                detectAnomaly(cumulativeProb, windowSize, deltaTime, firstMoment, secondMoment, dissimilarity);

            }
        }

        _collector.ack(tuple);
    }

    private void detectAnomaly(double pc, int windowSize, double deltaTime, double oldFirstMoment, double oldSecondMoment, double dissimilarity)
    {
        double beta = deltaTime / windowSize;
        double firstMoment = ((1 - beta) * oldFirstMoment) + (beta*dissimilarity);
        double secondMoment = ((1 - beta) * oldSecondMoment) + (beta*Math.pow(dissimilarity, 2));
        double nMinusOne = (2 * Math.pow(firstMoment, 2)) / (secondMoment - Math.pow(firstMoment, 2));
        double sigma = (secondMoment - Math.pow(firstMoment, 2))/(2*firstMoment);

        double scale = 2 * sigma;
        double shape = nMinusOne / 2;

        GammaDistribution gamma = new GammaDistribution(shape, scale);
        org.apache.commons.math3.distribution.ChiSquaredDistribution chi = new org.apache.commons.math3.distribution.ChiSquaredDistribution(nMinusOne);

        double chizth = chi.inverseCumulativeProbability(pc);
        double zth = gamma.inverseCumulativeProbability(pc);

        boolean isAnomaly = dissimilarity > zth;
        boolean isChi = dissimilarity > chizth;

        //System.out.println(beta + ", " + firstMoment + ", " + secondMoment + ", " + sigma + ", " + shape + ", " + scale + ", " + dissimilarity + ", " + zth + ", " + (dissimilarity - zth) + ", " + isAnomaly);
        System.out.println(dissimilarity + ", " + zth + ", " + chizth + ", " + isAnomaly + ", " + isChi + ", " + nMinusOne);

        this.firstMoment = firstMoment;
        this.secondMoment = secondMoment;

    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        //single output
        //outputFieldsDeclarer.declare(new Fields("plsv"));

        //multiple output
        //outputFieldsDeclarer.declareStream("stream1Name", new Fields("fieldName"));
        //outputFieldsDeclarer.declareStream("stream2Name", new Fields("field1", "field2"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
