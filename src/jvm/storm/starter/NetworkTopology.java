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

    //Shared Parameters
    int tickFrequency = 20;             //matrix refresh rate in seconds

    //DependencyMatrixBolt
    double decay = 1;                   //0-1, 0 = no decay, 1 = complete substitution of matrix at each tick
    int trainingTime = 3;               //multiples of tickFrequency
    double replaceThreshold = 15;
    int matrixSizeLimit = 200;

    //AnomalyDetectionBolt
    int windowSize = 3;
    double cumulativeProbabilityThreshold = 0.20;

    //Graph and PrinterBolt Configuration
    String csvFileName = "ITL-RFilter+MatrixSize";
    String detailsFileName = "ITL-RFilter+MatrixSize-Detail";
    String outputPath = "./dataOutput/";

    TopologyBuilder builder = new TopologyBuilder();

    //Spout
    builder.setSpout("spout", new SocketSpout("Wintermute", 9999));

    //SplitBolt
    builder.setBolt("split", new SplitBolt(), 3).shuffleGrouping("spout");

    //Experiment Bolt Chains
    //Chain 1
    builder.setBolt("matrix-1", new DependencyMatrixBolt(tickFrequency, decay, trainingTime, replaceThreshold, matrixSizeLimit), 1).shuffleGrouping("split", "ITLStream");
    builder.setBolt("anomaly-1", new AnomalyDetectionBolt(windowSize, tickFrequency, cumulativeProbabilityThreshold), 1).shuffleGrouping("matrix-1", "EigenStream");
    builder.setBolt("graph-1", new GraphBolt(false, outputPath + detailsFileName + "25.txt"), 1).shuffleGrouping("matrix-1", "MatrixStream").shuffleGrouping("anomaly-1", "AnomaliesStream");
    builder.setBolt("printer-1", new PrinterBolt(outputPath + csvFileName + "25.csv"), 1).shuffleGrouping("anomaly-1", "AnomaliesStream");

    //Chain 2
    builder.setBolt("matrix-2", new DependencyMatrixBolt(tickFrequency, decay, trainingTime, replaceThreshold, 50), 1).shuffleGrouping("split", "ITLStream");
    builder.setBolt("anomaly-2", new AnomalyDetectionBolt(windowSize, tickFrequency, cumulativeProbabilityThreshold), 1).shuffleGrouping("matrix-2", "EigenStream");
    builder.setBolt("graph-2", new GraphBolt(false, outputPath + detailsFileName + "50.txt"), 1).shuffleGrouping("matrix-2", "MatrixStream").shuffleGrouping("anomaly-2", "AnomaliesStream");
    builder.setBolt("printer-2", new PrinterBolt(outputPath + csvFileName + "50.csv"), 1).shuffleGrouping("anomaly-2", "AnomaliesStream");

    //Chain 3
    builder.setBolt("matrix-3", new DependencyMatrixBolt(tickFrequency, decay, trainingTime, replaceThreshold, 100), 1).shuffleGrouping("split", "ITLStream");
    builder.setBolt("anomaly-3", new AnomalyDetectionBolt(windowSize, tickFrequency, cumulativeProbabilityThreshold), 1).shuffleGrouping("matrix-3", "EigenStream");
    builder.setBolt("graph-3", new GraphBolt(false, outputPath + detailsFileName + "100.txt"), 1).shuffleGrouping("matrix-3", "MatrixStream").shuffleGrouping("anomaly-3", "AnomaliesStream");
    builder.setBolt("printer-3", new PrinterBolt(outputPath + csvFileName + "100.csv"), 1).shuffleGrouping("anomaly-3", "AnomaliesStream");

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
      Thread.sleep(1800000);
      cluster.shutdown();

    }
  }
}
