#ITL-filter, Matrix size 50,100,200  tick=20sec thresh=0.20, W=6, train=9 start at 14:50
library("ggplot2")
partA <- read.csv("/home/fil/Documents/WordCountStorm/dataOutput/ITL-MatrixSize-thresh0.2050.csv")     
partB <- read.csv("/home/fil/Documents/WordCountStorm/dataOutput/ITL-MatrixSize-thresh0.20100.csv")    
partC <- read.csv("/home/fil/Documents/WordCountStorm/dataOutput/ITL-MatrixSize-thresh0.20200.csv")     
partA$time <- as.POSIXlt(as.character(partA$Timestamp), format="%F %H:%M:%S") 
partB$time <- as.POSIXlt(as.character(partB$Timestamp), format="%F %H:%M:%S")
partC$time <- as.POSIXlt(as.character(partC$Timestamp), format="%F %H:%M:%S")

#timestamp correction
partA$time <- partA$time - (1427800424 - 1427208900)
partB$time <- partB$time - (1427800424 - 1427208900)
partC$time <- partC$time - (1427800424 - 1427208900)

ggplot(partC, aes(x = time, y = Threshold)) + 
  #geom_line(aes(y = partA$Threshold, colour = "f = 20")) + 
  #geom_line(aes(y = partB$Threshold, colour = "f = 10")) + 
  geom_line(aes(y = partC$Threshold, colour = "f = 5")) + 
  #geom_line(aes(y = partD$Threshold, colour = "Pc = 0.025")) + 
  #geom_line(aes(y = partC$Dissimilarity, colour = "Dissimilarity"), linetype = "dashed") +
  #geom_vline(xintercept = as.numeric(as.POSIXlt(as.character("2015-02-27 16:42:00 GMT"))), color = "green", linetype="dotted") + 
  #geom_vline(xintercept = as.numeric(as.POSIXlt(as.character("2015-02-27 16:46:00 GMT"))), color = "red", linetype="dotted") + 
  theme(legend.title=element_blank()) + 
  ggtitle("Hadoop Threshold & Dissimilarity\nvariables: decay = 1, window=2min, replace threshold = 1, tick = 20, Pc = 0.05")

ggplot(partC, aes(x = time, y = Threshold, colour = Anomaly)) +
  #geom_line(aes(y = partA$Dissimilarity, colour = "Dissimilarity"), linetype = "dashed", color = "violet") +
  geom_point() +
  #geom_vline(xintercept = as.numeric(as.POSIXlt(as.character("2015-03-24 15:12:00 GMT"))), color = "green", linetype="dotted") + 
  #geom_vline(xintercept = as.numeric(as.POSIXlt(as.character("2015-02-27 16:46:00 GMT"))), color = "red", linetype="dotted") + 
  ggtitle("Anomalies and Thresholds Over Time\nvariables: decay = 100%, window= 2min, replace threshold = 20, tick = 20s, Pc = 0.05")



#ITL-filter, Matrix size 50,100,200  tick=20sec thresh=0.20, W=6, train=9 start at 14:50 + NO REPLACEMENT
library("ggplot2")
partA <- read.csv("/home/fil/Documents/WordCountStorm/dataOutput/ITL-MatrixSize-thresh0.20-noReplace50.csv")     
partB <- read.csv("/home/fil/Documents/WordCountStorm/dataOutput/ITL-MatrixSize-thresh0.20-noReplace100.csv")    
partC <- read.csv("/home/fil/Documents/WordCountStorm/dataOutput/ITL-MatrixSize-thresh0.20-noReplace200.csv")     
partA$time <- as.POSIXlt(as.character(partA$Timestamp), format="%F %H:%M:%S") 
partB$time <- as.POSIXlt(as.character(partB$Timestamp), format="%F %H:%M:%S")
partC$time <- as.POSIXlt(as.character(partC$Timestamp), format="%F %H:%M:%S")

#timestamp correction
partA$time <- partA$time - (1427811209 - 1427208900)
partB$time <- partB$time - (1427811209 - 1427208900)
partC$time <- partC$time - (1427811209 - 1427208900)

