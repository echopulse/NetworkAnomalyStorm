#CumulativeProbability on real data

library("ggplot2")
partA <- read.csv("~/Documents/WordCountStorm/pc005.csv")
partB <- read.csv("~/Documents/WordCountStorm/pc01.csv")
partC <- read.csv("~/Documents/WordCountStorm/pc025.csv")
partA$time <- as.POSIXlt(as.character(partA$Timestamp), format="%H:%M:%S")
partB$time <- as.POSIXlt(as.character(partB$Timestamp), format="%H:%M:%S")
partC$time <- as.POSIXlt(as.character(partC$Timestamp), format="%H:%M:%S")

#timestamp correction
partA$time <- partA$time - 9658161
partB$time <- partB$time - 9658161
partC$time <- partC$time - 9658161

ggplot(partA, aes(x = time, y = Chi-zth)) + 
  geom_line(aes(y = partA$Chi.th, colour = "Chi-zth pc=0.005")) + 
  geom_line(aes(y = partB$Chi.th, colour = "Chi-zth pc=0.01")) + 
  geom_line(aes(y = partC$Chi.th, colour = "Chi-zth pc=0.025")) + 
  geom_line(aes(y = partA$Dissimilarity, colour = "Dissimilarity")) +
  theme(legend.title=element_blank()) + 
  ggtitle("decay=0.25, replace=0.001, W=5, tick=60 seconds")


#Histogram
require(MASS)
dissimilarity <- partA$Dissimilarity
hist(dissimilarity, breaks=50, xlab="Dissimilarity", ylab="Frequency")

#chi-squared curve fit
chiFit <- fitdistr(dissimilarity, "chi-squared", start=list(df=0.03))
curve( dchisq(x, df=chiFit$estimate), col='red', add=TRUE)



##WINDOW TEST

library("ggplot2")
partA <- read.csv("~/Documents/WordCountStorm/dataOutput/windowTest1.csv")
partB <- read.csv("~/Documents/WordCountStorm/dataOutput/windowTest2.csv")
partC <- read.csv("~/Documents/WordCountStorm/dataOutput/windowTest3.csv")
partD <- read.csv("~/Documents/WordCountStorm/dataOutput/windowTest4.csv")
partA$time <- as.POSIXlt(as.character(partA$Timestamp), format="%H:%M:%S")
partB$time <- as.POSIXlt(as.character(partB$Timestamp), format="%H:%M:%S")
partC$time <- as.POSIXlt(as.character(partC$Timestamp), format="%H:%M:%S")
partD$time <- as.POSIXlt(as.character(partD$Timestamp), format="%H:%M:%S")
partA$var <- "W=10"
partB$var <- "W=7"
partC$var <- "W=5"
partD$var <- "W=3"
df = rbind(partA, partB, partC, partD)
ggplot(df, aes(x = time, y = Threshold, color = var)) + geom_line() + 
  theme(legend.title=element_blank()) + 
  ggtitle("pc=0.05, replace=0, training time=5, tick=1 seconds")

#DECAY TEST

partA <- read.csv("~/Documents/WordCountStorm/dataOutput/decayTest1.csv")
partB <- read.csv("~/Documents/WordCountStorm/dataOutput/decayTest2.csv")
partC <- read.csv("~/Documents/WordCountStorm/dataOutput/decayTest3.csv")
partD <- read.csv("~/Documents/WordCountStorm/dataOutput/decayTest4.csv")
partA$time <- as.POSIXlt(as.character(partA$Timestamp), format="%H:%M:%S")
partB$time <- as.POSIXlt(as.character(partB$Timestamp), format="%H:%M:%S")
partC$time <- as.POSIXlt(as.character(partC$Timestamp), format="%H:%M:%S")
partD$time <- as.POSIXlt(as.character(partD$Timestamp), format="%H:%M:%S")
partA$var <- "d=1"
partB$var <- "d=0.75"
partC$var <- "d=0.5"
partD$var <- "d=0.25"

ggplot(partA, aes(x = time, y = Threshold)) + 
  geom_line(aes(y = partA$Threshold, colour = "thresh(d=1)")) + 
  geom_line(aes(y = partB$Threshold, colour = "thresh(d=0.75)")) + 
  geom_line(aes(y = partC$Threshold, colour = "thresh(d=0.5)")) + 
  geom_line(aes(y = partD$Threshold, colour = "thresh(d=0.25)")) +
  geom_line(aes(y = partA$Dissimilarity, colour = "diss(d=1)"), linetype="dashed") +  
  geom_line(aes(y = partB$Dissimilarity, colour = "diss(d=0.75)"), linetype="dashed") + 
  geom_line(aes(y = partC$Dissimilarity, colour = "diss(d=0.7)"), linetype="dashed") + 
  geom_line(aes(y = partD$Dissimilarity, colour = "diss(d=0.25)"), linetype="dashed") + 
  theme(legend.title=element_blank()) + 
  ggtitle("replace=0, trainTime=5 window=5, tick=1 second")

