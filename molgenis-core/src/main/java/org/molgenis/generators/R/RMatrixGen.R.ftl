<#-- we expect $matrix from the java generator program -->
<#include "GeneratorHelper.ftl">

<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
# File:        ${model.getName()}/R/${file}
# Copyright:   GBIC 2000-${year?c}, all rights reserved
# Date:        ${date}
#
# generator:   ${generator} ${version}
#
# This file provides action methods to MOLGENIS for matrix ${Name(matrix)}
#
# THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
#

<#assign row_entity = matrix.getRowEntity()/>
<#assign col_entity = matrix.getColEntity()/>
<#assign container_entity = matrix.getContainerEntity()/>
<#assign content_entity = matrix.getContentEntity()/>
<#assign container_skey_fields = key_fields(skeys(container_entity))>
<#assign row_skey_fields = key_fields(skeys(row_entity))>
<#assign col_skey_fields = key_fields(skeys(col_entity))>

add.${name(matrix)} <- function(.data_matrix, <#list allFields(container_entity) as field>${name(field)}=NULL <#if field_has_next>, </#if></#list>, .usesession=T, .verbose=F)
{    
	starttime=Sys.time()
	
	#check matrix structure
    if(is.null(rownames(.data_matrix)) || is.null(colnames(.data_matrix)) )
    {
        stop("the matrix has to have columnames and rownames")
    } 	
    
	#get session parameters
	<#list container_skey_fields as f><#if f.type == "xref">
   	<#assign session_var = ".MOLGENIS$session." + name(f.getXRefEntity()) + "."+ name(f.getXRefField())>
   	<#assign xref_entity = model.getEntity(f.getXRefEntity())>
   	if(.usesession && is.null(${name(f)}) && !is.null(${session_var}))    
   	{
       	${name(f)} = ${session_var}
       	cat("Using ${name(f)} (${pkeyname(xref_entity)}='",${session_var},"'", sep="")
<#list skeys(xref_entity) as skey><#list key_fields(skey) as f>        
		cat(", ${name(f)}='",.MOLGENIS$session.${name(xref_entity)}.${name(f)},"'", sep="")
</#list></#list>        	 
		cat(") from session (.usession = T).\n")
   	} 
   	</#if></#list>    

    #add container entity '${name(matrix.container)}' 
    container_arguments <-  mget(ls(),environment())
                                       
    #get the ids of the rows
    cat('checking rownames and columnames with database: \n')
    #todo: trim the spaces of rownames, otherwise comparison fails...
    #todo: add the missing parts for this query [or check that this never happens]
    row_labels <- find.${name(row_entity)}( <#list row_skey_fields as f><#if f.type != "xref">${name(f)}=rownames(.data_matrix)<#else>${name(f)}=${name(f)}</#if><#if f_has_next>, </#if></#list>, .verbose=.verbose )   
    missing_row_labels <- setdiff( rownames(.data_matrix), row_labels$<#list row_skey_fields as f><#if f_index == 0>${name(f)}</#if></#list>)
    if(length(missing_row_labels)>0)
    {
        stop("not all ${name(row_entity)} are known in database, missing rows: ", toString(missing_row_labels),". Use add.${name(row_entity)}() to correct.")
    }

    #get the ids of the columns 
    #todo: trim the spaces of rownames, otherwise comparison fails...
    #todo: add the missing parts for this query [or check that this never happens]
    col_labels <- find.${name(col_entity)}( <#list col_skey_fields as f><#if f.type != "xref">${name(f)}=colnames(.data_matrix)<#else>${name(f)}=${name(f)}</#if><#if f_has_next>, </#if></#list>, .verbose=.verbose )   
    missing_col_labels <- setdiff( colnames(.data_matrix), col_labels$<#list col_skey_fields as f><#if f_index == 0>${name(f)}</#if></#list>)
    if(length(missing_col_labels)>0)
    {
        stop("not all ${name(col_entity)} are known in database, missing cols: ", toString(missing_col_labels),". Use add.${name(col_entity)}() to correct.")
    }
    
    container <- add.${name(container_entity)}(container_arguments, .verbose=.verbose)
    if( !is.logical(container) )
    {     
        cat('Preparing matrix for database:\n')   
        
        #get row and column ids        
        rows = merge(data.frame(name = rownames(.data_matrix)), data.frame(id=row_labels$${name(pkeyname(row_entity))}, name=row_labels$${name(skeys(row_entity)?first.fields?first)}), sort=F)
        cols = merge(data.frame(name = colnames(.data_matrix)), data.frame(id=col_labels$${name(pkeyname(col_entity))}, name=col_labels$${name(skeys(col_entity)?first.fields?first)}), sort=F)
        
        #calculate row batch size
        MAX_BATCH <- 1000000 #todo: centralize this parameter
        nrowPerBatch <- min(nrow(.data_matrix),floor( MAX_BATCH / ncol(.data_matrix)))
        if(nrowPerBatch == 0)
        {
            nrowPerBatch <- 1
        }
        
        #iterate through row batches
        i = 1;
        while(i < nrow(rows))
        {
        	until <- min(nrow(.data_matrix), i + nrowPerBatch - 1)
        	cat("rows ",i,":",until,"\n")
        	#transform into RCV
			content_rows <- cbind(
        		${name(matrix.container)} = container$${name(pkeyname(container_entity))},
        		${name(matrix.row)} = rows$id[i:until],
        		${name(matrix.col)} = rep(cols$id, each=(until - i + 1)),         	
    			${name(matrix.content)}=c(.data_matrix[i:until,]))

			#add to database or handle error
			result <- add.${name(content_entity)}( content_rows , .verbose=.verbose)  
        	if( is.logical(result) )
        	{
            	cat('addition failed, rolling back\n')
            	remove.${name(content_entity)}(${name(matrix.container)}=container$${name(pkeyname(container_entity))})
            	remove.${name(container_entity)}(container)
            	stop('addition failed, rolling back\n')
            	#return(FALSE)
        	}

            i <- i + nrowPerBatch
        }

        cat('Upload of ${name(matrix)} matrix successful in', format(difftime(Sys.time(),starttime, units="sec"), digits=3)," sec.\n")  
        return(TRUE)
    }
}

#todo: move this logic to servlet?
<#assign container_pkey = name(pkeyname(container_entity))/>
get.${name(matrix)}<-function(${container_pkey}=NULL<#list container_skey_fields as f>, ${name(f)}=NULL</#list>, .usesession=T)
{
    starttime=Sys.time()    
    
    #map each skey to pkey
    if(is.null(${container_pkey}))
    {
 		#get session parameters
		<#list container_skey_fields as f><#if f.type == "xref">
    	<#assign session_var = ".MOLGENIS$session." + name(f.getXRefEntity()) + "."+ name(f.getXRefField())>
    	<#assign xref_entity = model.getEntity(f.getXRefEntity())>
    	if(.usesession && is.null(${name(f)}) && !is.null(${session_var}))    
    	{
        	${name(f)} = ${session_var}
        	cat("Using ${name(f)} (${pkeyname(xref_entity)}='",${session_var},"'", sep="")
<#list skeys(xref_entity) as skey><#list key_fields(skey) as f>        
			cat(", ${name(f)}='",.MOLGENIS$session.${name(xref_entity)}.${name(f)},"'", sep="")
</#list></#list>        	 
			cat(") from session (usession = T).\n")
    	} 
    	</#if></#list>       
		<#list skeys(container_entity) as skey>
		#use secondary key
		if(<#list key_fields(skey) as f>!is.null(${name(f)})<#if f_has_next> && </#if></#list>)
		{
        	container <- find.${name(container_entity)}(<#list key_fields(skey) as f>${name(f)}=${name(f)}<#if f_has_next>, </#if></#list>, .usesession=F)
        	if( length(container) == 0 )
            	stop('${name(matrix)} for ${name(container_entity)}(',<#list key_fields(skey) as f>'${name(f)}=', ${name(f)}<#if f_has_next>, </#if></#list>,') not found')
            else
        		${container_pkey} <- container$${container_pkey}			
		}<#if skey_has_next> else </#if>
		</#list>
		else
		{
 			stop('you need to provide {${container_pkey}} or <#list skeys(container_entity) as skey>{<#list key_fields(skey) as f>${name(f)}<#if f_has_next> and </#if></#list>}<#if skey_has_next> or </#if></#list>')		
		}
    }    

    #download textdata
    content_rows <- find.${name(content_entity)}(${name(matrix.container)} = ${container_pkey}, .usesession=F)
    rnames <- unique(content_rows$${name(matrix.row)}_name)
    cnames <- unique(content_rows$${name(matrix.col)}_name)
  
    #todo: move to reusable asset 
    if( length(content_rows)>0)
    {
        result  <- matrix(nrow=length(rnames), ncol=length(cnames), dimnames=list(rnames,cnames))
        for(i in 1:length(cnames))
        {
            nstart <- (i-1)*length(rnames)+1
            nend <- i*length(rnames)
            result[,i]<-content_rows$value[ nstart:nend ]
        }
        cat("Transformed data into ${Name(matrix)} matrix(nrow=",nrow(result),",ncol=",ncol(result),") in ", format(difftime(Sys.time(),starttime, units="sec"), digits=3),"\n", sep="")
        return(result)        
    }      
   
<#--    
    starttime=Sys.time()       
    if( length(content_rows)>0)
    {        
        result  <- matrix(nrow=length(rnames), ncol=length(cnames), dimnames=list(rnames,cnames))
        for(i in 1:nrow(content_rows))
        {
            result[content_rows$${name(matrix.row)}_name[i],content_rows$${name(matrix.col)}_name[i]]<-content_rows$${name(matrix.content)}[i]
        }
        cat("Transformed data into matrix(nrow=",nrow(result),",ncol=",ncol(result),") in ", format(difftime(Sys.time(),starttime, units="sec"), digits=3),"\n", sep="")
        return(result)
    }
-->
    return(NULL)
}

#find by pkey or skey container and remove
remove.${name(matrix)} <- function(${container_pkey}=NULL<#list container_skey_fields as f>, ${name(f)}=NULL</#list>)
{     
    container <- find.${name(container_entity)}(${container_pkey}<#list container_skey_fields as f>, ${name(f)}=${name(f)}</#list>)
    if(is.null(container)) stop('cannot find ${name(container_entity)}. Search using {${container_pkey}} or <#list skeys(container_entity) as skey>{<#list key_fields(skey) as f>${name(f)}<#if f_has_next> and </#if></#list>}<#if skey_has_next> or </#if></#list>')
    content_rows <- find.${name(content_entity)}(${name(matrix.container)} = ${container_pkey})
     
    #todo: make more efficient
    remove.${name(content_entity)}(content_rows)
    remove.${name(container_entity)}(container)    
}