ggplot(partB, aes(x = time, y = Threshold)) + 
  #geom_line(aes(y = partA$Threshold, colour = "f = 20")) + 
  #geom_line(aes(y = partB$Threshold, colour = "f = 10")) + 
  geom_line(aes(y = partB$Threshold, colour = "f = 5")) + 
  #geom_line(aes(y = partD$Threshold, colour = "Pc = 0.025")) + 
  #geom_line(aes(y = partC$Dissimilarity, colour = "Dissimilarity"), linetype = "dashed") +
  #geom_vline(xintercept = as.numeric(as.POSIXlt(as.character("2015-02-27 16:42:00 GMT"))), color = "green", linetype="dotted") + 
  #geom_vline(xintercept = as.numeric(as.POSIXlt(as.character("2015-02-27 16:46:00 GMT"))), color = "red", linetype="dotted") + 
  theme(legend.title=element_blank()) + 
  ggtitle("Hadoop Threshold & Dissimilarity\nvariables: decay = 1, window=2min, replace threshold = 1, tick = 20, Pc = 0.05")

ggplot(partA, aes(x = time, y = Threshold, colour = Anomaly)) +
  #geom_line(aes(y = partA$Dissimilarity, colour = "Dissimilarity"), linetype = "dashed", color = "violet") +
  geom_point() +
  geom_vline(xintercept = as.numeric(as.POSIXlt(as.character("2015-03-24 15:12:00 GMT"))), color = "green", linetype="dotted") + 
  #geom_vline(xintercept = as.numeric(as.POSIXlt(as.character("2015-02-27 16:46:00 GMT"))), color = "red", linetype="dotted") + 
  ggtitle("Anomalies and Thresholds Over Time\nvariables: decay = 100%, window= 2min, no replacement, tick = 20s, Pc = 0.20")


#ITL-QuickTick

partA <- read.csv("/home/fil/Documents/WordCountStorm/dataOutput/ITL-MatrixSize-quickTick-1minW5.csv")     
partB <- read.csv("/home/fil/Documents/WordCountStorm/dataOutput/ITL-MatrixSize-quickTick-1minW10.csv")    
partC <- read.csv("/home/fil/Documents/WordCountStorm/dataOutput/ITL-MatrixSize-quickTick-1minW15.csv")     
partA$time <- as.POSIXlt(as.character(partA$Timestamp), format="%F %H:%M:%S") 
partB$time <- as.POSIXlt(as.character(partB$Timestamp), format="%F %H:%M:%S")
partC$time <- as.POSIXlt(as.character(partC$Timestamp), format="%F %H:%M:%S")

#timestamp correction
partA$time <- partA$time - (1427899125 - 1427208840)
partB$time <- partB$time - (1427899125 - 1427208840)
partC$time <- partC$time - (1427899125 - 1427208840)

ggplot(partA, aes(x = time, y = Threshold)) + 
  geom_line(aes(y = partA$Threshold, colour = "f = 20")) + 
  geom_line(aes(y = partB$Threshold, colour = "f = 10")) + 
  geom_line(aes(y = partC$Threshold, colour = "f = 5")) + 
  #geom_line(aes(y = partD$Threshold, colour = "Pc = 0.025")) + 
  #geom_line(aes(y = partB$Dissimilarity, colour = "Dissimilarity"), linetype = "dashed") +
  #geom_vline(xintercept = as.numeric(as.POSIXlt(as.character("2015-02-27 16:42:00 GMT"))), color = "green", linetype="dotted") + 
  #geom_vline(xintercept = as.numeric(as.POSIXlt(as.character("2015-02-27 16:46:00 GMT"))), color = "red", linetype="dotted") + 
  theme(legend.title=element_blank()) + 
  ggtitle("Hadoop Threshold & Dissimilarity\nvariables: decay = 1, window=2min, replace threshold = 1, tick = 20, Pc = 0.05")

ggplot(partA, aes(x = time, y = Threshold, colour = Anomaly)) +
  #geom_point(aes(y = partA$Dissimilarity, colour = "Dissimilarity"), color = "violet") +
  geom_point() +
  #geom_vline(xintercept = as.numeric(as.POSIXlt(as.character("2015-03-24 15:00:02 GMT"))), color = "green", linetype="dotted") + 
  #geom_vline(xintercept = as.numeric(as.POSIXlt(as.character("2015-03-24 15:12:00 GMT"))), color = "green", linetype="dotted") + 
  #geom_vline(xintercept = as.numeric(as.POSIXlt(as.character("2015-02-27 16:46:00 GMT"))), color = "red", linetype="dotted") + 
  ggtitle("Anomalies and Thresholds Over Time\nvariables: decay = 100%, window= 1min, replace threshold = 20, tick = 5s, Pc = 0.20")