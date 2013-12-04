### R script for generating heatmaps.
# Obligatory parameters: values, rowNames, colNames, wd
# Optional parameters: title, legend

###################
# LOAD PARAMETERS #
###################
<#compress>
mat <- matrix(
	c(
		<#list chart.data.values as col>
			<#list col as value>
				<#t> <#if value??>${value}<#else>NA</#if><#if value_has_next>,</#if>
			</#list><#if col_has_next>,</#if>
		</#list>
	),
	nrow = ${nRow?c},
	ncol = ${nCol?c},
	byrow = FALSE)

rownames(mat) <- 
	c(
		<#list chart.data.rowTargets as r>
			<#t> "${r.label}"<#if r_has_next>,</#if>
		</#list>
	)

colnames(mat) <- 
	c(
		<#list chart.data.columnTargets as c>
			<#t> "${c.label}"<#if c_has_next>,</#if>
		</#list>
	)


colAnnotations <- 
	c(
		<#if colAnnotations??>
			<#list colAnnotations as ca>
				<#t> "${ca}"<#if ca_has_next>,</#if>
			</#list>
		</#if>
	)
	
rowAnnotations <- 
	c(
		<#if rowAnnotations??>
			<#list rowAnnotations as ra>
				<#t> "${ra}"<#if ra_has_next>,</#if>
			</#list>
		</#if>
	)
	
title <- "<#if title??>${title}</#if>"
	
wd <- "${wd}"

fileName <- "${fileName}"
		
</#compress>


#############
# LIBRARIES #
#############

library(heatmap.plus)

#################
# GENERATE PLOT #
#################

# set plot destination path
setwd(wd)

# make heatmap and store as .svg
svg(paste(fileName, ".svg", sep=""))

hv <- NULL

# check for column annotations and make colors if there are any
if (length(colAnnotations) != 0){
	color.map <- function(colAnnotations) { if (colAnnotations=="ALL1/AF4") "#FF0000" else "#0000FF" }
	colColors <- unlist(lapply(colAnnotations, color.map))
	
	
	#hv <- heatmap(mat, col=topo.colors(100), ColSideColors = colColors)
	
	hv <- heatmap.plus(mat, col=topo.colors(75), scale="none", ColSideColors=colColors)
	
	d <- dev.off() #store in d to prevent printing 
}else{
    hv <- heatmap.plus( 
    	col=topo.colors(75),
    	<#if title??>main="${title}",</#if>
    	mat) 
    
    d <- dev.off() #store in d to prevent printing 
}

# export data as .csv using the applied reordering done by heatmap() 
# list with row-indexes is reversed because of bottom-to-top build-up of plot
rowInd <- rev(hv$rowInd)
out <- mat[rowInd, hv$colInd]
write.csv(out, file = paste(fileName, ".csv", sep=""), quote = TRUE, na = "NA")


# print the col annotations in the reordered state, so it can be parsed in the controller 
# and used to add tooltips in correct order
cat(paste(colAnnotations[hv$colInd],col='\t',sep=''), "\n", sep='')

