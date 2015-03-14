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

        //TESTING DATA -- REMEMBER TO CHANGE THE STREAMS
      //Shared Parameters
      int tickFrequency = 1; //matrix refresh rate in seconds

      //DependencyMatrixBolt
      double decay = 1;                 //0-1, 0 = no decay, 1 = complete substitution of matrix at each tick
      int trainingTime = 5;               //multiples of tickFrequency
      double replaceThreshold = 0;        //suggest: decay ^ windowSize
      int trainingThreshold = 0;         //tuples with count below threshold during training are ignored

      //AnomalyDetectionBolt
      int windowSize = 5;                 //MUST BE GREATER THAN 1
      double cumulativeProbabilityThreshold = 0.05;


     //LIVE DATA -- REMEMBER TO CHANGE THE STREAMS
    /*//Shared Parameters
    int tickFrequency = 20; //matrix refresh rate in seconds

    //DependencyMatrixBolt
    double decay = 1;                 //0-1, 0 = no decay, 1 = complete substitution of matrix at each tick
    int trainingTime = 9;               //multiples of tickFrequency
    double replaceThreshold = 1;        //suggest: decay ^ windowSize
    int trainingThreshold = 10;         //tuples with count below threshold during training are ignored

    //AnomalyDetectionBolt
    int windowSize = 6;                 //MUST BE GREATER THAN 1
    double cumulativeProbabilityThreshold = 0.05;*/
    String outputFileName = "Tester-cumProb";
    String outputPath = "./dataOutput/";



    TopologyBuilder builder = new TopologyBuilder();

    //Spout
    builder.setSpout("spout", new SocketSpout());

    //Split
    builder.setBolt("split", new SplitBolt(), 3).shuffleGrouping("spout");

    //decay
    //d = 1
    builder.setBolt("matrix-1", new DependencyMatrixBolt(tickFrequency, decay, trainingTime, replaceThreshold, trainingThreshold), 1).shuffleGrouping("split", "SubnetStream");
    builder.setBolt("anomaly-1", new AnomalyDetectionBolt(windowSize, tickFrequency, 0.2), 1).shuffleGrouping("matrix-1", "EigenStream");
    builder.setBolt("printer-1", new PrinterBolt(outputPath + outputFileName + "20.csv"), 1).shuffleGrouping("anomaly-1", "AnomaliesStream");

    //d = .75
    builder.setBolt("matrix-2", new DependencyMatrixBolt(tickFrequency, decay, trainingTime, replaceThreshold, trainingThreshold), 1).shuffleGrouping("split", "SubnetStream");
    builder.setBolt("anomaly-2", new AnomalyDetectionBolt(windowSize, tickFrequency, 0.1), 1).shuffleGrouping("matrix-2", "EigenStream");
    builder.setBolt("printer-2", new PrinterBolt(outputPath + outputFileName + "10.csv"), 1).shuffleGrouping("anomaly-2", "AnomaliesStream");

    //d = .5
    builder.setBolt("matrix-3", new DependencyMatrixBolt(tickFrequency, decay, trainingTime, replaceThreshold, trainingThreshold), 1).shuffleGrouping("split", "SubnetStream");
    builder.setBolt("anomaly-3", new AnomalyDetectionBolt(windowSize, tickFrequency, 0.05), 1).shuffleGrouping("matrix-3", "EigenStream");
    builder.setBolt("printer-3", new PrinterBolt(outputPath + outputFileName + "05.csv"), 1).shuffleGrouping("anomaly-3", "AnomaliesStream");

    //d = .25
    builder.setBolt("matrix-4", new DependencyMatrixBolt(tickFrequency, decay, trainingTime, replaceThreshold, trainingThreshold), 1).shuffleGrouping("split", "SubnetStream");
    builder.setBolt("anomaly-4", new AnomalyDetectionBolt(windowSize, tickFrequency, 0.025), 1).shuffleGrouping("matrix-4", "EigenStream");
    builder.setBolt("printer-4", new PrinterBolt(outputPath + outputFileName + "025.csv"), 1).shuffleGrouping("anomaly-4", "AnomaliesStream");

      /*//Decay
      //d = 1
      builder.setBolt("matrix-5", new DependencyMatrixBolt(tickFrequency, 1, trainingTime, replaceThreshold, trainingThreshold), 1).shuffleGrouping("split", "ServiceStream");
      builder.setBolt("anomaly-5", new AnomalyDetectionBolt(windowSize, tickFrequency, cumulativeProbabilityThreshold), 1).shuffleGrouping("matrix-5", "EigenStream");
      builder.setBolt("printer-5", new PrinterBolt(outputPath + outputFileName + "5.csv"), 1).shuffleGrouping("anomaly-5", "AnomaliesStream");

      //d = 0.75
      builder.setBolt("matrix-6", new DependencyMatrixBolt(tickFrequency, 0.75, trainingTime, replaceThreshold, trainingThreshold), 1).shuffleGrouping("split", "ServiceStream");
      builder.setBolt("anomaly-6", new AnomalyDetectionBolt(windowSize, tickFrequency, cumulativeProbabilityThreshold), 1).shuffleGrouping("matrix-6", "EigenStream");
      builder.setBolt("printer-6", new PrinterBolt(outputPath + outputFileName + "6.csv"), 1).shuffleGrouping("anomaly-6", "AnomaliesStream");

      //d = 0.5
      builder.setBolt("matrix-7", new DependencyMatrixBolt(tickFrequency, 0.5, trainingTime, replaceThreshold, trainingThreshold), 1).shuffleGrouping("split", "ServiceStream");
      builder.setBolt("anomaly-7", new AnomalyDetectionBolt(windowSize, tickFrequency, cumulativeProbabilityThreshold), 1).shuffleGrouping("matrix-7", "EigenStream");
      builder.setBolt("printer-7", new PrinterBolt(outputPath + outputFileName + "7.csv"), 1).shuffleGrouping("anomaly-7", "AnomaliesStream");

      //d = 0.25
      builder.setBolt("matrix-8", new DependencyMatrixBolt(tickFrequency, 0.25, trainingTime, replaceThreshold, trainingThreshold), 1).shuffleGrouping("split", "ServiceStream");
      builder.setBolt("anomaly-8", new AnomalyDetectionBolt(windowSize, tickFrequency, cumulativeProbabilityThreshold), 1).shuffleGrouping("matrix-8", "EigenStream");
      builder.setBolt("printer-8", new PrinterBolt(outputPath + outputFileName + "8.csv"), 1).shuffleGrouping("anomaly-8", "AnomaliesStream");*/


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
      Thread.sleep(60000);
      //Thread.sleep(14400000);
      //cluster.killTopology("word-count");
      cluster.shutdown();

    }
  }
}
