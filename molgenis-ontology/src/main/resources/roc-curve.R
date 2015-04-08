biobank.readExcel <- function(file) {
  biobanks <- sheetNames(file);
  aggregateData <- NULL
  for(biobankName in biobanks){
    biobankData <- read.xls(file, sheet=which(biobanks == biobankName))
    group <- rep(which(biobanks==biobankName), nrow(biobankData))
    biobankData <- cbind(biobankData, group)
    aggregateData <- rbind(aggregateData, biobankData)
  }
  return(aggregateData)
}

library(gdata)
library(ggplot2)

inputData <- biobank.readExcel("${filePath}")

legend <- c("1"="blue", "2"="red", "3"="black", "4"="purple1", "5"="green4", "6"="darkblue")
linestyle <- c("1"="solid","2"="longdash", "3"="dotted","4"="F1", "5"="twodash", "6"="1F")
label <- c("roc")

png("${outputFile}", width = 480, height = 480)
ggplot() + ylab("Sensitivity") + xlab("Specificity") + ggtitle("Sensitivity-Specificity") + 
  theme(plot.title = element_text(lineheight=.8, face="bold")) + xlim(0, 1) + ylim(0, 1) +
  geom_path(data=inputData, aes(y = TPR, x=FPR, group=group, linetype=as.factor(group), colour=as.factor(group)), size = 0.5) + 
  geom_point(data=inputData, aes(y = TPR, x=FPR, colour=as.factor(group), shape=as.factor(group))) + 
  scale_colour_manual("legend", values=legend, labels=label) + 
  scale_linetype_manual("legend", values=linestyle, labels=label) +
  scale_shape_manual("legend", values=c(1,1,2,1,1,2), labels=label) + 
  coord_fixed(ratio=1)