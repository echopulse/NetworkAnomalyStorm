package storm.starter;

import backtype.storm.Config;
import backtype.storm.Constants;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import org.jblas.DoubleMatrix;
import org.jblas.Singular;

import java.util.Map;

/**
 * Created by fil on 01/02/15.
 */
public class AnomalyDetectionBolt implements IRichBolt {

    OutputCollector _collector;
    DoubleMatrix windowMatrix = null;
    int windowSize = 3;
    int tickFrequency = 0;

    public AnomalyDetectionBolt(int newTickFrequency, int newWindowSize)
    {
        windowSize = newWindowSize;
        tickFrequency = newTickFrequency;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        _collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
    //Vectors are entering at tick speed defined in DependencyMatrixBolt so there is no need for it here.

        DoubleMatrix inputVector = (DoubleMatrix) tuple.getValue(0);

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
                windowMatrix = newMatrix;

               //get L2 normalized principal left singular vector
                DoubleMatrix plsv = MatrixUtilities.getPLSV(windowMatrix);

                //TODO anomaly detection stuff
            }
        }

        _collector.ack(tuple);
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        //single output
        //outputFieldsDeclarer.declare(new Fields("fieldName"));

        //multiple output
        //outputFieldsDeclarer.declareStream("stream1Name", new Fields("fieldName"));
        //outputFieldsDeclarer.declareStream("stream2Name", new Fields("field1", "field2"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
