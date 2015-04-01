package storm.starter;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import java.util.Map;
/*
* Prepares tuples by filtering spout data for relevant information
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
        //incoming tuple example:
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

            //extract octets
            String[] srcOctets = srcIP.split("\\.");
            String[] dstOctets = dstIP.split("\\.");

            //clears IPv6 cases
            if (srcOctets.length > 1 && dstOctets.length > 1) {

                //emits IP+Port pairs for predefined Hadoop services
                if(isHadoopService(srcPort) || isHadoopService(dstPort))
                {
                    String srcService = srcIP + ":" + srcPort;
                    String dstService = dstIP + ":" + dstPort;
                    _collector.emit("ServiceStream", new Values(srcService, dstService));
                }

                //emits first n IP addresses' octets
                int octets = 3;
                String[] subnets = getSubnets(srcOctets, dstOctets, octets);
                String srcSubnet = subnets[0];
                String dstSubnet = subnets[1];
                _collector.emit("SubnetStream", new Values(srcSubnet, dstSubnet));

                //emits IPs belonging to ITL machines
                if(srcSubnet.equals("138.37.36") || dstSubnet.equals("138.37.36"))
                {
                    _collector.emit("ITLStream", new Values(srcIP, dstIP));
                }
            }
        }

    }

    private String[] getSubnets(String[] srcBytes, String[] dstBytes, int octets)
    {
        String srcSubnet = "";
        String dstSubnet = "";

        for (int i = 0; i < octets; i++) {
            srcSubnet += srcBytes[i] + ".";
            dstSubnet += dstBytes[i] + ".";
        }
        srcSubnet = srcSubnet.substring(0, srcSubnet.length() - 1);
        dstSubnet = dstSubnet.substring(0, dstSubnet.length() - 1);

        return new String[] {srcSubnet, dstSubnet};
    }

    private boolean isHadoopService(String port)
    {
        int[] hadoopPorts = {
                //1024,   //EECSBackup
                50010,  //HadoopDataXceiver
                50075,  //HadoopDNWeb
                54310,  //HadoopHDFS
                50070,  //HadoopNNWeb
                50090,  //HadoopNN2Web
                8485,   //HadoopJournalNode
                50105,  //HadoopCheckpoint
                8021,   //HadoopMRJobTracker
                50030,  //HadoopMRJobTrackerWeb
                8030, 8031, 8032, 8033, //HadoopYARNResourceManager
                8088,   //HadoopYARNResourceManagerWeb
                8040, 8041, 8042,   //HadoopNodeManager
                10020, 19888, //HadoopMRJobHistory
                2181,    //HadoopZookeeper
                7077,    //Spark
        };

        for (int hadoopPort : hadoopPorts) {
            if (Integer.parseInt(port) == hadoopPort)
                return true;
        }
        return false;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream("SubnetStream", new Fields("srcSubnet", "dstSubnet"));
        declarer.declareStream("ServiceStream", new Fields("srcService", "dstService"));
        declarer.declareStream("ITLStream", new Fields("srcIP", "dstIP"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
