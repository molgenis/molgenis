<#-- @formatter:off -->
###################################################################
#
# Molgenis R api client.
#
####################################################################
library('RCurl')
library('rjson')
library('httr')
library('ff')

#Prevent scientific notation
options(scipen=999)

#Create a new environment
molgenis.env <- new.env()

local({
    molgenis.api.url <- paste0("${api_url}",'v1/')
    molgenis.api.url.v2 <- paste0("${api_url}",'v2/')
    <#if token??>
    molgenis.token <- "${token}"
    <#else>
    molgenis.token <- NULL
    </#if>
}, molgenis.env)
###################################################################
#
# Login to the rest api and create a session
#
# Parameters:
#    username: your username
#    password: your password
#
###################################################################
molgenis.login <- local(function(username, password) {
    jsonRequest <- toJSON(list(username = username, password = password))
        url <- paste0(molgenis.api.url, "login")
        jsonResponse <- postForm(url, .opts = list(postfields = jsonRequest, httpheader = c('Content-Type' = 'application/json')))
        cat("Login success")
        response <- fromJSON(jsonResponse)
        molgenis.token <<- response$token
}, molgenis.env)

####################################################################
#
# Logout from the rest api
#
####################################################################
molgenis.logout <- local(function() {
    url <- paste0(molgenis.api.url, 'logout')
    POST(url, add_headers('x-molgenis-token' = molgenis.token))
    cat("Logout success")
}, molgenis.env)


#####################################################################
#
# Query the data api
# Parameters:
#    entity: the entityname (required)
#   q: the query
#   start: the index of the first result to return
#   num: the number of results to return (default is 1000, max is 10000)
#   attributes: the attributes to return if NULL (default) all attributes are returned
#
# Return:
#    Dataframe with the query result
#
# The query must be in fiql/rsql format (see https://github.com/jirutka/rsql-parser)
#
# Example: molgenis.get(entity = "Person", q = "firstName==Piet", start = 6, num = 10)

#######################################################################
molgenis.get <- local(function(entity, q = NULL, start = 0, num = 1000, sortColumn= NULL, sortOrder = NULL, attributes = NULL) {
    url <- paste0(molgenis.api.url, "csv/", entity, "?molgenis-token=", molgenis.token, "&start=", start, "&num=", num, "&sortColumn=", sortColumn, "&sortOrder=", sortOrder)

    if (!is.null(q)) {
        url <- paste0(url, "&q=", curlEscape(q))
    }

    if (!is.null(attributes)) {
        url <- paste0(url, "&attributes=", curlEscape(paste0(attributes, collapse = ",")))
    }

    # FIXME Check metadata for every column and set a colClass vector corresponding to the correct type
    # EXAMPLE: column1 contains strings,
    # characterClass <- c("character")
    # names(characterClass) <- c("column1")
    # read.csv(url, colClass = c(characterClass))
    csv <- getURL(url)
    dataFrame <- read.csv(textConnection(csv))
        return (dataFrame)
}, molgenis.env)


######################################################################
#
# Creates a new entity
#
# Parameters:
#    entity: the entityname
#   ...: var arg list of attribute name/value pairs
#
# Return:
#    the id of the created entity
#
# example: molgenis.add(entity = "Person", firstName = "Piet", lastName = "Paulusma")
#
####################################################################
molgenis.add <- local(function(entity, ...) {
    molgenis.addList(entity, list(...))
}, env = molgenis.env)

######################################################################
#
# Creates new entities
#
# Parameters:
#    entity: the entityname
#   rows: dataFrame where each row is an entity
#
# example:
#    firstName <- c("Piet", "Klaas")
#   lastName  <- c("Paulusma", "de Vries")
#   df <- data.frame(firstName, lastName)
#   molgenis.addAll("Person", df)
#
####################################################################
molgenis.addAll <- local(function(entity, rows) {
  ids <- c()
  url <- paste0(molgenis.api.url.v2, entity)
  #only 1000 rows can be processed ad once, so make chunks of 1000
  for(i in chunk(from = 1, to = nrow(rows), by = 1000)){
    rowsChunk = rows[min(i):max(i), ]
    rowJSON = gsub("\\", "", apply(rowsChunk, 1, rjson::toJSON), fixed=T)
    entities <- paste0(rowJSON, collapse=",")
    param =  paste0('{entities:[',entities,']}')
    #dont use curl, use httr POST
    response <- POST(url, add_headers('x-molgenis-token' = molgenis.token), body = param, content_type_json())
    status <- status_code(response)

    #On success the api returns httpcode 201 CREATED
    if (status != "201") {
      cat(status)
      stop("Error creating entity")
    }

    #The entity is created successfully, return the new id
    resources <- sapply(content(response)$resources, function(r){
      l <- strsplit(r$href, "/")[[1]]
      return(l[length(l)])
    })
    ids <- c(ids, resources)
  }
  return (ids)
}, molgenis.env)

