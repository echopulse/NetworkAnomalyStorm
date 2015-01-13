package storm.starter;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

public class PrinterBolt extends BaseBasicBolt {
    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        System.out.println("output: " + tuple);
    }
    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd) {
    }

    @Override
    public void cleanup(){
        System.out.println("Complete");
    }
}
