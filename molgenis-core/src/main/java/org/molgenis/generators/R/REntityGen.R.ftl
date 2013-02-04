<#--helper functions-->
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
# This file provides action methods to MOLGENIS for entity ${JavaName(entity)}
#
# THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
#
<#assign pkey_fields = entity.getKeyFields(0)>
<#assign skey_fields = key_fields(skeys(entity))>

#create valid data_frame for ${JavaName(entity)}
.create.${RName(entity)} <- function(data_frame, value_list, .usesession=T, .verbose=T)
{
	#convert to data_frame, remove null columns
	if(!is.data.frame(data_frame))
	{
		if(is.matrix(data_frame))
		{
			data_frame <- as.data.frame(data_frame)
		}
		else if(is.list(data_frame))
		{
			data_frame <- as.data.frame(data_frame[!sapply(data_frame, is.null)])
		}
		#transform non-null values into data.frame
		else
		{
			data_frame <- as.data.frame(value_list[!sapply(value_list, is.null)])
		}
  	}	
  	  	  	
    <#list skey_fields as f><#if f.type == "xref">
    <#assign session_var = ".MOLGENIS$session." + RName(f.xrefEntity) + "."+ RName(f.xrefField)>
    <#assign xref_entity = f.xrefEntity>
    #add missing xref values from session parameters (optional)
    if(.usesession && is.null(data_frame$${RName(f)}__${RName(f.xrefField)}) && !is.null(${session_var}))    
    {
        data_frame$${RName(f)} = ${session_var}
        if(.verbose) 
        {
        	cat("Using ${RName(f)} (${RName(pkey(xref_entity))}='",${session_var},"'", sep="")
<#list skeys(xref_entity) as skey><#list key_fields(skey) as f>        
			cat(", ${RName(f)}='",.MOLGENIS$session.${RName(xref_entity)}.${RName(f)},"'", sep="")
</#list></#list>        	 
			cat(") from session (.usession = T).\n")
		} 
    }
    </#if>
    </#list>      
        
    return(data_frame)
}

#freely find ${JavaName(entity)} 
<@compress single_line=true>
find.${RName(entity)} <- function(
<#list allFields(entity) as f>
	<#if f.name != typefield()>
		<#if f.type="xref">
			${RName(f)}_${RName(f.xrefField)}=NULL
			<#if f.xrefLabelNames[0] != f.xrefFieldName>
				<#list f.xrefLabelNames as label>
					, ${RName(f)}_${RName(label)}=NULL
				</#list>
			</#if>
		<#else>
			${RName(f)}=NULL
		</#if>
		,
	</#if>
</#list>
.usesession = T, .verbose=T)
</@compress>
{
	#add session parameters
    <#list skey_fields as f><#if f.type == "xref">
    <#assign session_var = ".MOLGENIS$session." + RName(f.xrefEntity) + "."+ RName(f.xrefField)>
    <#assign xref_entity = f.xrefEntity>
    if(.usesession && is.null(${RName(f)}_${RName(f.xrefField)}) && !is.null(${session_var}))    
    {
        ${RName(f)}_${RName(f.xrefField)} = ${session_var}
        cat("Using ${RName(f)}_${RName(f.xrefField)} (${RName(pkey(xref_entity))}='",${session_var},"'", sep="")
<#list skeys(xref_entity) as skey><#list key_fields(skey) as f>        
		cat(", ${RName(f)}='",.MOLGENIS$session.${RName(xref_entity)}.${RName(f)},"'", sep="")
</#list></#list>        	 
		cat(") from session (.usession = T).\n")        
    } 
    </#if></#list>  	   
    
	result <- MOLGENIS.find( "${JavaName(entity)}", mget(ls(),environment()), .verbose=.verbose)
	<#if skey_fields?size &gt; 0>
	#use secondary key as rownames
	#rownames(result)<-result$${RName(skey_fields?first)}
	</#if>
	return(result)
}

