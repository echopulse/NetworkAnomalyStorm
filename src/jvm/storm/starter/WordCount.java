package storm.starter;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

/**
 * Created by fil on 08/11/14.
 */
public class WordCount extends BaseBasicBolt {
    Map<String, Integer> counts = new HashMap<String, Integer>();


    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        String srcIP = tuple.getString(0);
        String dstIP = tuple.getString(1);
        String IPs = srcIP+","+dstIP;

        Integer count = counts.get(IPs);
        if (count == null)
            count = 0;
        count++;
        counts.put(IPs, count);

        collector.emit(new Values(IPs, count));

    }

    @Override
    public void cleanup() {
        try{
            PrintWriter file = new PrintWriter(new BufferedWriter(new FileWriter("bolt-output.txt", true)));
            file.println();
            file.println(new Date().toString());
            for(Map.Entry<String, Integer> entry:counts.entrySet()) {
                String result = entry.getKey()+" = " + entry.getValue();
                System.out.println(result);
                file.println(result);
            }
            file.close();
        }
        catch (IOException e) {

        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word", "count"));
    }
}
