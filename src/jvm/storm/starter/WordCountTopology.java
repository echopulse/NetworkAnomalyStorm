package storm.starter;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;


public class WordCountTopology {

  public static void main(String[] args) throws Exception {

    TopologyBuilder builder = new TopologyBuilder();

    builder.setSpout("spout", new SocketSpout());
    builder.setBolt("split", new SplitBolt(), 8).shuffleGrouping("spout");
    //fieldsGrouping arguments - group all "srcSubnet" on the same machine.
    builder.setBolt("count", new WordCount(), 12).fieldsGrouping("split", new Fields("srcSubnet"));
    builder.setBolt("graph", new GraphBolt(), 1).shuffleGrouping("split");

    Config conf = new Config();
    conf.setDebug(true);


    if (args != null && args.length > 0) {
      conf.setNumWorkers(3);

      StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
    }
    else {
      conf.setMaxTaskParallelism(3);

      StormTopology topology = builder.createTopology();

      LocalCluster cluster = new LocalCluster();
      cluster.submitTopology("word-count", conf, topology);
      Thread.sleep(3000);
      //cluster.killTopology("word-count");
      cluster.shutdown();

    }
  }
}
