package storm.starter;

import backtype.storm.Config;
import backtype.storm.Constants;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.Viewer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class GraphBolt implements IRichBolt{

    OutputCollector _collector;
    Graph graph;
    long startTime;
    int tickFrequency = 0;
    double decayWeight = 0;

//    int tupleCount = 0;
//    int connCount = 0;


    public GraphBolt(int newTickFrequency, double newDecayVar)
    {
        tickFrequency = newTickFrequency;
        decayWeight = newDecayVar;
    }


    @Override
    public void prepare(Map config, TopologyContext topology, OutputCollector collector) {
        _collector = collector;
        graph = new SingleGraph("Title");
        startTime = System.currentTimeMillis() / 1000L;
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        Config conf = new Config();
        conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, tickFrequency);
        return conf;
    }

    private boolean isTickTuple(Tuple tuple){
        return tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID) &&
                tuple.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID);
    }

    private Node createNode(String nodeName)
    {
        Node node = graph.addNode(nodeName);
        node.setAttribute("count", 0.0);
        node.setAttribute("ui.label", 0);
        return node;
    }

    private double[][] fillDependencyMatrix()
    {
        //build new matrix[nXn] where n = number of nodes
        int n = graph.getNodeCount();
        double[][] matrix = new double[n][n];

        for(int i = 0; i < matrix.length; i++) {
            Arrays.fill(matrix[i], 0);
        }

        //Edge counts
        Iterator eIter = graph.getEachEdge().iterator();
        while(eIter.hasNext()) {
            Edge edge = (Edge)eIter.next();
            int i = edge.getSourceNode().getIndex();
            int j = edge.getTargetNode().getIndex();
            double count = edge.getAttribute("count");
            matrix[i][j] = count;
            matrix[j][i] = count;
        }

        //Node counts, or internal network connections, are equivalent to the matrix diagonal
        Iterator nIter = graph.getEachNode().iterator();
        while(nIter.hasNext()){
            Node node = (Node)nIter.next();
            int i = node.getIndex();
            double count = node.getAttribute("count");
            matrix[i][i] = count;
        }
        return matrix;
    }

    @Override
    public void execute(Tuple tuple) {

        if(isTickTuple(tuple)){
            //Edge count decay logic
            for( Edge edge : graph.getEachEdge())
            {
                double count = edge.getAttribute("count");
                count = count * (1 - decayWeight);
                if(count < 1){
                    graph.removeEdge(edge);
                }
                else {
                    edge.setAttribute("count", count);
                    edge.setAttribute("ui.label", (int)count);
                }
            }
            //Node count decay logic
            for(Node node : graph.getEachNode())
            {
                double count = node.getAttribute("count");
                count = count * (1 - decayWeight);
                if(count < 1) {
                    node.setAttribute("count", 0.0);
                    node.setAttribute("ui.label", 0);
                }
                else {
                    node.setAttribute("count", count);
                    node.setAttribute("ui.label", (int)count);
                }

            }

            //Refresh Dependency Matrix
            double[][] matrix = fillDependencyMatrix();

            //calculate principal eigenvalue

        }
        //INCREMENT GRAPH
        else {

            String srcIP = tuple.getString(0);
            String dstIP = tuple.getString(1);

            Node srcNode = graph.getNode(srcIP);

            if (!srcIP.equals(dstIP)) {
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
                if (edge == null) {
                    edge = graph.addEdge(srcIP + "," + dstIP, srcNode, dstNode);
                    edge.addAttribute("count", 1.0);
                    edge.addAttribute("ui.label", 1);

                    //debug
                    //connCount++;
                }
                //Else increases the edge count property by one.
                else {
                    double currentCount = edge.getAttribute("count");
                    currentCount++;
                    edge.setAttribute("count", currentCount);
                    edge.setAttribute("ui.label", (int)currentCount);

                    //debug
                    //connCount++;
                }
            }
            //Internal Connection i.e. within the same subnet
            else {
                if (srcNode == null) {
                    srcNode = createNode(srcIP);
                }
                double currentCount = srcNode.getAttribute("count");
                currentCount++;
                srcNode.setAttribute("count", currentCount);
                srcNode.setAttribute("ui.label", currentCount);

                //debug
                //connCount++;
            }
            _collector.ack(tuple);
        }


    }

    @Override
    public void cleanup() {
        //graph.addAttribute("ui.screenshot", "/home/fil/Documents/WordCountStorm/screenshots/graph2.png");
//        for(Node n:graph) {
//            System.out.println(n.getId() +": "+ n.getAttribute("count"));
//        }
//        graph.display();

        //debug: checks if all connections have been accounted for.
        //System.out.println("connCount:" + connCount + "   tupleCount:" + tupleCount);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
}