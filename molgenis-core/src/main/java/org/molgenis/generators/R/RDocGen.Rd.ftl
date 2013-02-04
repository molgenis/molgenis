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
# This file provides action methods to MOLGENIS for entity ${Name(entity)}
#
# THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
#

# Search parameters
<#list allFields(entity) as field>
# ${name(field)}:${field.type}  	
#		${field.description} 
</#list> 
find${PluralName(entity)} <- function( <#list allFields(entity) as field>${name(field)}=NULL <#if field_has_next>, </#if></#list> )
{
  ##see wether we have search parameters
  querystring <- "";
  <#list allFields(entity) as field>
  if( ! is.null(${name(field)}) )
  {
    querystring <- paste( querystring, ${name(field)},"=",toString(${name(field)}),sep="")
  }   
  </#list>

  # Returns a list where each element is another list.
  #  The 'big/outer' list contains the original columns, and each column
  #  a list with the row values.
  listResult <- getEntity( "${name(model)}.data.types.${Name(entity)}", querystring)

  # We calculate the size of the output matrix (FIXME: put in lib?)
  lenRow <- length( unique( listResult$row ))
  lenCol <- length( unique( listResult$col ))
  
  outRowNames <- unique( listResult$row_name )
  outColNames <- unique( listResult$col_name )

  outputMatrix <- matrix( data=as.vector(listResult$value),
                             nrow=lenRow, ncol=lenCol,
                             byrow=FALSE,
                             dimnames=list( outRowNames, outColNames ) )
  outputMatrix
}

 