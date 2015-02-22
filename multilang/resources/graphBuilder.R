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



##WINDOW TEST

library("ggplot2")
partA <- read.csv("~/Documents/WordCountStorm/w5test.csv")
partB <- read.csv("~/Documents/WordCountStorm/w7test.csv")
partC <- read.csv("~/Documents/WordCountStorm/w10test.csv")
partA$time <- as.POSIXlt(as.character(partA$Timestamp), format="%H:%M:%S")
partB$time <- as.POSIXlt(as.character(partB$Timestamp), format="%H:%M:%S")
partC$time <- as.POSIXlt(as.character(partC$Timestamp), format="%H:%M:%S")
partA$var <- "W=5"
partB$var <- "W=7"
partC$var <- "W=10"
df = rbind(partA, partB, partC)
ggplot(df, aes(x = time, y = Chi.th, color = var)) + geom_line() + 
  theme(legend.title=element_blank()) + 
  ggtitle("pc=0.05, decay=1, replace=0, W=5, tick=1 seconds")
