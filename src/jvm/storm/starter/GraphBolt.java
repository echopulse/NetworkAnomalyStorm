package storm.starter;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.jblas.DoubleMatrix;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

/*
* Provides detailed output information for each anomaly
* optionally outputs a graph of the current state of the network using the GraphStream library
*/
public class GraphBolt implements IRichBolt{

    OutputCollector _collector;
    DoubleMatrix oldDependencyMatrix;
    DoubleMatrix dependencyMatrix;
    TreeMap<Integer, String> oldMap;
    TreeMap<Integer, String> IDtoIPMap;
    Graph graph;
    Graph oldGraph;
    int matrixID = -1;
    boolean displayGraph;
    String filename;
    PrintWriter writer;

    public GraphBolt(boolean displayGraph, String filename){
        this.displayGraph = displayGraph;
        this.filename = filename;
    }

    @Override
    public void prepare(Map config, TopologyContext topology, OutputCollector collector) {
        _collector = collector;

        if(displayGraph){
            String styleSheet =
                    "node {" +
                            "size: 3px;" +
                            "fill-color: blue;" +
                            "}" +

                            "edge.important {" +
                            "shape: line;" +
                            "fill-color: red;" +
                            "}" +

                            "node.important {" +
                            "fill-color: red;" +
                            "size: 15px;" +
                            "}";


            graph = new SingleGraph("T");
            graph.addAttribute("ui.stylesheet", styleSheet);
            graph.display();
        }

        try{
            writer = new PrintWriter(filename, "UTF-8");
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    private Node createNode(String nodeName)
    {
        Node node = graph.addNode(nodeName);
        node.setAttribute("ui.label", nodeName);
        return node;
    }

    @Override
    public void execute(Tuple tuple){

        if(tuple.getSourceStreamId().equals("MatrixStream")){

            oldDependencyMatrix = dependencyMatrix;
            dependencyMatrix = (DoubleMatrix)tuple.getValue(0);

            oldMap = IDtoIPMap;
            IDtoIPMap = (TreeMap)tuple.getValue(1);

            matrixID = tuple.getInteger(2);

            if(displayGraph) {
                oldGraph = graph;
                graph.clear();

                //add nodes
                for (Map.Entry<Integer, String> entry : IDtoIPMap.entrySet()) {
                    Node node = createNode(entry.getValue());

                    //new node that did not exist in the old graph
                    if (oldGraph.getNode(entry.getValue()) == null) {
                        node.setAttribute("ui.class", "important");
                    }
                }

                //add edges
                for (int i = 0; i < dependencyMatrix.getRows(); i++) {

                    String rowNode = IDtoIPMap.get(i);

                    for (int j = i + 1; j < dependencyMatrix.getColumns(); j++) {

                        String colNode = IDtoIPMap.get(j);
                        double count = dependencyMatrix.get(i, j);
                        if (count > 0) {

                            Edge edge = graph.addEdge(rowNode + "," + colNode, rowNode, colNode);
                            edge.setAttribute("ui.label", (int) count);
                            Edge oldEdge = oldGraph.getEdge(edge.getId());

                            if (oldEdge == null || edge.getAttribute("ui.label") != oldEdge.getAttribute("ui.label")) {
                                edge.setAttribute("ui.class", "important");
                            }
                        }
                    }
                }
            }
        }
        else if(tuple.getSourceStreamId().equals("AnomaliesStream")){
            boolean isAnomaly = tuple.getBoolean(2);
            int anomalyID = tuple.getInteger(3);
            String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());

            if(isAnomaly && matrixID == anomalyID){
                writer.println("\nANOMALY tick: +" + matrixID + "@" + timestamp);
                boolean changes = false;

                for (int i = 0; i < dependencyMatrix.getRows(); i++) {

                    String rowKey= IDtoIPMap.get(i);
                    String changeReport = "no changes";
                    boolean keyChange = false;

                    if(oldMap != null){
                        if(oldMap.get(i) != null && !oldMap.get(i).equals(rowKey)){
                            keyChange = true;
                            changeReport = rowKey + " replaces " + oldMap.get(i);
                        }
                        else if(oldMap.get(i) == null && rowKey != null) {
                            keyChange = true;
                            changeReport = rowKey + " replaces null"; //should never happen
                        }
                    }

                    for (int j = i + 1; j < dependencyMatrix.getColumns(); j++) {

                        String colKey = IDtoIPMap.get(j);

                        double count = dependencyMatrix.get(i, j);
                        double oldCount = oldDependencyMatrix.get(i, j);

                        if (!keyChange && (count != oldCount)) {
                            writer.println(rowKey + "->" + colKey + ": " + count + " (" + (count - oldCount) + ")");
                            changes=true;
                        }
                        else if(keyChange){
                            writer.println(changeReport + ": " + count);
                            changes = true;
                        }
                    }
                }
                if(!changes){
                    writer.println("No changes");
                }
                writer.flush();
            }
        }
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
}