package storm.starter;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.util.Map;

/**
 * Created by fil on 08/11/14.
 */
public class SplitBolt implements IRichBolt
{
    private OutputCollector _collector;


    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        _collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {
        String str = tuple.getString(0);
        String[] elements = str.split(",");

        String srcIP =  elements[9];
        String dstIP = elements[10];

        srcIP = srcIP.substring(srcIP.length() - 3, srcIP.length());
        dstIP = dstIP.substring(dstIP.length() - 3, dstIP.length());

        _collector.emit(tuple, new Values(srcIP, dstIP));
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("srcIP", "dstIP"));
        //declarer.declareStream("B", new Fields("srcIP", "dstIP"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
