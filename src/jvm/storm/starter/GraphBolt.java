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

    //debug - checks if all connections are accounted for.
    //int tCount = 0;
    //int connCount = 0;

    @Override
    public void prepare(Map config, TopologyContext topology, OutputCollector collector) {
        _collector = collector;
        graph = new SingleGraph("Title");
    }

    private Node createNode(String nodeName)
    {
        Node node = graph.addNode(nodeName);
        node.setAttribute("ui.label", 0);
        node.setAttribute("internalConnections", 0);
        return node;
    }

    @Override
    public void execute(Tuple tuple) {

        //debug
        //tCount++;

        String srcIP = tuple.getString(0);
        String dstIP = tuple.getString(1);

        Node srcNode = graph.getNode(srcIP);

        if(!srcIP.equals(dstIP)) {
            Node dstNode = graph.getNode(dstIP);

            //Nodes
            if (srcNode == null) {
                srcNode = createNode(srcIP);
            }
            if (dstNode == null) {
                dstNode = createNode(dstIP);
            }

            Edge edge = srcNode.getEdgeBetween(dstNode);
            //Adds edges between nodes if none exist
            if (edge == null)
            {
                edge = graph.addEdge(srcIP + "," + dstIP, srcNode, dstNode);
                edge.addAttribute("count", 1);
                edge.addAttribute("ui.label", 1);

                //debug
                //connCount++;
            }
            //Else increases the edge count property by one.
            else
            {
                int currentCount = edge.getAttribute("count");
                currentCount++;
                edge.setAttribute("count", currentCount);
                edge.setAttribute("ui.label", currentCount);

                //debug
                //connCount++;
            }
        }
        //Internal Connection
        else
        {
            if (srcNode == null) {
                srcNode = createNode(srcIP);
            }
            int currentCount = srcNode.getAttribute("internalConnections");
            currentCount++;
            srcNode.setAttribute("internalConnections", currentCount);
            srcNode.setAttribute("ui.label", currentCount);

            //debug
            //connCount++;
        }

        _collector.ack(tuple);

    }

    @Override
    public void cleanup() {
        //graph.addAttribute("ui.screenshot", "/home/fil/Documents/WordCountStorm/screenshots/graph2.png");
        for(Node n:graph) {
            System.out.println(n.getId() +": "+ n.getAttribute("internalConnections"));
        }
        graph.display();

        //checks if all connections have been accounted for.
        //System.out.println("connCount:" + connCount + "   tCount:" + tCount);
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