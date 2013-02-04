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
# This file sources all R files.
#
# THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
#

#entities
<#list model.entities as entity><#if !entity.abstract && !entity.association && !entity.system>
msource(paste(r_api_location,"${entity.namespace?replace(".","/")}/R/${Name(entity)}.R", sep=""))
</#if></#list>

<#--
# matrices
<#list model.matrices as matrix>
msource(paste(molgenispath,"${name(model)}/R/${Name(matrix)}.R", sep=""))
</#list>
-->

#relative location of the Find and Add APIs which are required
findAPIlocation <- "${findAPIlocation}"
addAPIlocation <- "${addAPIlocation}"

#allows to pass different locations when sourcing the script in a custom way
if( !exists("r_api_location") ) r_api_location <- ""
if( !exists("app_location") ) app_location <- ""

#connect to the R API, setting up a session
MOLGENIS.connect <- function()
{
	# Loading RCurl under Unix this way will ONLY work when the package is installed as root.
	# User installations are in different directories.
	# If users load it in a custom way, the library must not be sourced again.
	# If they do not load it.. TODO: write alternative call to load RCurl
	if(!any(loadedNamespaces()=="RCurl")){
    	library( RCurl )
    }
    
	#keep track of the session by emulating a browser
	#save curlHandle out-of-scope
	.MOLGENIS.curlHandle <- getCurlHandle()
	.MOLGENIS.curlHandle <<- curlSetOpt(
		curl = .MOLGENIS.curlHandle,
		ssl.verifypeer = FALSE,
		useragent = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.6; en-US; rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13",
		timeout = 60,
		followlocation = TRUE,
		cookiejar = "./cookies",
		cookiefile = "./cookies"
	)
	
	#legacy?
	.MOLGENIS <<- data.frame()
    
    cat("MOLGENIS is connected\n")
}

#send a login request to the R API
MOLGENIS.login = function(username, password)
{
	if(missing(password)) password <- username
	curlParams = list(usr = username, pwd = password)
	response <- postForm( r_api_location, .params = curlParams, curl = .MOLGENIS.curlHandle )
	handle <- textConnection(response)
	status <- readLines(handle, 1)
	cat(status, "\n")
	close( handle )
}

#send a logout request to the R API
MOLGENIS.logout = function()
{
	curlParams = list(logout = "logout")
	response <- postForm( r_api_location, .params = curlParams, curl = .MOLGENIS.curlHandle )
	handle <- textConnection(response)
	status <- readLines(handle, 1)
	cat(status, "\n")
	close( handle )
}

#download a file via RCurl
downloadFileViaCurl <- function(url = "www.dannyarends.nl/apps/genotypes.txt", filename = "out.txt"){
  mydata <- getURLContent(url, binary=T, curl = .MOLGENIS.curlHandle)
  fin <- file(filename,"wb")
  writeBin(mydata[1:length(mydata)],con=fin)
  close(fin)
  invisible(mydata)
}

#file upload wrapper to pass the RCurl handle with the session
#untested
MOLGENIS.upload <- function(url, Investigation_name, name, type, filename, style = 'HTTPPOST'){
  postForm(url, Investigation_name=Investigation_name, name=name, type = type, file = fileUpload(filename=filename), style=style, curl = .MOLGENIS.curlHandle)
}

#helper function to print debug
MOLGENIS.debug<-function(...)
{
    if(!is.null(.MOLGENIS$debug) && .MOLGENIS$debug == T)
    {
        cat(...);
    }
}

#create a list of query criteria
MOLGENIS.createCriteria<-function(args)
{
    result <- list()
  	for(i in 1:length(args))
  	{
        #cat('what:',names(args)[i], args[[i]], class(args[[i]]),"\n" )
          if(!is.null(args[[i]]))
          {                
              if(length(args[[i]])>1)
                 result[names(args[i])] <- paste("[",paste(args[[i]], collapse=","),"]", sep="") 
              else
                 result[names(args[i])] <- toString(args[[i]])               
          }
  	}
  	return(result)
}

