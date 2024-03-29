# Permissions API

This API can be used to perform create, retrieval, update and delete operations on the MOLGENIS permission system.
The MOLGENIS permission is based on [Spring Security](https://spring.io/projects/spring-security).

## Parameters
These parameters are used by the endpoint in this API:
- 'typeId': This is the type of resource for which the permission is granted. 
This maps to the 'type' in Spring security's [ObjectIdentity](https://docs.spring.io/spring-security/site/docs/5.1.x/api/org/springframework/security/acls/model/ObjectIdentity.html).
In MOLGENIS examples of these types are 'entityType', 'package', 'plugin', and also row level secured entities, in which case the type is the entityType identifier prefixed with "entity-" 
- 'identifier': This is the identifier of the actual resource within the resource type for which the permission is granted. 
This maps to the 'identifier' in Spring security's ObjectIdentity.
- 'inheritance': a boolean indicating if inherited permissions should be returned or only the permission that are actually set for the roles and users requested. This parameter is only used in the GET permission requests. Setting this to true will return a tree with all inherited permissions for the requested users and roles. This field cannot be combined with paging.
- 'page': The pagenumber for the results, should only be provided combined with 'pageSize'. This field cannot be combined with 'inheritance=true'.
- 'pageSize': The number of results per result page, should only be provided combined with 'page'. This field cannot be combined with 'inheritance=true'.
- 'permission': A permission for a resource, the permissions that can be used differ per resource type, but are always a subset of READMETA,COUNT,READ,WRITE,WRITEMETA. 

## Query for user or role
- user
The user for which to get/create/update the permission. This should be the username as stated in the 'sys_sec_User' table in the MOLGENIS database.
- role
The role for which to get/create/update the permission. This should be the rolename as stated in the 'sys_sec_Role' table in the MOLGENIS database.

The user/role query for this API's GET operations should be provided in the [RSQL syntax](developer_documentation/ref-RSQL.md)
Examples:

Query for user 'Cardiologist' or users with role "CARDIOLOGY"
```
q=user==Cardiologist,role==CARDIOLOGY
```
Query for users 'Cardiologist' or 'Neurologist'
```
q=user=in=(Cardiologist,Neurologist)
```
### Permission inheritance
There are two kinds of inheritance in the permission system:
- Users inherit permissions from their roles, and roles from their parentroles.
- The access control list (ACL) for a resource can have a parent, in that case permissions on the parent also grant permissions on the child ACL's. In the current MOLGENIS system this is the case for entity types and packages, both inherit permissions from the package in which they reside..

## Managing row level security
### Getting all resource types in the system
##### Endpoint
```
GET https://molgenis.mydomain.example/api/permissions/types
```
##### Parameters
URL: none
Query: none

##### Response

| Status code         | Description                                                                       |
|---------------------|-----------------------------------------------------------------------------------|
|200 Ok | Success |
|403 Forbidden | the user has no permissions to execute this action |

##### Response body
List of ACL types available in the system.
##### Example 
Response:
```json
{
  "data": [
      {
          "id": "entity-sys_ImportRun",
          "entityType": "sys_ImportRun",
          "label": "Import"
      },
      {
          "id": "entity-sys_job_ResourceCopyJobExecution",
          "entityType": "sys_job_ResourceCopyJobExecution",
          "label": "Resource Copy Job Execution"
      },
      {
          "id": "entity-hospital_cardiology_patients",
          "entityType": "hospital_cardiology_patients",
          "label": "patients"
      },
      {
          "id": "package",
          "entityType": "sys_md_Package",
          "label": "Package"
      },
      {
          "id": "entity-sys_FileMeta",
          "entityType": "sys_FileMeta",
          "label": "File metadata"
      },
      {
          "id": "plugin",
          "entityType": "sys_Plugin",
          "label": "Plugin"
      },
      {
          "id": "entityType",
          "entityType": "sys_md_EntityType",
          "label": "Entity type"
      }
  ]
}
```

### Getting all suitable permissions for a resource type
##### Endpoint
```
GET https://molgenis.mydomain.example/api/permissions/types/permissions/{typeId}
```
##### Parameters
URL: TypeId as described in the [parameters section](##Parameters)

##### Response

| Status code         | Description                                                                       |
|---------------------|-----------------------------------------------------------------------------------|
|200 Ok | Success |
|403 Forbidden | the user has no permissions to execute this action |
|404 Not found | the type does not exist |

##### Response body
A list of permissions that can be used for this resource type.

##### Example 
Request:

```
https://molgenis.mydomain.example/api/permissions/types/permissions/entityType
```

Response:
```json
"data": {
  [
       "READMETA",
       "READ",
       "COUNT",
       "WRITEMETA",
       "WRITE"
   ]
}
```

### Creating a new resource type in the system (Row level securing an entity type)

This will create a new type in the system and add access control lists for all existing rows.
This enables row level security and is only enabled for superusers.
Please keep in mind that all existing rows will get the current user as owner.

##### Endpoint
```
POST https://molgenis.mydomain.example/api/permissions/types/{typeId}
```
##### Parameters
URL: 'typeId' as described in the [parameters section](##Parameters)

##### Response

| Status code         | Description                                                                       |
|---------------------|-----------------------------------------------------------------------------------|
|201 Created | Type created |
|403 Forbidden  | the user has no permissions to execute this action |
|409 Conflict | the type already exists |

##### Example
Enable row level security on entity type 'hospital_neurology_patients'.

Request:
```
https://molgenis.mydomain.example/api/permissions/types/entity-hospital_neurology_patients
```

### Deleting a resource type from the system (Removing row level security from an entity type)

This will delete a type, the access control lists for all rows will remain in the system.
This disables row level security.

##### Endpoint
```
DELETE https://molgenis.mydomain.example/api/permissions/types/{typeId}
```
##### Parameters
URL: 'typeId' as described in the [parameters section](##Parameters)

##### Response

| Status code         | Description                                                                       |
|---------------------|-----------------------------------------------------------------------------------|
|204 No content| Type removed |
|403 Forbidden  | the user has no permissions to execute this action |
|404 Not found | the type does not exist |

##### Example
Enable row level security on entity type 'hospital_neurology_patients'.

Request:
```
https://molgenis.mydomain.example/api/permissions/types/entity-hospital_neurology_patients
```

### Creating a new access control list for a resource
##### Endpoint
```
POST https://molgenis.mydomain.example/api/permissions/objects/{typeId}/{objectId}
```
##### Parameters
URL: 
- 'typeId' as described in the [parameters section](##Parameters)
- 'identifier' as described in the [parameters section](##Parameters)

##### Response

| Status code         | Description                                                                       |
|---------------------|-----------------------------------------------------------------------------------|
|201 Created | access control list created |
|400 Bad request| the permission request content is not valid |
|403 Forbidden  | the user has no permissions to execute this action |
|404 Not found | the type, object, user, role does not exist |
|409 Conflict | the a access control list already exists for this object |

##### Example
Add an ACL for 'Patient1' in _entity_ type 'hospital_neurology_patients'.

Request:
```
POST https://molgenis.mydomain.example/api/permissions/types/entity-hospital_neurology_patients/Patient1
```

### Getting all objects of a type
##### Endpoint
```
GET https://molgenis.mydomain.example/api/permissions/objects/{typeId}
```
##### Parameters
URL:
- 'typeId' as described in the [parameters section](##Parameters)
Query: 
- Optional: 'page' as described in the [parameters section](##Parameters)
- Optional: 'pageSize' as described in the [parameters section](##Parameters)

| Status code         | Description                                                                       |
|---------------------|-----------------------------------------------------------------------------------|
|200 Ok | Success |
|403 Forbidden | the user has no permissions to execute this action |
|404 Not found | the type, object, user, role or permission does not exist |

##### Response body
List of ACLs for a type in the system.
##### Example 
Request:
```
GET https://molgenis.mydomain.example/api/objects/entity-hospital_cardiology_patients
```
Response:
```json
{
    "page": {
        "size": 100,
        "totalElements": 3,
        "totalPages": 1,
        "number": 1
    },
    "links": {
        "self": "https://molgenis.mydomain.example/api/permissions/objects/entity-hospital_cardiology_patients?page=1&pageSize=100"
    },
    "data": [
        {
            "id": "Patient1",
            "label": "Patient1",
            "ownedByUser": "Cardiologist",
            "yours": false
        },
        {
            "id": "Patient2",
            "label": "Patient2",
            "ownedByRole": "CARDIOLOGY",
            "yours": true
        },
        {
            "id": "Patient3",
            "label": "Patient3",
            "yours": false
        }
    ]
}
```

## Retrieving permissions for users and roles

### Getting permissions for one or more users and/or roles for a resource
##### Endpoint
```
GET https://molgenis.mydomain.example/api/permissions/{typeId}/{objectId}
```
##### Parameters
URL: 
- 'typeId' as described in the [parameters section](##Parameters)
- 'identifier' as described in the [parameters section](##Parameters)
Query: 
- Optional: [Query for user or role](##Query for user or role)
- Optional: 'inheritance' as described in the [parameters section](##Parameters)

##### Response

| Status code         | Description                                                                       |
|---------------------|-----------------------------------------------------------------------------------|
|200 Ok | Success |
|403 Forbidden | the user has no permissions to execute this action |
|404 Not found | the type, object, user, role or permission does not exist |

##### Response body
A list of permissions objects containing the identifier and the label for the resource, the user or role that has this permission if any, and a list of [inherited permissions](#Permission inheritance).
The user and role of a permission can be absent if permission is only derived from inherited permissions. 

##### Example
Request:
```
GET https://molgenis.mydomain.example/api/permissions/package/hospital_neurology?inheritance=true
```
Response:
```json
"data": {
  {
       "permissions": [
           {
               "user": "Neurologist",
               "inheritedPermissions": [
                   {
                       "role": "NEUROLOGY",
                       "permission": "READ",
                       "inheritedPermissions": []
                   }
               ]
           },
           {
               "user": "NeuroNurse",
               "inheritedPermissions": [
                   {
                       "role": "NEUROLOGY",
                       "permission": "READ",
                       "inheritedPermissions": []
                   }
               ]
           },
           {
               "user": "Reception",
               "inheritedPermissions": [
                   {
                      "object":{
                        "id": "hospital",
                        "label": "hospital"
                        "yours": false
                      },
                      "type":{
                        "label": "Package",
                        "id": "package"
                      },
                      "permission": "READ",
                      "inheritedPermissions": []
                   }
               ]
           }
       ]
   }
 }
```
The neurologist and the nurse inherit their READ permissions from the their "NEUROLOGY" role, while the reception inherits the READ permission from the READ permission they have on the parent package "hospital" of the "hospital_neurology" package.

### Getting permissions for a resource type
##### Endpoint
```
GET https://molgenis.mydomain.example/api/permissions/{typeId}
```
##### Parameters
URL: 
- 'typeId' as described in the [parameters section](##Parameters)
Query: 
- Optional: [Query for user or role](##Query for user or role)
- Optional: 'inheritance' as described in the [parameters section](##Parameters)
- Optional: 'page' as described in the [parameters section](##Parameters)
- Optional: 'pageSize' as described in the [parameters section](##Parameters)

##### Response

| Status code         | Description                                                                       |
|---------------------|-----------------------------------------------------------------------------------|
|200 Ok |  |
|403 Forbidden | the user has no permissions to execute this action |
|404 Not found | the type, object, user, role or permission does not exist |

##### Response body
A list of permissions per resource is returned in the 'data' field.
These lists of permissions contain object containing the 'identifier' and the 'label' for the resource, the user or role that has this permission if any, and a list of [inherited permissions](#Permission inheritance) if any.
The 'user'/'role' and 'permission' of a permission can be absent if permission is only derived from inherited permissions. 
A links object is returned with a URL for the current result page in the 'self' field. If available a 'previous' and 'next' page are returned with links to the previous and next page of results/
Optionally a 'page' object is returned containing the size and number of the current page and a 'totalElements' field containing the total number of results and 'totalPages' containing the total number of queries.
The page object is only returned if the request contained paging parameters

##### Example
Request:
```
https://molgenis.mydomain.example/api/permissions/entityType?q=user==Cardiologist,role==CARDIOLOGY&page=1&pageSize=10
```
Response:
```json
{
    "page": {
        "size": 10,
        "totalElements": 2,
        "totalPages": 1,
        "number": 1
    },
    "links": {
        "self": "/api/permissions/entityType?q=user==Cardiologist,role==CARDIOLOGY&page=1&pageSize=10"
    },
    "data": {
        "id": "entityType",
        "label": "Entity type",
        "objects": [
            {
                "id": "hospital_cardiology_patients",
                "label": "Cardiology Patients",
                "ownedByUser": "admin",
                "yours": true,
                "permissions": [
                    {
                        "role": "CARDIOLOGY",
                        "permission": "WRITE"
                    }
                ]
            },
            {
                "id": "hospital_cardiology_results",
                "label": "Cardiology Results",
                "ownedByUser": "admin",
                "yours": true,
                "permissions": [
                    {
                        "user": "Cardiologist",
                        "permission": "WRITE"
                    }
                ]
            }
        ]
    }
}
```



### Getting all permissions
##### Endpoint
```
GET https://molgenis.mydomain.example/api/permissions
```
##### Parameters
URL: none
Query: 
[Query for user or role](##Query for user or role)

##### Response

| Status code         | Description                                                                       |
|---------------------|-----------------------------------------------------------------------------------|
|200 Ok | Success |
|403 Forbidden | the user has no permissions to execute this action |
|404 Not found | the type, object, user, role or permission does not exist |

##### Response body
A list of permissions on resources per resource type.
For every resource type a object is returned containing the typeId, a label for the typeId, a label for the resource type and a list of permissions per resource.
These lists of permissions contain object containing the identifier and the label for the resource, the user or role that has this permission if any, and a list of [inherited permissions](#Permission inheritance).
The user and role of a permission can be absent if permission is only derived from inherited permissions.


##### Example 
Request:
```
https://molgenis.mydomain.example/api/permissions?q=user==Cardiologist&inheritance=true
```

Response:
```json
{
  "data": {
    "permissions": [
      {
        "role": "CARDIOLOGY",
        "object": {
          "id": "hospital_cardiology_patients",
          "label": "Cardiology Patients",
          "yours": false
        },
        "type": {
          "id": "entityType",
          "entityType": "sys_md_EntityType",
          "label": "Entity type"
        },
        "permission": "READ"
      },
      {
        "user": "Cardiologist",
        "object": {
          "id": "hospital_cardiology_patients",
          "label": "Cardiology Patients",
          "yours": false
        },
        "type": {
          "id": "entityType",
          "entityType": "sys_md_EntityType",
          "label": "Entity type"
        },
        "inheritedPermissions": [
          {
            "role": "CARDIOLOGY",
            "permission": "READ",
            "inheritedPermissions": []
          }
        ]
      },
      {
        "user": "Cardiologist",
        "object": {
          "id": "hospital_cardiology_results",
          "label": "Results",
          "yours": false
        },
        "type": {
          "id": "entityType",
          "entityType": "sys_md_EntityType",
          "label": "Entity type"
        },
        "permission": "WRITE"
      },
      {
        "role": "CARDIOLOGY",
        "object": {
          "id": "dataexplorer",
          "label": "dataexplorer",
          "yours": false
        },
        "type": {
          "id": "plugin",
          "entityType": "sys_Plugin",
          "label": "Plugin"
        },
        "permission": "READ"
      },
      {
        "user": "Cardiologist",
        "object": {
          "id": "dataexplorer",
          "label": "dataexplorer",
          "yours": false
        },
        "type": {
          "id": "plugin",
          "entityType": "sys_Plugin",
          "label": "Plugin"
        },
        "inheritedPermissions": [
          {
            "role": "CARDIOLOGY",
            "permission": "READ",
            "inheritedPermissions": []
          }
        ]
      },
      {
        "role": "CARDIOLOGY",
        "object": {
          "id": "home",
          "label": "home",
          "yours": false
        },
        "type": {
          "id": "plugin",
          "entityType": "sys_Plugin",
          "label": "Plugin"
        },
        "permission": "READ"
      },
      {
        "user": "Cardiologist",
        "object": {
          "id": "home",
          "label": "home",
          "yours": false
        },
        "type": {
          "id": "plugin",
          "entityType": "sys_Plugin",
          "label": "Plugin"
        },
        "inheritedPermissions": [
          {
            "role": "CARDIOLOGY",
            "permission": "READ",
            "inheritedPermissions": []
          }
        ]
      }
    ]
  }
}
```


## Granting permissions

### Creating permissions for a resource
##### Endpoint
```
POST https://molgenis.mydomain.example/api/permissions/{typeId}/{objectId}
```
##### Request
The endpoint expects a list of permissions, each permission should contain a 'permission' and a 'user' or a 'role'.

##### Response

| Status code         | Description                                                                       |
|---------------------|-----------------------------------------------------------------------------------|
|201 No content | permissions created |
|400 Bad request| the permission request content is not valid |
|403 Forbidden  | the user has no permissions to execute this action |
|404 Not found | the type, object, user, role does not exist |
|409 Conflict | the a permission already exists for this combination of user/role and object |

##### Example request
URL:
```
https://molgenis.mydomain.example/api/permissions/entityType/hospital_cardiology_patients
```
Body:
```json
{
  "permissions":[{
    "permission": "READ",
    "role": "CARDIOLOGY"
  },{
    "permission": "WRITE",
    "user": "Cardiologist"
  }]
}
```

### Create permissions for multiple resources
```
POST https://molgenis.mydomain.example/api/permissions/{typeId}")
``` 

##### Request
The endpoint expects a list of resources, each of which should contrain the identifier for the
resource and may contain a list of permissions, each of these permission should contain a
'permission' and a 'user' or a 'role'.
If you want to update ownership, you can also add an 'ownedByUser' or an 'ownedByRole' field.

##### Response 

| Status code         | Description                                                                       |
|---------------------|-----------------------------------------------------------------------------------|
|201 No content | permissions created |
|400 Bad request| the permission request content is not valid |
|403 Forbidden  | the user has no permissions to execute this action |
|404 Not found | the type, object, user, role does not exist |
|409 Conflict | the a permission already exists for this combination of user/role and object |

##### Example
URL: 
```
https://molgenis.mydomain.example/api/permissions/entity-hospital_neurology_patients
```
Body:
```json
{ "data": {
    "objects":[{
        "objectId": "Patient1",
        "permissions": [
        {
          "role": "CARDIOLOGY",
          "permission": "WRITE"
        }
      ]
    },{
        "objectId": "Patient2",
        "permissions": [
        {
          "user": "Cardiologist",
          "permission": "WRITE"
        },
        {
          "user": "CardioNurse",
          "permission": "READ"
        }
      ]}
    ]
  }
}
```

### Update permissions and/or ownership for one resource
##### Endpoint
```
PATCH https://molgenis.mydomain.example/api/permissions/{typeId}/{objectId}")
```
Request: 
The endpoint expects a list of permissions, each permission should contain a 'permission' and a 'user' or a 'role'.
If you want to update ownership, you can also add an 'ownedByUser' or an 'ownedByRole' field.  

##### Response

| Status code         | Description                                                                       |
|---------------------|-----------------------------------------------------------------------------------|
|204 No content | permissions updated |
|400 Bad request| the permission request content is not valid |
|403 Forbidden  | the user has no permissions to execute this action |
|404 Not found | the type, object, user, role or permission does not exist |

##### Example 
Request:
URL: 
```
https://molgenis.mydomain.example/api/permissions/entityType/hospital_cardiology_patients
```
Body example 1: Update permissions (only works if the resource already has permissions):
```json
{
  "permissions": [{
    "permission": "WRITE",
    "role": "CARDIOLOGY"
  },{
    "permission": "WRITEMETA",
    "user": "Cardiologist"
  }]
}
```

Body example 2: Update ownership
```json
{
  "ownedByUser": "Cardiologist"
}
```

Body example 3: Update ownership and permissions (only works if the resource already has permissions):
```json
{
  "ownedByUser": "Cardiologist",
  "permissions": [{
    "permission": "WRITE",
    "role": "CARDIOLOGY"
  }]
}
```

### Update permissions and/or ownership for multiple resources
```
PATCH https://molgenis.mydomain.example/api/permissions/{typeId}")
``` 

##### Request
The endpoint expects a list of resources, each of which should contain the identifier for the
resource and may contain a list of permissions. Each of these permissions should contain a
'permission' and a 'user' or a 'role'.
If you want to update ownership, you can also add an 'ownedByUser' or an 'ownedByRole' field.

##### Response 

| Status code         | Description                                                                       |
|---------------------|-----------------------------------------------------------------------------------|
|204 No content | permissions updated |
|400 Bad request| the permission request content is not valid |
|403 Forbidden  | the user has no permissions to execute this action |
|404 Not found | the type, object, user, role or permission does not exist |

##### Example
URL: 
```
https://molgenis.mydomain.example/api/permissions/entity-hospital_neurology_patients
```
Body:
```json
{
    "objects": [
      {
        "objectId": "Patient1",
        "ownedByRole": "CARDIOLOGY",
        "permissions": [
          {
            "role": "CARDIOLOGY",
            "permission": "WRITE"
          }
        ]
      },
      {
        "objectId": "Patient2",
        "label": "Patient2",
        "ownedByUser": "Cardiologist",
        "permissions": [
          {
            "user": "CardioNurse",
            "permission": "READ"
          },
          {
            "user": "Cardiologist",
            "permission": "WRITE"
          }
        ]
      }
    ]
}
```

### Removing permissions for a resource

##### Endpoint
```
DELETE https://molgenis.mydomain.example/api/permissions/{typeId}/{objectId}
```

##### Parameters
- 'typeId' as described in the [parameters section](##Parameters)
- 'identifier' as described in the [parameters section](##Parameters)
Body: 
a json object with either a ['user' or a 'role'](##Query for user or role) field for the user/role which the permission should be deleted.
The field takes a single user or role. 

##### Response: 

| Status code         | Description                                                                       |
|---------------------|-----------------------------------------------------------------------------------|
|204 No content | Permission deleted |
|400 Bad request | the permission request content is not valid |
|403 Forbidden | the user has no permissions to execute this action |
|404 Not found | the type, object, user, role or permission does not exist |

##### Example 
URL:
```
https://molgenis.mydomain.example/api/permissions/entityType/hospital_cardiology_patients
```

Body 
```json
{
  "user": "Cardiologist"
}
```