#add data.frame of ${JavaName(entity)} or each column individually
#note: each column must have the same length
add.${RName(entity)} <- function(.data_frame=NULL<#list allFields(entity) as f><#if f.type="xref">, ${RName(f)}_${RName(f.xrefField)}=NULL<#if f.xrefLabelNames[0] != f.xrefFieldName><#list f.xrefLabelNames as label>, ${RName(f)}_${RName(label)}=NULL</#list></#if><#elseif !f.auto>, ${RName(f)}=NULL</#if></#list>, .usesession = T, .verbose=T )
{
	.data_frame = .create.${RName(entity)}(.data_frame, mget(ls(),environment()), .usesession = .usesession, .verbose = .verbose)
   	return( MOLGENIS.update("${entity.namespace}.${JavaName(entity)}", .data_frame, "ADD", .verbose=.verbose) )
}


#remove data.frame of ${JavaName(entity)} or just one row using named arguments.
remove.${RName(entity)} <- function( .data_frame=NULL<#list pkey_fields as f>, ${RName(f)}=NULL</#list><#list skey_fields as f><#if f.name != PkeyName(entity)>, ${RName(f)}=NULL</#if></#list>, .usesession = T )
{	
	#todo: translate to skey to pkey
	.data_frame = .create.${RName(entity)}(.data_frame, mget(ls(),environment()), .usesession = .usesession)
   	return( MOLGENIS.update("${entity.namespace}.${JavaName(entity)}", .data_frame, "REMOVE") )
}

<#assign skey_fields = key_fields(skeys(entity))>
use.${RName(entity)}<-function(.data_frame=NULL<#list pkey_fields as f>, ${RName(f)}=NULL</#list><#list skey_fields as f><#if f.name != PkeyName(entity)>, ${RName(f)}=NULL</#if></#list>)
{
	#add session parameters
    <#list skey_fields as f><#if f.type == "xref">
    <#assign session_var = ".MOLGENIS$session." + RName(f.xrefEntity) + "."+ RName(f.xrefField)>
    <#assign xref_entity = f.xrefEntity>
    if(is.null(${RName(f)}) && !is.null(${session_var}))    
    {
        ${RName(f)} = ${session_var}
        cat("Using ${RName(f)} (${RName(pkey(xref_entity))}='",${session_var},"'", sep="")
<#list skeys(xref_entity) as skey><#list key_fields(skey) as f>        
		cat(", ${RName(f)}='",.MOLGENIS$session.${RName(xref_entity)}.${RName(f)},"'", sep="")
</#list></#list>        	 
		cat(") from session.\n")        
    } 
    </#if></#list>           
    
    #retrieve the ${RName(entity)} by pkey or skey
    row<-F
    if(!is.null(${RName(pkey(entity))}))
    {
    	row<-find.${RName(entity)}(${RName(pkey(entity))}=${RName(pkey(entity))}) 
    }  
<#list skeys(entity) as skey> 
	else if( !(<#list key_fields(skey) as f>is.null(${RName(f)})<#if f_has_next> ||</#if></#list>) )
	{
		row<-find.${RName(entity)}(<#list key_fields(skey) as f>${RName(f)}=${RName(f)}<#if f_has_next>,</#if></#list>)
	} 
</#list>    
    else
    {
    	stop('you need to provide {${RName(pkey(entity))}}<#list skeys(entity) as skey> or {<#list key_fields(skey) as f>${RName(f)}<#if f_has_next> and </#if></#list>}</#list>')
    }       
    
    #if exists, put in session
    if(!is.logical(row) && nrow(row) == 1)
    {
    	cat("Using ${RName(entity)} with:\n")
    	cat("\t${RName(pkey(entity))}=",row$${RName(pkey(entity))},"\n")
		.MOLGENIS$session.${RName(entity)}.${RName(pkey(entity))}<<-row$${RName(pkey(entity))}
<#list skeys(entity) as skey><#list key_fields(skey) as f>        
		cat("\t${RName(f)}=",row$${RName(f)},"\n")
		.MOLGENIS$session.${RName(entity)}.${RName(f)}<<-row$${RName(f)}
</#list></#list>
    }
    else
    {
       cat("Did not find ${RName(entity)} using ","${RName(pkey(entity))}=",${RName(pkey(entity))},<#list skey_fields as f>"${RName(f)}=",${RName(f)},</#list>"\n")
    }
}