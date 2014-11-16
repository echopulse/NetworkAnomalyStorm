package storm.starter;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.Viewer;

import java.util.Map;

public class GraphBolt implements IRichBolt{

    OutputCollector _collector;
    Graph graph;

    @Override
    public void prepare(Map config, TopologyContext topology, OutputCollector collector) {
        _collector = collector;
        graph = new SingleGraph("Title");
    }

    @Override
    public void execute(Tuple tuple) {
        String srcIP = tuple.getString(0);
        String dstIP = tuple.getString(1);

        if(graph.getNode(srcIP) == null) {
            Node n = graph.addNode(srcIP);
            n.setAttribute("ui.label", srcIP);
        }

        if(graph.getNode(dstIP) == null){
            Node n = graph.addNode(dstIP);
            n.setAttribute("ui.label", dstIP);
        }


        String edgeName = srcIP + "->" + dstIP;
        Edge edge = graph.getEdge(edgeName);
        if(edge == null) {
            graph.addEdge(edgeName, srcIP, dstIP);
            edge = graph.getEdge(edgeName);
            edge.addAttribute("count", 1);
            edge.addAttribute("ui.label", 1);

        }
        else
        {
            int currentCount = edge.getAttribute("count");
            currentCount++;
            edge.setAttribute("count", currentCount);
            edge.setAttribute("ui.label", currentCount);
        }


        _collector.ack(tuple);

    }

    @Override
    public void cleanup() {
        graph.display();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}




//Graph graph = new SingleGraph("Graph-Title");
//graph.setAutoCreate(true);