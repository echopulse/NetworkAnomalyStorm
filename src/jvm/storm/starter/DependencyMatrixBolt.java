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
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import org.jblas.DoubleMatrix;

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
    private int trainingThreshold = 0;
    private double replaceThreshold = 0;
    private Map<String, Integer> IPtoIDMap = new HashMap<String, Integer>();
    private Map<Integer, String> IDtoIPMap = new TreeMap<Integer, String>();
    private DoubleMatrix dependencyMatrix;
    private Multiset<String> candidateBag = HashMultiset.create();


    public DependencyMatrixBolt(int newTickFrequency, double newDecayVar, int newTrainingTime, double newReplaceThreshold, int newTrainingThreshold)
    {
        tickFrequency = newTickFrequency;
        decayWeight = newDecayVar;
        trainingTime = newTrainingTime;
        replaceThreshold = newReplaceThreshold;
        trainingThreshold = newTrainingThreshold;
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
            //Matrix Review, Value Decay and Eigenvector calculation Phase
            if(tickCount > trainingTime)
            {
                dependencyMatrix = MatrixUtilities.setDiagonal(dependencyMatrix, 0.0);

                //gets L2-normalized principal eigenvector
                DoubleMatrix principalEigenvector = MatrixUtilities.getPrincipalEigenvector(dependencyMatrix);

                //decay dependency matrix values
                dependencyMatrix = (dependencyMatrix.mul(1 - decayWeight));

                //replace decayed services, threshold is sum of service respective row/column in the dependency matrix
                if(replaceThreshold > 0) replaceDecayedIPs(replaceThreshold);

                _collector.emit("EigenStream", new Values(principalEigenvector));
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

            //Training Phase
            if(tickCount <= trainingTime)
            {
                if(trainingThreshold > 0)
                {
                    incrementBags(srcIP, dstIP);
                }
                else
                {
                    incrementMaps(srcIP, dstIP);
                }
            }
            //Matrix Update Phase
            else
            {
                if(dependencyMatrix == null)
                {
                    if(trainingThreshold > 0)
                    {
                        useSortedMapValues(trainingThreshold);
                    }

                    dependencyMatrix = new DoubleMatrix(lastMapValue,lastMapValue).fill(0);
                }

                incrementValues(srcIP, dstIP);
            }
        }

        _collector.ack(tuple);
    }

    //Increments values inside dependency matrix where they exist
    private void incrementValues(String srcIP, String dstIP)
    {

        Integer srcID = IPtoIDMap.get(srcIP);
        Integer dstID = IPtoIDMap.get(dstIP);

        //if id's are already in the matrix, increase the relative count
        if(srcID != null && dstID != null) {

            double countVal = dependencyMatrix.get(srcID, dstID);
            countVal++;
            dependencyMatrix.put(srcID, dstID, countVal);
            dependencyMatrix.put(dstID, srcID, countVal);
        }
        //if IPs are not in the matrix, add them to the multiset bag of candidates
        else
        {
            //candidateBag size set to not exceed 20% of dependency matrix size
            if(candidateBag.size() < (int)(dependencyMatrix.rows * 0.2))
            {
                if(srcID == null)
                    candidateBag.add(srcIP);
                if(dstID == null)
                    candidateBag.add(dstIP);
            }
        }
    }

    //Assigns IPs a unique int value in the IPtoIDMap HashMap for later use in the dependency matrix
    private void incrementMaps(String srcIP, String dstIP)
    {
        if(!srcIP.equals(dstIP)) {
            if(IPtoIDMap.get(srcIP) == null)
            {
                IPtoIDMap.put(srcIP, lastMapValue);
                IDtoIPMap.put(lastMapValue, srcIP);
                lastMapValue++;
            }
            if(IPtoIDMap.get(dstIP) == null)
            {
                IPtoIDMap.put(dstIP, lastMapValue);
                IDtoIPMap.put(lastMapValue, dstIP);
                lastMapValue++;
            }
        }
        else
        {
            if(IPtoIDMap.get(srcIP) == null)
            {
                IPtoIDMap.put(srcIP, lastMapValue);
                IDtoIPMap.put(lastMapValue, srcIP);
                lastMapValue++;
            }
        }
    }

    private void incrementBags(String srcIP, String dstIP)
    {
        candidateBag.add(srcIP);
        candidateBag.add(dstIP);
    }

    //sets dependency matrix creation variables to account for top counted tuples in training
    private void useSortedMapValues(int threshold)
    {
        Multiset<String> highCountFirst = Multisets.copyHighestCountFirst(candidateBag);
        Iterator<Multiset.Entry<String>> bagIterator = highCountFirst.entrySet().iterator();

        int i = 0;
        while(bagIterator.hasNext())
        {
            Multiset.Entry<String> entry = bagIterator.next();
            if(entry.getCount() >= threshold)
            {
                String candidate =  entry.getElement();
                System.out.println(candidate + ": " + entry.getCount());
                IPtoIDMap.put(candidate, i);
                IDtoIPMap.put(i, candidate);
                i++;
            }
        }

        lastMapValue = IPtoIDMap.size();
        candidateBag.clear();

    }

    private void replaceDecayedIPs(double replaceThreshold)
    {
        Multiset<String> highCountFirst = Multisets.copyHighestCountFirst(candidateBag);
        Iterator<Multiset.Entry<String>> bagIterator = highCountFirst.entrySet().iterator();
        DoubleMatrix rowSums = dependencyMatrix.rowSums();

        //sets column and row i to zeros if their sum is below a threshold
        for(int i = 0; i < rowSums.getRows(); i++)
        {
            if(bagIterator.hasNext() && rowSums.get(i) < replaceThreshold)
            {
                //clear row and columns in dependency matrix
                DoubleMatrix zeroVector = DoubleMatrix.zeros(rowSums.getRows());
                dependencyMatrix.putRow(i, zeroVector);
                dependencyMatrix.putColumn(i, zeroVector);

                //remap index to new IP
                IPtoIDMap.remove(IDtoIPMap.remove(i));

                //get highest counted not included
                String candidate = bagIterator.next().getElement();
                candidateBag.remove(candidate, candidateBag.count(candidate));

                //replace values in IPtoIDMap and IDtoIPMap
                IPtoIDMap.put(candidate, i);
                IDtoIPMap.put(i, candidate);

            }
        }
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        //single output
        outputFieldsDeclarer.declareStream("EigenStream", new Fields("eigenField"));

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
}
