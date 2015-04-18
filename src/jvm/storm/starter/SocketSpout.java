package storm.starter;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;

/**
 * Connects to server streaming sFlow data
 */
public class SocketSpout extends BaseRichSpout
{
    SpoutOutputCollector _collector;
    TopologyContext _context;
    Socket sock;
    BufferedReader reader;
    String host;
    int port;

    public SocketSpout(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("line"));
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        _collector = collector;
        _context = context;

        try{
            sock = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        }
        catch(Exception ex)
        {
            System.out.println("Exception in SocketSpout while attempting connection: " + ex.getMessage());
        }

    }

    @Override
    public void nextTuple() {
        Utils.sleep(100);
        try {
            String str;
            while ((str = reader.readLine()) != null) {
                this._collector.emit(new Values(str));
            }
        }
        catch (Exception ex){
            System.out.println("Exception in SocketSpout while reading lines: " + ex.getMessage());
        }
    }

    public void close()
    {
        try {
            reader.close();
            sock.close();
        } catch (IOException ex) {
            System.out.println("Exception in SocketSpout while closing socket or reader: " + ex.getMessage());
        }
    }



}
