package storm.starter;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import java.io.PrintWriter;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;

public class PrinterBolt extends BaseRichBolt {

    PrintWriter writer;
    private OutputCollector _collector;
    private String filename;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH:mm:ss");

    public PrinterBolt(String filename)
    {
        this.filename = filename;
    }
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        _collector = outputCollector;
        try{
            writer = new PrintWriter(filename, "UTF-8");
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }

        //CSV header
        writer.println("Timestamp,Dissimilarity,Threshold,Anomaly");
    }
    @Override
    public void execute(Tuple tuple) {
        Date date = new Date();
        String dissimilarity = tuple.getDouble(0).toString();
        String threshold = tuple.getDouble(1).toString();
        String anomaly = tuple.getBoolean(2).toString();

        //System.out.println(dateFormat.format(date) + "," + dissimilarity + "," + threshold + ","+anomaly);
        writer.println(dateFormat.format(date) + "," + dissimilarity + "," +threshold+ ","+anomaly);
        writer.flush();

        _collector.ack(tuple);
    }
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    }
    @Override
    public void cleanup() {
        writer.close();
        super.cleanup();
    }
}
