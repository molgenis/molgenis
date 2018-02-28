**
We are expanding and improving the REST api. At its heart is a simplified query syntax called RSQL for collection queries
For query syntax see [RSQL parser](https://github.com/jirutka/rsql-parser). In addition we simplify the reading of entities.
**

# Instance

## Read instance
Key | Type | Description
--- | --- | ---
*attrs* | Comma-separated string | Defines which fields in the API response to select
*_method* | HTTP method | Tunnel request through defined method over default API operation

*Request*
```
GET http://molgenis.mydomain.example/api/v2/<entity_name>/<entity_id>
GET http://molgenis.mydomain.example/api/v2/<entity_name>/<entity_id>?attrs=attr0
GET http://molgenis.mydomain.example/api/v2/<entity_name>/<entity_id>?attrs=attr0,attr1
GET http://molgenis.mydomain.example/api/v2/<entity_name>/<entity_id>?attrs=attr0(subattr0,subattr1),attr1(*)
```
```
POST http://molgenis.mydomain.example/api/v2/<entity_name>/<entity_id>?_method=GET
```
```
POST http://molgenis.mydomain.example/api/v2/<entity_name>/<entity_id>?_method=GET
attrs=attr0(subattr0,subattr1),attr1(*)
```

## Delete instance

*Request*
```
DELETE http://molgenis.mydomain.example/api/v2/<entity_name>/<entity_id>
```

# Query

We use the [RSQL](https://github.com/jirutka/rsql-parser) HTTP/URL based query language:

Key | Type | Description
--- | --- | ---
*q* | Query string in RSQL | RSQL query to filter the entity collection response
*sort* | Query string in RSQL | Defines how entity collection response is sorted
*attrs* | Comma-separated string | Defines which fields in the API response to select
*start* | int | Offset in resource collection
*num* | int | Number of resources to retrieve starting at *start*
*_method* | HTTP method | Tunnel request through defined method over default API operation

```
GET http://molgenis.mydomain.example/api/v2/<entity_name>
GET http://molgenis.mydomain.example/api/v2/<entity_name>?attrs=attr0
GET http://molgenis.mydomain.example/api/v2/<entity_name>?attrs=attr0,attr1
GET http://molgenis.mydomain.example/api/v2/<entity_name>?attrs=attr0(subattr0,subattr1),attr1(*)
GET http://molgenis.mydomain.example/api/v2/<entity_name>?sort=attr0
GET http://molgenis.mydomain.example/api/v2/<entity_name>?sort=attr0:asc
GET http://molgenis.mydomain.example/api/v2/<entity_name>?sort=attr0:desc
GET http://molgenis.mydomain.example/api/v2/<entity_name>?sort=attr0:desc,attr1
GET http://molgenis.mydomain.example/api/v2/<entity_name>?start=40&num=20
GET http://molgenis.mydomain.example/api/v2/<entity_name>?q=attr0==val
GET http://molgenis.mydomain.example/api/v2/<entity_name>?q=attr0!=val
GET http://molgenis.mydomain.example/api/v2/<entity_name>?q=attr0!=val;q=attr1=ge=5
```

See the [MOLGENIS RSQL documentation](./ref-RSQL.md) for all supported operators in MOLGENIS and matching examples.

## Aggregation

We also support aggregation of the result query:

Key | Type | Description
--- | --- | ---
*q* | Query string in RSQL | RSQL query to filter the entity collection response
*aggs* | Aggregation query string in RSQL | Aggregation query to aggregate entities

The aggregation query supports the RSQL selectors 'x', 'y' and 'distinct' and the RSQL operator '=='. The selector 'x' defines the first aggregation attribute name, 'y' defines the second aggregation attribute name, 'distinct' defines the distinct aggregation attribute name.

```
GET http://molgenis.mydomain.example/api/v2/<entity_name>?aggs=x==attr0
GET http://molgenis.mydomain.example/api/v2/<entity_name>?aggs=x==attr0;y==attr1
GET http://molgenis.mydomain.example/api/v2/<entity_name>?aggs=x==attr0;y==attr1;distinct=attr2
GET http://molgenis.mydomain.example/api/v2/<entity_name>?aggs=x==attr0;y==attr1;distinct=attr2&q=attr4==val
```

## Including categorical options

For simple lookup lists, it might be a convenience to include the list in the initial API response.
You can do this for CATEGORICAL, and CATEGORICAL_MREF attributes using `includeCategories`.

Not including the query option will set it to the default `false`.

Key | Type | Description
--- | --- | ---
*includeCategories* | boolean | Includes a list of categorical options in attribute metadata for CATEGORICAL and CATEGORICAL_MREF attributes

## Example request - response
**TableA**

| id | category |
|----|----------|
| 1  | A        |

**TableB**

| id | label |
|----|-------|
| A  | A very awesome category |
| B  | A very busy category |
| C  | A very complex category |

*Request*
```
GET http://molgenis.mydomain.example/api/v2/TableA?includeCategories=true
```

*Response*
```json
{
  "meta": {
    "attributes": [
      {
        "name": "id"
      },
      {
        "name": "category",
        "categoricalOptions": [
          {
            "id": "A",
            "label": "A very awesome category"
          },
          {
            "id": "B",
            "label": "A very busy category"
          },
          {
            "id": "C",
            "label": "A very complex category"
          }
        ]
      }
    ]
  }
}
```

# Batch 

When working with larger datasets the RESTv2 api provides batching via the 'entities' parameter:

## Create

To create/add a list of entities into a collection you can POST the 'entities' parameter to a collection:

*Request*
```
POST http://molgenis.mydomain.example/api/v2/person
Content-Type: application/json

{entities: [
{
    "age": "101",
    "driverslicence": true
},
{
    "age": "102",
    "driverslicence": true
}
]}

```
*Response*
```
201 Created

```
Body
```
{
    location: "/api/v2/Person?q=id=in=("1","2")"
    resources: [{
        href: "/api/v2/Person/1"
    },
    {
        href: "/api/v2/Person/2"
    }]
}

* href: "/api/v2/Person/2" returns a created resource
* location: /api/v2/person/?q=id=in=(1,2) returns all created resources
```
## Update

To batch update a list of entities of a collection you can PUT the 'entities' parameter to a collection:

*Request*
```
PUT http://molgenis.mydomain.example/api/v2/person
Content-Type: application/json

{entities: [
{
    "id": 1,
    "age": "11",
    "driverslicence": true
},
{
    "id": 2,
    "age": "12",
    "driverslicence": true
}
]}

```
*Response*
```
204 No Content
```

## Delete (since v3.0.0)

To delete a list of entities of a collection you can DELETE the 'entityIds' parameter to a collection:

*Request*
```
DELETE http://molgenis.mydomain.example/api/v2/person
Content-Type: application/json

{
    entityIds: ["person0", "person1"]
}

```
*Response*
```
204 No Content

```
Body
```
{
    location: "/api/v2/Person?q=id=in=("1","2")"
    resources: [{
        href: "/api/v2/Person/1"
    },
    {
        href: "/api/v2/Person/2"
    }]
}

## One value

If you only want to change one attribute without needing to provide all other attributes:

*Request*
```
PUT http://molgenis.mydomain.example/api/v2/person/age
Content-Type: application/json

{entities: [
{
    "id": 1,
    "age": "1"
},
{
    "id": 2,
    "age": "2"
}
]}
```
*Response*
```
204 No Content
```
