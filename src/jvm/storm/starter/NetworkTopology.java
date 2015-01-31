package storm.starter;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;


public class NetworkTopology {

  public static void main(String[] args) throws Exception {

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
    builder.setBolt("matrix", new DependencyMatrixBolt(5, 0, 2), 1).shuffleGrouping("split", "Subnets");

    //Printers
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
      Thread.sleep(20000);
      //cluster.killTopology("word-count");
      cluster.shutdown();

    }
  }
}
