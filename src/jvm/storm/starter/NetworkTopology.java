package storm.starter;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;

/*
* Deals with the configuration of the storm topology
* - provides classes for each worker and defines chains of operation
* - links output streams from one worker to another
* - sets number of threads each worker can have and topology run time
*/
public class NetworkTopology {

  public static void main(String[] args) throws Exception {

      //TESTING DATA SETUP -- REMEMBER TO CHANGE THE STREAMS
      /*//Shared Parameters
      int tickFrequency = 1; //matrix refresh rate in seconds

      //DependencyMatrixBolt
      double decay = 1;                 //0-1, 0 = no decay, 1 = complete substitution of matrix at each tick
      int trainingTime = 5;               //multiples of tickFrequency
      double replaceThreshold = 1;        //suggest: decay ^ windowSize
      int trainingThreshold = 0;         //tuples with count below threshold during training are ignored

      //AnomalyDetectionBolt
      int windowSize = 5;                 //MUST BE GREATER THAN 1
      double cumulativeProbabilityThreshold = 0.05;*/


    //LIVE DATA SETUP -- REMEMBER TO CHANGE THE STREAMS

    //Shared Parameters
    int tickFrequency = 5;         //20    //matrix refresh rate in seconds

    //DependencyMatrixBolt
    double decay = 1;                      //0-1, 0 = no decay, 1 = complete substitution of matrix at each tick
    int trainingTime = 12;         //9     //multiples of tickFrequency
    double replaceThreshold = 1;
    int trainingThreshold = 200;   //10    //matrix size limit

    //AnomalyDetectionBolt
    int windowSize = 12;           //6     //MUST BE GREATER THAN 1
    double cumulativeProbabilityThreshold = 0.20;

    //Graph and PrinterBolt
    String csvFileName = "ITL-MatrixSize-quickTick-1minW";
    String detailsFileName = "ITL-MatrixSize-quickTick-1minW-Detail";
    String outputPath = "./dataOutput/";

    TopologyBuilder builder = new TopologyBuilder();

    //Spout
    builder.setSpout("spout", new SocketSpout());

    //Split
    builder.setBolt("split", new SplitBolt(), 3).shuffleGrouping("spout");

    //Frequency Experiment Chains
    //f = 5, W = 1 min
    builder.setBolt("matrix-1", new DependencyMatrixBolt(tickFrequency, decay, trainingTime, replaceThreshold, trainingThreshold), 1).shuffleGrouping("split", "ITLStream");
    builder.setBolt("anomaly-1", new AnomalyDetectionBolt(windowSize, tickFrequency, cumulativeProbabilityThreshold), 1).shuffleGrouping("matrix-1", "EigenStream");
    builder.setBolt("graph-1", new GraphBolt(false, outputPath + detailsFileName + "5.txt"), 1).shuffleGrouping("matrix-1", "MatrixStream").shuffleGrouping("anomaly-1", "AnomaliesStream");
    builder.setBolt("printer-1", new PrinterBolt(outputPath + csvFileName + "5.csv"), 1).shuffleGrouping("anomaly-1", "AnomaliesStream");

    //f = 10, W = 1 min
    builder.setBolt("matrix-2", new DependencyMatrixBolt(10, decay, 6, replaceThreshold, trainingThreshold), 1).shuffleGrouping("split", "ITLStream");
    builder.setBolt("anomaly-2", new AnomalyDetectionBolt(6, 10, cumulativeProbabilityThreshold), 1).shuffleGrouping("matrix-2", "EigenStream");
    builder.setBolt("graph-2", new GraphBolt(false, outputPath + detailsFileName + "10.txt"), 1).shuffleGrouping("matrix-2", "MatrixStream").shuffleGrouping("anomaly-2", "AnomaliesStream");
    builder.setBolt("printer-2", new PrinterBolt(outputPath + csvFileName + "10.csv"), 1).shuffleGrouping("anomaly-2", "AnomaliesStream");

    //f = 15, W = 1 min
    builder.setBolt("matrix-3", new DependencyMatrixBolt(15, decay, 4, replaceThreshold, trainingThreshold), 1).shuffleGrouping("split", "ITLStream");
    builder.setBolt("anomaly-3", new AnomalyDetectionBolt(4, 15, cumulativeProbabilityThreshold), 1).shuffleGrouping("matrix-3", "EigenStream");
    builder.setBolt("graph-3", new GraphBolt(false, outputPath + detailsFileName + "15.txt"), 1).shuffleGrouping("matrix-3", "MatrixStream").shuffleGrouping("anomaly-3", "AnomaliesStream");
    builder.setBolt("printer-3", new PrinterBolt(outputPath + csvFileName + "15.csv"), 1).shuffleGrouping("anomaly-3", "AnomaliesStream");

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
      //Thread.sleep(60000);
      Thread.sleep(1800000);
      cluster.shutdown();

    }
  }
}
