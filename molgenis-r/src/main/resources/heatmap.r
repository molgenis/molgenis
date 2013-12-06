#############
# LIBRARIES #
#############
#library(RGraphicsDevice)
library(XML)
library("ALL")
library(limma)
library(Biobase) #else can't find exprs()

########
# DATA #
########
source("http://www.bioconductor.org/biocLite.R")
data("ALL")
biocLite("ALL")
biocLite("Biobase")

########
# CODE #
########
setwd("/Users/erwin/")

# prepare heatmap data, serious statistics happening:
eset <- ALL[ , ALL$mol.biol %in% c("BCR/ABL","ALL1/AF4")]

f <- factor(as.character(eset$mol.biol))
design <- model.matrix(~f)
fit <- eBayes(lmFit(eset, design))

selected <- p.adjust(fit$p.value[, 2]) < 0.05
esetSel <- eset [selected, ]

color.map <- function(mol.biol) { if (mol.biol=="ALL1/AF4") "#FF0000" else "#0000FF" }
patientcolors <- unlist(lapply(esetSel$mol.bio, color.map))

## save heatmap as svg
# CairoSVG(file="heat.svg") # empty file
svg("geneHeat.svg")
hv = heatmap(exprs(esetSel), col=topo.colors(100), ColSideColors=patientcolors)
dev.off()

# csv export:
#reverse row-indexes because of bottom to top build-up of plot
rowInd <- rev(hv$rowInd)
out <- exprs(esetSel)[rowInd, hv$colInd]
write.csv(out, file = "geneHeatData.csv", quote = TRUE, na = "NA")