MOLGENIS.find<-function( entityName, conditions, .verbose=T )
{ 	
    starttime=Sys.time()
    
    # a list of conditions
    filter<-MOLGENIS.createCriteria(conditions) 

    # Check wether we are connected
    if(!exists(".MOLGENIS.curlHandle"))
    {
        stop("You first must connect to a MOLGENIS. Use function 'MOLGENIS.connect()'")
    }
 
    # We check and prepare the argument variables
    if( missing( entityName ) ) {
        stop( "arg1: You should provide an entityName (e.g. \"Experiment\")" )
    }
    #todo: use post instead of get
    uri <- paste( app_location, findAPIlocation, "/", entityName, sep="" )
 
    ##log
    # cat("retrieving using uri",uri,"\n")
    flush.console()
  
	#if the params list is empty, RCurl will complain, so we add something here. 'default' is a keyword and can never match a real field.
  	if(length(filter)==0)
  	{
  		filter <- c(default="default")
  	}
  	
    # We query the server
    outputString <- postForm( uri, .params = filter, curl = .MOLGENIS.curlHandle )
	MOLGENIS.debug("Send find to server and got to parse in", format(difftime(Sys.time(),starttime, units="sec"), digits=3),"sec.\n")    
	
    # Check for errors
    # - the Entity doesn't exists
    if( regexpr( "java.lang.ClassNotFoundException", outputString ) != -1 ) {
        cat( "Error in getEntity: entity '", entityName,"' doesn't exists\n", sep="" )
        flush.console()
        return (-1)
    }      
    # - empty answer
    else if( nchar( outputString ) == 0 ) {
        # We let the empty string to go outside. It is not a drastic error!   
        if(.verbose) cat("Downloaded", 0,"rows of",entityName,"in", format(difftime(Sys.time(),starttime, units="sec"), digits=3),"sec.\n")    
        return (outputString)
    }
    # no more errors, we can process the output
    else {	
        handle <- textConnection( outputString )
        result <- read.table( handle, as.is=T, header=T, sep="\t", quote="\"" )
        close( handle )
        if(.verbose) cat("Downloaded", format(nrow(result),big.mark=","),"rows of",entityName,"in", format(difftime(Sys.time(),starttime, units="sec"), digits=3),"sec.\n")
        rownames(result) <- tolower(rownames(result))
        colnames(result) <- tolower(colnames(result))
        return (result)
    }
}

MOLGENIS.update <- function(entityName, dataMatrix, action, is_matrix=F, row_type=NA, col_type=NA, is_silent=F, .verbose=T)
{
    if(!is.data.frame(dataMatrix) && !is.matrix(dataMatrix))
        stop("MOLGENIS.Update expects dataMatrix of type data.frame or matrix") 
    if(nrow(dataMatrix) == 0)
        stop("nothing to do, dataMatrix is empty")        

    starttime=Sys.time()
    MOLGENIS.debug(action, "ing " ,nrow(dataMatrix), " rows of ",entityName, ".\n" , sep="" )
    if(.verbose && ceiling(nrow(dataMatrix)/1000) > 5) cat("Can take ", ceiling(nrow(dataMatrix)/1000), " secs... \n",sep="")
    flush.console()
    
    #create tempfile to upload
    temp<-tempfile()
    write.table(dataMatrix, file=temp, sep="\t", quote=F, row.names=is_matrix)
    MOLGENIS.debug("\ncreated tab-file",temp,"for upload in", format(difftime(Sys.time(),starttime, units="sec"), digits=3),"\n")
    
    curl_params = list(
        data_type_input = entityName, 
        data_action = action,
        submit_input = "submit",
        data_silent = toString(is_silent),
        data_file = fileUpload(filename=temp, contentType="text/plain")
    )
    
    #if small set, do not use file upload)
    if(nrow(dataMatrix)<20000)
    {
        handle<-file(temp)
        curl_params = list(
            data_type_input = entityName, 
            data_input = paste(scan(handle,"raw", sep="\n", quote="\"", quiet=T), collapse="\n"),
            data_action = action,
            data_silent = toString(is_silent),
            submit_input = "submit")
        close(handle)
    }
    
    uri <- paste( app_location, addAPIlocation, sep="" )
    webResponse <- postForm( uri, .params = curl_params, curl = .MOLGENIS.curlHandle ) 
    MOLGENIS.debug("send data to server and got response in", format(difftime(Sys.time(),starttime, units="sec"), digits=3),"\n")
    #remove tempfile      
    unlink(temp)                                              
    
    #handle the response
    handle <- textConnection(webResponse)                           
    status <- readLines(handle, 1)
    if( regexpr( "Failed", status ) < 0 ) {
        result = TRUE
        
        # let result hold updated data (if any lines available)
        line <- readLines(handle, n=1, ok=TRUE, warn=TRUE)
        #cat("what is wrong with line '",line,"' of type ",typeof(line), sep="")
        if(line != "")
        {
            pushBack(line,handle)
            result <- read.table( handle, as.is=T, header=T, sep="\t", quote="\"" )
        }
        close( handle )
          
        #show status
        cat(status,"in",format(difftime(Sys.time(),starttime, units="sec"), digits=3),"\n")
        return (result);    	
    }   
    else {
        #cat("\n",status,"\n", sep="")
        close( handle )
        stop(status)
        #return (FALSE);
    }  
}
