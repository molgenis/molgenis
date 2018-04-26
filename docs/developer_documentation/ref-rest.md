**
The MOLGENIS REST API allows you to retrieve and modify your data model entities and entity collections. The API supports all CRUD (create, read, update and delete) methods as well as resource collections paging & filtering and resource field expansion. At the moment of writing JSON and form data are supported as request content types and JSON is the only supported response content type.
**

Your MOLGENIS data model defines the resources and resource collections that can be accessed and modified. Lets assume that your data model contains the entities DataSet, Protocol and Feature. The REST API will consist of the following endpoints:

# Collections

MOLGENIS entity collections are available as end-points:

*Endpoints*
* http://molgenis.mydomain.example/api/v1/dataset
* http://molgenis.mydomain.example/api/v1/protocol
* http://molgenis.mydomain.example/api/v1/feature

## Retrieve collection

Each entity collection has its own end-point:

*Request*
```
GET http://molgenis.mydomain.example/api/v1/dataset 
```
*Response*
```javascript
200 OK

{
    "href": "/api/v1/dataset",
    "start": 0,
    "num": 100,
    "total": 2,
    "items": [{
        "href": "/api/v1/dataset/1",
        "name": "my first data set",
        "protocol": {
            "href": "/api/v1/dataset/1/protocol"
        },{
        "href": "/api/v1/dataset/2",
        "name": "my second data set",
        "protocol": {
            "href": "/api/v1/dataset/2/protocol"
        }
    }]
}
```
## Delete collection

Datasets can be completely deleted (Nb. the meta data will be kept):

*Request*
```
DELETE http://molgenis.mydomain.example/api/v1/dataset 
```
*Response*
```javascript
204 No Content
```

## Query a collection

Key | Type | Description
--- | --- | ---
*start* | int | Offset in resource collection
*num* | int | Number of resources to retrieve starting at *start*
*q* | list of molgenis query rule objects | Query to filter the resource collection list

### start and num
*Request*
```
GET http://molgenis.mydomain.example/api/v1/dataset?start=1&num=1
```
*Response*
```javascript
200 OK

{
    "href": "/api/v1/dataset",
    "start": 1,
    "num": 1,
    "total": 3,
    "prevHref":"/api/v1/dataset?start=0&num=1"
    "nextHref":"/api/v1/dataset?start=2&num=1"
    "items": [{
        "href": "/api/v1/dataset/2",
        "name": "my second data set",
        "protocol": {
            "href": "/api/v1/dataset/2/protocol"
        }
    }]
}
```
*prevHref* is the location of the previous page of resources, *nextHref* is the location of the next page of resources.
### q
*Request*
```
POST http://molgenis.mydomain.example/api/v1/dataset?_method=GET
Content-Type: application/json

{
    "q": [{
       "field": "name",
       "operator": "EQUALS",
       "value": "my first data set"
    }]
}
```
*Response*
```javascript
200 OK

{
    "href": "/api/v1/dataset",
    "start": 0,
    "num": 100,
    "total": 1,
    "items": [{
        "href": "/api/v1/dataset/1",
        "name": "my first data set",
        "protocol": {
            "href": "/api/v1/dataset/1/protocol"
        }
    }]
}
```

# Instances

Each data row within a collection (i.e. entity instance) can be accessed via its identifier:

*Endpoints*
* http://molgenis.mydomain.example/api/v1/dataset/<entity id>
* http://molgenis.mydomain.example/api/v1/protocol/<entity id>
* http://molgenis.mydomain.example/api/v1/feature/<entity id>

## Retrieve

*Request*
```
GET http://molgenis.mydomain.example/api/v1/dataset/1
```
*Response*
```javascript
200 OK

{
    "href": "/api/v1/dataset/1",
    "name": "my first data set",
    "protocol": {
        "href": "/api/v1/dataset/1/protocol"
    }
}
```
*href* is the location of this resource, *name* is a string value and *protocol* is the location of the entity that this dataset refers to.
 
## Create

By psoting a JSON message that matches the meta data attributes you can add new instances:

*Request*
```
POST http://molgenis.mydomain.example/api/v1/person
Content-Type: application/json

{
    "age": "17",
    "driverslicence": true
}

```
*Response*
```
201 Created

In the response headers you can find the id of the newly created resource:
Example: Location: /api/v1/person/AAAACTUMDL2N4BTTSWWS6PQAAE 
```
## Update

Existing instances can be updated by putting a complete changed record to the entity instance endpoint:

*Request*
```
PUT http://molgenis.mydomain.example/api/v1/dataset/3
Content-Type: application/json

{
    "name": "renamed data set"
}
```
*Response*
```
204 No Content
```

## Update one value

If you only want to change one attribute without needing to provide all other attributes you can put values to the attributes seperately:

*Request*
```
PUT http://molgenis.mydomain.example/api/v1/dataset/3/name
Content-Type: application/json

"renamed data set"
```
*Response*
```
204 No Content
```

## Delete

*Request*
```
DELETE http://molgenis.mydomain.example/api/v1/dataset/3
```
*Response*
```
204 No Content
```

## Advanced options

Instance end-points have the following options:

Key | Type | Description
--- | --- | ---
*attributes* | Comma-separated string | Defines which fields in the API response to select
*expand* | Comma-separated string | Defines which fields in the API response to (partially) expand
*_method* | HTTP method | Tunnel request through defined method over default API operation
*callback* | string | Callback function name used as JSON padding to allow cross domain requests

