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
        //Current tuple example:
        //138.37.95.20,1414972694,138.37.88.245,138.37.89.120,2049,808
        //switchIP, timestamp, srcIP, dstIP, srcPort, dstPort

        String str = tuple.getString(0);
        String[] elements = str.split(",");

        //String switchIP = elements[0];
        //String timestamp = elements[1];
        String srcIP =  elements[2];
        String dstIP = elements[3];
        String srcPort = elements[4];
        String dstPort = elements[5];

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


                    _collector.emit("Subnets", new Values(srcSubnet, dstSubnet));
                    _collector.emit("Ports", new Values(srcPort, dstPort));
                } catch (ArrayIndexOutOfBoundsException ex) {
                    _collector.emit("Subnets", new Values("ex", "ex"));
                    _collector.emit("Ports", new Values("ex", "ex"));
                }
            }
        }

    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream("Subnets", new Fields("srcSubnet", "dstSubnet"));
        declarer.declareStream("Ports", new Fields("srcPort", "dstPort"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
