##########WINDOW TEST

library("ggplot2")
partA <- read.csv("~/Documents/WordCountStorm/dataOutput/Tester-window3.csv")
partB <- read.csv("~/Documents/WordCountStorm/dataOutput/Tester-window5.csv")
partC <- read.csv("~/Documents/WordCountStorm/dataOutput/Tester-window7.csv")
partD <- read.csv("~/Documents/WordCountStorm/dataOutput/Tester-window10.csv")
partA$time <- as.POSIXlt(as.character(partA$Timestamp), format="%H:%M:%S")
partB$time <- as.POSIXlt(as.character(partB$Timestamp), format="%H:%M:%S")
partC$time <- as.POSIXlt(as.character(partC$Timestamp), format="%H:%M:%S")
partD$time <- as.POSIXlt(as.character(partD$Timestamp), format="%H:%M:%S")

partA$time <- partA$time - 32
partB$time <- partB$time - 32
partC$time <- partC$time - 32
partD$time <- partD$time - 32

partA$var <- "W=3" 
partB$var <- "W=5"
partC$var <- "W=7" 
partD$var <- "W=10" #1 minute
df = rbind(partA, partB, partC, partD)
ggplot(df, aes(x = time, y = Threshold, color = var)) + 
  geom_line() + 
  geom_vline(xintercept = as.numeric(1426386610), color = "red", linetype="dotted") + 
  geom_vline(xintercept = as.numeric(1426386620), color = "green", linetype="dotted") + 
  theme(legend.title=element_blank()) + 
  ggtitle("Window Size Experiment\nfixed variables: cum.prob. = 0.05, decay = 1, replace threshold = 0, tick = 1s")


###########DECAY TEST

partA <- read.csv("~/Documents/WordCountStorm/dataOutput/Tester-Decay10.csv")
partB <- read.csv("~/Documents/WordCountStorm/dataOutput/Tester-Decay7.csv")
partC <- read.csv("~/Documents/WordCountStorm/dataOutput/Tester-Decay5.csv")
partD <- read.csv("~/Documents/WordCountStorm/dataOutput/Tester-Decay3.csv")
partA$time <- as.POSIXlt(as.character(partA$Timestamp), format="%H:%M:%S")
partB$time <- as.POSIXlt(as.character(partB$Timestamp), format="%H:%M:%S")
partC$time <- as.POSIXlt(as.character(partC$Timestamp), format="%H:%M:%S")
partD$time <- as.POSIXlt(as.character(partD$Timestamp), format="%H:%M:%S")
partA$var <- "d=1"
partB$var <- "d=0.75"
partC$var <- "d=0.5"
partD$var <- "d=0.25"

partA$time <- partA$time - 8
partB$time <- partB$time - 8
partC$time <- partC$time - 8
partD$time <- partD$time - 8

ggplot(partA, aes(x = time, y = Threshold)) + 
  geom_line(aes(y = partA$Threshold, colour = "d = 1.0")) + 
  geom_line(aes(y = partB$Threshold, colour = "d = 0.75")) + 
  geom_line(aes(y = partC$Threshold, colour = "d = 0.5")) + 
  geom_line(aes(y = partD$Threshold, colour = "d = 0.25")) +
  geom_line(aes(y = partA$Dissimilarity, colour = "diss(d=1)"), linetype="dashed") +  
  #geom_line(aes(y = partB$Dissimilarity, colour = "diss(d=0.75)"), linetype="dashed") + 
  #geom_line(aes(y = partC$Dissimilarity, colour = "diss(d=0.7)"), linetype="dashed") + 
  #geom_line(aes(y = partA$Dissimilarity, colour = "Dissimilarity"), linetype="dashed") + 
  geom_vline(xintercept = as.numeric(1426388050), color = "red", linetype="dotted") + 
  geom_vline(xintercept = as.numeric(1426388060), color = "green", linetype="dotted") + 
  theme(legend.title=element_blank()) + 
  ggtitle("Decay Experiment\nfixed variables: cum.prob. = 0.05, window = 5, replace threshold = 0, tick = 1s")

  #change the part for individual tests
ggplot(partD, aes(x = time, y = Threshold, colour = Anomaly)) +
  geom_point() + 
  geom_vline(xintercept = 1426301657, color = "red", linetype="dotted") +
  geom_vline(xintercept = 1426301667, color = "green", linetype="dotted")


############CUMULATIVE PROBABILITY TEST
#off: 1426301836
#on: 1426301846

partA <- read.csv("~/Documents/WordCountStorm/dataOutput/Tester-cumProb20.csv")
partB <- read.csv("~/Documents/WordCountStorm/dataOutput/Tester-cumProb10.csv")
partC <- read.csv("~/Documents/WordCountStorm/dataOutput/Tester-cumProb05.csv")
partD <- read.csv("~/Documents/WordCountStorm/dataOutput/Tester-cumProb025.csv")
partA$time <- as.POSIXlt(as.character(partA$Timestamp), format="%H:%M:%S")
partB$time <- as.POSIXlt(as.character(partB$Timestamp), format="%H:%M:%S")
partC$time <- as.POSIXlt(as.character(partC$Timestamp), format="%H:%M:%S")
partD$time <- as.POSIXlt(as.character(partD$Timestamp), format="%H:%M:%S")

partA$time <- partA$time - 6
partB$time <- partB$time - 6
partC$time <- partC$time - 6
partD$time <- partD$time - 6

ggplot(partA, aes(x = time, y = Chi-zth)) + 
  geom_line(aes(y = partA$Threshold, colour = "Pc = 0.20")) + 
  geom_line(aes(y = partB$Threshold, colour = "Pc = 0.10")) + 
  geom_line(aes(y = partC$Threshold, colour = "Pc = 0.05")) + 
  geom_line(aes(y = partD$Threshold, colour = "Pc = 0.025")) + 
  geom_line(aes(y = partA$Dissimilarity, colour = "Dissimilarity"), linetype = "dashed") +
  theme(legend.title=element_blank()) + 
  ggtitle("Cumulative Probability Experiment\nfixed variables: decay = 1, window = 5, replace threshold = 0, tick = 1s")


ggplot(partA, aes(x = time, y = Threshold, colour = Anomaly)) +
  geom_point() + 
  geom_vline(xintercept = 1426388230, color = "red", linetype="dotted") +
  geom_vline(xintercept = 1426388240, color = "green", linetype="dotted")