######################################################################
#
# Creates a new entity
#
# Parameters:
#    entity: the entityname
#   attributeList: list of attribute name/value pairs
#
# Return:
#    the id of the created entity
#
#####################################################################
molgenis.addList <- local(function(entity, attributeList) {
    url <- paste0(molgenis.api.url, entity)
    h <- basicHeaderGatherer()
    postForm(url,
            .params = attributeList,
            style = "POST",
            .opts = list(headerfunction = h$update,
                    httpheader = list("x-molgenis-token" = molgenis.token,
                    "Content-Type" = "application/x-www-form-urlencoded")))

    returnedHeaders <- h$value()

    #On success the api returns httpcode 201 CREATED
    if (returnedHeaders["status"] != "201") {
        stop("Error creating entity")
    }

    #The entity is created successfully, return the new id
    location <- returnedHeaders["Location"]
    l <- strsplit(location, "/")[[1]]

    return (l[length(l)])

}, env = molgenis.env)


#####################################################################
#
# Updates an existing entity
#
# Parameters:
#    entity: the entityname
#    id: the id of the entity to update
#   ...: var arg list of attribute name/value pairs
#
# example: molgenis.update(entity = "Person", id = 5, firstName = "Piet", lastName = "Paulusma")
#
#####################################################################
molgenis.update <- local(function(entity, id, ...) {
url <- paste0(molgenis.api.url, entity, "/", id)

parameters <- list(...)
    parameters <- c(parameters, "_method" = "PUT")

    h <- basicHeaderGatherer()

    postForm(url,
            .params = parameters,
            style = "POST",
            .opts = list(headerfunction = h$update,
            httpheader = list("x-molgenis-token" = molgenis.token,
                                "Content-Type" = "application/x-www-form-urlencoded")))

    returnedHeaders <- h$value()

    #On success the api returns httpcode 204 NO_CONTENT
    if (returnedHeaders["status"] != "204") {
        stop("Error updating entity")
    }

}, env = molgenis.env)


#####################################################################
#
# Deletes an existing entity
#
# Parameters:
#    entity: entityname
#    id: the id of the entity to delete
#
# Example: molgenis.delete(entity = "Person", id = 5)
#
#####################################################################
molgenis.delete <- local(function(entity, id) {
    url <- paste0(molgenis.api.url, entity, "/", id)
    h <- basicHeaderGatherer()

    postForm(url,
            .params = c("_method" = "DELETE"),
            style = "POST",
            .opts = list(headerfunction = h$update,
            httpheader = list("x-molgenis-token" = molgenis.token)))

    returnedHeaders <- h$value()

    #On success the api returns httpcode 204 NO_CONTENT
    if (returnedHeaders["status"] != "204") {
        stop("Error deleting entity")
    }

}, env = molgenis.env)

#####################################################################
#
# Deletes a list of entities in an entityType.
#
# Parameters:
#    entity: The entityType name
#    rows: List with ids of the rows
#
# Example: molgenis.deleteList(entity = "Person", rows = c("1", "2", "3"))
#
#####################################################################
molgenis.deleteList <- local(function(entity, rows) {
  url <- paste0(molgenis.api.url.v2, entity)
  #only 1000 rows can be processed ad once, so make chunks of 1000
  chunks <- split(rows, ceiling(seq_along(rows)/1000))
  for(i in chunks){
    param =  paste0('{entityIds:["',paste0(i,collapse = '","'),'"]}')
    #dont use curl, use httr POST
    response <- DELETE(url, add_headers('x-molgenis-token' = molgenis.token), body = param, content_type_json())
    status <- status_code(response)
    #On success the api returns httpcode 204 No Content
    if (status != "204") {
      cat(status)
      error_message <- content(response)$errors[[1]]$message
      stop(paste0("Error deleting entities: ", error_message))
    }
  }
  return (rows)
}, molgenis.env)

#####################################################################
#
# Gets entity metadata
#
# Parameters:
#    entity: the entityname
# Returns:
#    list
#
#####################################################################
molgenis.getEntityType <- local(function(entity) {
    url <- paste0(molgenis.api.url, entity, "/meta?expand=attributes")
    responseJson <- getURL(url, httpheader = list("x-molgenis-token" = molgenis.token))
    response <- fromJSON(responseJson)

    return (response)
}, molgenis.env)


#####################################################################
#
# Gets attribute metadata
#
# Parameters:
#    entity: the entityname
#    attribute: the attributename
# Returns:
#    list
#
#####################################################################
molgenis.getAttributeMetaData <- local(function(entity, attribute){
    url <- paste0(molgenis.api.url, entity, "/meta/", attribute)
    responseJson <- getURL(url, httpheader = list("x-molgenis-token" = molgenis.token))
    response <- fromJSON(responseJson)

    return (response)
}, molgenis.env)
