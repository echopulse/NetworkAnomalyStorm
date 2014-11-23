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

        //clears empty cases
        if(srcIP.length() > 1 && dstIP.length() > 1) {
            //extract subnet
            String[] srcBytes = srcIP.split("\\.");
            String[] dstBytes = dstIP.split("\\.");


            String srcSubnet = "";
            String dstSubnet = "";

            //clears IPv6 cases
            if (srcBytes.length > 1 && dstBytes.length > 1) {
                try {
                    int octets = 3;
                    for (int i = 0; i < octets; i++) {
                        srcSubnet += srcBytes[i] + ".";
                        dstSubnet += dstBytes[i] + ".";

                    }
                    srcSubnet = srcSubnet.substring(0, srcSubnet.length() - 1);
                    dstSubnet = dstSubnet.substring(0, dstSubnet.length() - 1);


                    _collector.emit(tuple, new Values(srcSubnet, dstSubnet));
                } catch (ArrayIndexOutOfBoundsException ex) {
                    _collector.emit(tuple, new Values("ex", "ex"));
                }
            }
        }

    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("srcSubnet", "dstSubnet"));
        //declarer.declareStream("B", new Fields("srcIP", "dstIP"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
