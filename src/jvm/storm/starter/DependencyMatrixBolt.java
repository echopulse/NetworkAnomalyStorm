package storm.starter;

import backtype.storm.Config;
import backtype.storm.Constants;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import org.jblas.ComplexDouble;
import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;
import org.jblas.Eigen;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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
    private Map<Integer, String> IDtoIPMap;
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
                //finding principal eigen vector
                //emitting eigen vector
            }
            else
            {
                tickCount++;
            }

            //decay code
        }
        else
        {
            String srcIP = tuple.getString(0);
            String dstIP = tuple.getString(1);

            //map dimensions grow
            if(tickCount < trainingTime)
            {
                incrementIDMap(srcIP, dstIP);
            }
            //map dimensions do not grow, matrix is built, values are updated
            else
            {
                if(dependencyMatrix == null)
                {
                    buildDependencyMatrix();
                }
                else
                {
                    //update values inside dependency matrix

                }
            }
        }

        _collector.ack(tuple);
    }

    //Assigns IPs a unique int value in the matrixIDMap HashMap for later building the dependency matrix
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

    //Builds dependency matrix
    private void buildDependencyMatrix()
    {
        IDtoIPMap = new TreeMap<Integer, String>();
        for (Map.Entry<String, Integer> entry : IPtoIDMap.entrySet()) {
            IDtoIPMap.put(entry.getValue(), entry.getKey());
        }

//        DEBUG
//        for(Map.Entry<Integer, String> entry : IDtoIPMap.entrySet())
//        {
//            System.out.println("key: " + entry.getKey() + ", value: " + entry.getValue());
//            System.out.println("lastMapValue: " + lastMapValue);
//        }

        dependencyMatrix = new DoubleMatrix(lastMapValue,lastMapValue);
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        Config conf = new Config();
        conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, tickFrequency);
        return conf;
    }
}
