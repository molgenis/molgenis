### R script for generating heatmaps.
# Obligatory parameters: values, rowNames, colNames, wd
# Optional parameters: title

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
		
</#compress>

#################
# GENERATE PLOT #
#################

# set plot destination path
setwd("${wd}")

# make heatmap and store as .svg
svg("${fileName}.svg")

hv <- NULL

# check for column annotations and make colors if there are any
if (length(colAnnotations) != 0){
	color.map <- function(colAnnotations) { if (colAnnotations=="ALL1/AF4") "#FF0000" else "#0000FF" }
	colColors <- unlist(lapply(colAnnotations, color.map))
	
	
	#hv <- heatmap(mat, col=topo.colors(100), ColSideColors = colColors)
	
	hv <- heatmap.2(mat, col=topo.colors(75), scale="none", ColSideColors=colColors,
          key=TRUE, symkey=FALSE, density.info="none", trace="none", cexRow=0.5)
	
	d <- dev.off() #store in d to prevent printing 
}else{
	# removed scale="none" property
	#hv <- heatmap.2(mat, col=topo.colors(75),  
    #     key=TRUE, symkey=FALSE, density.info="none", trace="none", cexRow=0.5)
     
    hv <- heatmap(mat, col=topo.colors(75)) 
          
    d <- dev.off() #store in d to prevent printing 
}

# export data as .csv using the applied reordering done by heatmap() 
# list with row-indexes is reversed because of bottom-to-top build-up of plot
rowInd <- rev(hv$rowInd)
out <- mat[rowInd, hv$colInd]
write.csv(out, file = "${fileName}.csv", quote = TRUE, na = "NA")


# print the col annotations in the reordered state, so it can be parsed in the controller 
# and used to add tooltips in correct order
cat(paste(colAnnotations[hv$colInd],col='\t',sep=''), "\n", sep='')

