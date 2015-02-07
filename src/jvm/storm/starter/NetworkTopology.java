package storm.starter;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;


public class NetworkTopology {

  public static void main(String[] args) throws Exception {

    //SETUP
    /*
        DependencyMatrixBolt ( int tickFrequency, int decay, int training time)
            tickFrequency in seconds
            decay parameter from 0 to 1, 0 meaning no decay, 1 meaning matrix is completely rebuilt
            training time in multiples of tick frequency seconds. if tickFreq is 5 and training time is 2,
            training time is 10 seconds.

        AnomalyDetectionBolt ( int windowSize)
            number of columns in the matrix composed of eigenvectors
    */



    TopologyBuilder builder = new TopologyBuilder();
    //Spout
    builder.setSpout("spout", new SocketSpout());
    //Split
    builder.setBolt("split", new SplitBolt(), 3).shuffleGrouping("spout");
    //Counts
    //builder.setBolt("portCount", new CountBolt(), 3).fieldsGrouping("split", "Ports", new Fields("srcPort"));
    //builder.setBolt("subnetCount", new CountBolt(), 3).fieldsGrouping("split", "Subnets", new Fields("srcSubnet"));
    //Graph
    //builder.setBolt("graph", new GraphBolt(5, 0.5), 1).shuffleGrouping("split", "Subnets");
    //DependencyMatrix
    builder.setBolt("matrix", new DependencyMatrixBolt(1, 0, 5), 1).shuffleGrouping("split", "SubnetStream");
    //Anomaly Detection Bolt
    builder.setBolt("anomaly", new AnomalyDetectionBolt(5), 1).shuffleGrouping("matrix", "EigenStream");

    //Printers
    //builder.setBolt("printzt", new PrinterBolt(), 1).shuffleGrouping("anomaly");
    //builder.setBolt("printPorts", new PrinterBolt(), 1).shuffleGrouping("portCount");
    //builder.setBolt("printNets", new PrinterBolt(), 1).shuffleGrouping("subnetCount");

    Config conf = new Config();
    conf.setDebug(false);



    if (args != null && args.length > 0) {
      conf.setNumWorkers(3);

      StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
    }
    else {
      conf.setMaxTaskParallelism(3);

      StormTopology topology = builder.createTopology();

      LocalCluster cluster = new LocalCluster();
      cluster.submitTopology("word-count", conf, topology);
      Thread.sleep(30000);
      //cluster.killTopology("word-count");
      cluster.shutdown();

    }
  }
}