### select attributes

In a query you can specify what attributes to return, useful when having thousands of attributes:

*Request*
```
GET http://molgenis.mydomain.example/api/v1/dataset/1?attributes=identifier,name
```
*Response*
```javascript
200 OK

{
    "href": "/api/v1/DataSet/1",
    "Identifier": "celiacsprue",
    "Name": "Celiac Sprue"
}
```
### expand xref attributes

MOLGENIS can have attributes of type xref (foreign key) that you can follow and expand attributes of:

*Request*
```
GET http://molgenis.mydomain.example/api/v1/dataset/1?expand=protocol
```
*Response*
```javascript
200 OK

{
    "href": "/api/v1/dataset/1",
    "name": "my first data set",
    "protocol": {
        "href": "/api/v1/protocol/10",
        "name": "protocol for dataset #1",
        "features": {
            "href":"/api/v1/protocol/37265/features"
        }
    }
}
```
### partial expand

Within the expansion you can again specify the attributes to be included:

*Request*
```
GET http://molgenis.mydomain.example/api/v1/dataset/1?expand=protocol[name]
```
*Response*
```javascript
200 OK

{
    "href": "/api/v1/dataset/1",
    "name": "my first data set",
    "protocol": {
        "href": "/api/v1/protocol/10",
        "name": "protocol for dataset #1"
    }
}
```

### _method
Some browsers do not support operations such as PUT and DELETE. The *_method* parameter can be used to tunnel the request over a POST operation.

*Request*
```
POST http://molgenis.mydomain.example/api/v1/dataset/3?_method=PUT
Content-Type: application/json

{
    "name": "renamed data set"
}
```
*Response*
```
204 No Content
```

### callback
*Request*
```
GET http://molgenis.mydomain.example/api/v1/dataset/1?callback=myfunction
```
*Response*
```javascript
200 OK
myfunction(
{
    "href": "/api/v1/dataset/1",
    "name": "my first data set",
    "protocol": {
        "href": "/api/v1/dataset/1/protocol"
    }
}
)
```

# Meta data

Meta data provides details on the attributes within your collection. This allows for client side generation of user interfaces. Assuming that you have entities 'datasets', 'protocol' and 'features' then you can retrieve the metadata as follows:

*Endpoints*
* http://molgenis.mydomain.example/api/v1/dataset/meta
* http://molgenis.mydomain.example/api/v1/protocol/meta
* http://molgenis.mydomain.example/api/v1/feature/meta

## Retrieve meta data

You can retrieve meta data for each collection:

*Request*
```
GET http://molgenis.mydomain.example/api/v1/dataset/meta
```
*Response*
```javascript
200 OK

{
    "href": "/api/v1/DataSet/meta",
    "name": "DataSet",
    "label": "",
    "attributes": {
        "Identifier": {
            "href": "/api/v1/DataSet/meta/Identifier"
        },
        "Name": {
            "href": "/api/v1/DataSet/meta/Name"
        },
        "description": {
            "href": "/api/v1/DataSet/meta/description"
        },
        "ProtocolUsed": {
            "href": "/api/v1/DataSet/meta/ProtocolUsed"
        },
        "startTime": {
            "href": "/api/v1/DataSet/meta/startTime"
        },
        "endTime": {
            "href": "/api/v1/DataSet/meta/endTime"
        }
    },
    "labelAttribute": "Identifier",
    "languageCode": "en"
}
```
## Delete meta data

Deletes resource meta data and all data associated with this resource.

*Request*
```
DELETE http://molgenis.mydomain.example/api/v1/dataset/meta
```
*Response*
```javascript
204 No Content
```

# Authentication
The login route generates a MOLGENIS-token which you can use to access the RESTAPI. When 2-factor-authentication is enabled this route
will be disabled if the current user is 2 factor authenticated. For more a detailed description go to [2 factor authentication](../user_documentation/guide-authentication.md).

## Login
*Request*
```
POST http://molgenis.mydomain.example/api/v1/login
Content-Type: application/json

{
    "username": "your username",
    "password": "your password"
}
```
*Response*
```
200 OK
{
    "token": "4296ef4fd9324360aa5c-bf8a849003da",
    "username": "admin",
    "firstname": "John",
    "lastname": "Doe"
}

OR
401 Unauthorized
```

The token can be used as authentication token in subsequent api calls. The token must be added to the http header:
```
x-molgenis-token: 4296ef4fd9324360aa5c-bf8a849003da
```

## Logout
*Request*
```
GET http://molgenis.mydomain.example/api/v1/logout

header:
x-molgenis-token: 4296ef4fd9324360aa5c-bf8a849003da
```
*Response*
```
200 OK
```

# Response codes
Code | Description
--- | ---
200 | Request ok, returned content in body
201 | Resource succesfully created
204 | Request ok
400 | Your request was not valid
401 | You are not authorized to perform this operation, did you authenticate?
404 | Resource does not exist
500 | Request ok but something went wrong on the server

# FAQ

How to resolve a 400 Bad Request error?
> Did you specify the Content-Type header if your body contains content?

What options exist to define query rules for resource collection requests?

> The query rules are serialized Java QueryRule objects, take a look at the source code of the QueryRule class to see what options are available.