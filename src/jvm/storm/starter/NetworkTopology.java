package storm.starter;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;


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

        //Shared Parameters
        int tickFrequency = 1; //matrix refresh rate in seconds

        //DependencyMatrixBolt
        double decay = 1;                 //0-1, 0 = no decay, 1 = complete substitution of matrix at each tick
        int trainingTime = 5;               //multiples of tickFrequency
        double replaceThreshold = 0;        //suggest: decay ^ windowSize
        int trainingThreshold = 0;         //tuples with count below threshold during training are ignored

        //AnomalyDetectionBolt
        int windowSize = 5;                 //multiples of tickFrequency
        double cumulativeProbabilityThreshold = 0.05;



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

    //W = 5
    builder.setBolt("matrix-005", new DependencyMatrixBolt(tickFrequency, decay, trainingTime, replaceThreshold, trainingThreshold), 1).shuffleGrouping("split", "SubnetStream");
    builder.setBolt("anomaly-005", new AnomalyDetectionBolt(5, tickFrequency, cumulativeProbabilityThreshold), 1).shuffleGrouping("matrix-005", "EigenStream");
    builder.setBolt("printer-005", new PrinterBolt("w5test.csv"), 1).shuffleGrouping("anomaly-005", "AnomaliesStream");

    //W = 7
    builder.setBolt("matrix-01", new DependencyMatrixBolt(tickFrequency, decay, trainingTime, replaceThreshold, trainingThreshold), 1).shuffleGrouping("split", "SubnetStream");
    builder.setBolt("anomaly-01", new AnomalyDetectionBolt(7, tickFrequency, cumulativeProbabilityThreshold), 1).shuffleGrouping("matrix-01", "EigenStream");
    builder.setBolt("printer-01", new PrinterBolt("w7test.csv"), 1).shuffleGrouping("anomaly-01", "AnomaliesStream");

    //W = 10
    builder.setBolt("matrix-025", new DependencyMatrixBolt(tickFrequency, decay, trainingTime, replaceThreshold, trainingThreshold), 1).shuffleGrouping("split", "SubnetStream");
    builder.setBolt("anomaly-025", new AnomalyDetectionBolt(10, tickFrequency, cumulativeProbabilityThreshold), 1).shuffleGrouping("matrix-025", "EigenStream");
    builder.setBolt("printer-025", new PrinterBolt("w10test.csv"), 1).shuffleGrouping("anomaly-025", "AnomaliesStream");


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
      Thread.sleep(45000);
      //Thread.sleep(14400000);
      //cluster.killTopology("word-count");
      cluster.shutdown();

    }
  }
}
