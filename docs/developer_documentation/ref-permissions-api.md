# Permission API

This API can be used to perform create, update and delete operations on the MOLGENIS permission system.
The MOLGENIS permission is based on [Spring Security](https://spring.io/projects/spring-security).

## Parameters
These parameters are used by the endpoint in this API:
- 'typeId': This is the type of resource for which the permission is granted. 
This maps to the 'type' in Spring security's [ObjectIdentity](https://docs.spring.io/spring-security/site/docs/4.2.11.RELEASE/apidocs/org/springframework/security/acls/model/ObjectIdentity.html).
In MOLGENIS #####Examples of these types are 'entityType', 'package', 'plugin', and also row leve secured entities, in which case the type is the entityType identifier perfixed with "-entity" 
- 'identifier': This is the identifier of the actual resource within the resource type for which the permission is granted. 
This maps to the 'identifier' in Spring security's ObjectIdentity.
- 'inheritance': a boolean indicating if inherited permissions should be returned or only the permission that are actually set for the roles and users requested. This parameter is only used in the the GET permission requests. Setting this to true will return a tree with all inherited permissions for the requested users and roles. This field cannot be combined with paging.
- 'page': The pagenumber for the results, should only be provided combined with 'pageSize'. This field cannot be combined with 'inheritance=true'.
- 'pageSize': The number of results per result page, should only be provided combined with 'page'. This field cannot be combined with 'inheritance=true'.
- 'permission': A permission for a resource, the permissions that can be used differ per resource type, but are always a subset of READMETA,COUNT,READ,WRITE,WRITEMETA. 

## Query for user or role
- user
The user for which to get/create/update the permission. This should be the username as stated in the 'sys_sec_User' table in the MOLGENIS database;
- role
The role for which to get/create/update the permission. This should be the rolename as stated in the 'sys_sec_Role' table in the MOLGENIS database;

The user/role query for this API's GET operations should be provided in the [RSQL syntax](developer_documentation/ref-RSQL.md)

Query for user or role(##Query for user or role)

### Permission inheritance
There are two kinds of inheritance in the permission system:
- Users inherit permissions from their roles, and roles from their parentroles.
- The access control list for a resource can have a parent, in that case permissions on the parent also grant permissions on the child ACL's. In the current MOLGENIS system this is the case for entity types and packages, both inherit permissions from the package in which they reside..

## Managing row level security
### Getting all resource types in the system
##### Endpoint
```
GET https://molgenis.mydomain.example/api/permissions/v1/types
```
##### Parameters
URL: none
Query: none

##### Response
List of ACL types available in the system.
##### Example 
Response:
```
[
       "package",
       "entity-sys_job_ResourceDownloadJobExecution",
       "entity-sys_FileMeta",
       "entity-sys_ImportRun",
       "entity-sys_job_OneClickImportJobExecution",
       "entity-sys_job_ResourceDeleteJobExecution",
       "entity-sys_job_ResourceCopyJobExecution",
       "entityType",
       "plugin",
       "entity-hospital_cardiology_patients"
   ]
   ```

### Getting all suitable permissions for a resource type
##### Endpoint
```
GET https://molgenis.mydomain.example/api/permissions/v1/types/{typeId}")
```
##### Parameters
URL: TypeId as described in the [parameters section](##Parameters)

##### Response
A list of permissions that can be used for this resource type.

##### Example 
Request:
```https://molgenis.mydomain.example/api/permissions/v1/types/permissions/entityType```
Response:
```[
       "READMETA",
       "READ",
       "COUNT",
       "WRITEMETA",
       "WRITE"
   ]
   ```

### Creating a new resource type in the system (Row level securing an entity)

Warning: this will not create ACL's for already existing resources of this type.
You can use the endpoint in the next section 'Creating a new access control list for a resource' to create ACL's for these resources.

##### Endpoint
```
POST https://molgenis.mydomain.example/api/permissions/v1/types/{typeId}")
```
########## Parameters
URL: 'typeId' as described in the [parameters section](##Parameters)

########## Response
201 CREATED

########## Example
Enable row level security on entity 'hospital_neurology_patients'.

Request:
```https://molgenis.mydomain.example/api/permissions/v1/types/entity-hospital_neurology_patients```


### Creating a new access control list for a resource
##### Endpoint
```
POST https://molgenis.mydomain.example/api/permissions/v1/objects/{typeId}/{objectId}")
```
##### Parameters
URL: 
- 'typeId' as described in the [parameters section](##Parameters)
- 'identifier' as described in the [parameters section](##Parameters)

##### Response
201 CREATED

##### Example
Add an ACL for 'Patient1' in entity 'hospital_neurology_patients'.

Request:
```POST https://molgenis.mydomain.example/api/permissions/v1/types/entity-hospital_neurology_patients/Patient1```

### Getting all acls for a class
##### Endpoint
```
GET https://molgenis.mydomain.example/api/permissions/v1/objects/{typeId}
```
##### Parameters
URL:
- 'typeId' as described in the [parameters section](##Parameters)
Query: 
- Optional: 'page' as described in the [parameters section](##Parameters)
- Optional: 'pageSize' as described in the [parameters section](##Parameters)

##### Response
List of ACLs for a class in the system.
##### Example 
Request:
```
GET https://molgenis.mydomain.example/api/permissions/v1/objects/plugin?page=2&pageSize=2
```
Response:
```
{
    "page": {
        "size": 2,
        "totalElements": 36,
        "totalPages": 18,
        "number": 2
    },
    "links": {
        "previous": "/api/permissions/v1/objects/plugin?page=1&pageSize=2",
        "self": "/api/permissions/v1/objects/plugin?page=2&pageSize=2",
        "next": "/api/permissions/v1/objects/plugin?page=3&pageSize=2"
    },
    "data": [
        "background",
        "contact"
    ]
}
   ```

## Retrieving permissions for users and roles

### Getting permissions for one or more users and/or roles for a resource
##### Endpoint
```
GET https://molgenis.mydomain.example/api/permissions/v1/{typeId}/{objectId}")
```
##### Parameters
URL: 
- 'typeId' as described in the [parameters section](##Parameters)
- 'identifier' as described in the [parameters section](##Parameters)
Query: 
- Optional: [Query for user or role](##Query for user or role)
- Optional: 'inheritance' as described in the [parameters section](##Parameters)
##### Response

A list of permissions objects containing the identifier and the label for the resource, the user or role that has this permission if any, and a list of [inherited permissions](###Permission inheritance).
The user and role of a permission can be absent if permission is only derived from inherited permissions. 

##### Example
Request:
```
GET https://molgenis.mydomain.example/api/permissions/v1/package/hospital_neurology?inheritance=true
```
Response:
```
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
                       "identifier": "hospital",
                       "label": "hospital",
                       "classLabel": "Package",
                       "classId": "package",
                       "permission": "READ",
                       "inheritedPermissions": []
                   }
               ]
           }
       ]
   }
   ```
The neurologist and the nurse inherit their READ permissions from the their "NEUROLOGY" role, while the reception inherits the READ permission from the READ permission they have on the parent package "hospital" of the "hospital_neurology" package.

### Getting permissions for one or more users and/or roles for a resource type
##### Endpoint
```
GET https://molgenis.mydomain.example/api/permissions/v1/{typeId}")
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

A list of permissions per resource is returned in the 'data' field.
These lists of permissions contain object containing the 'identifier' and the 'label' for the resource, the user or role that has this permission if any, and a list of [inherited permissions](###Permission inheritance) if any.
The 'user'/'role' and 'permission' of a permission can be absent if permission is only derived from inherited permissions. 
A links object is returned with a URL for the current result page in the 'self' field. If available a 'previous' and 'next' page are returned with links to the previous and next page of results/
Optionally a 'page' object is returned containing the size and number of the current page and a 'totalElements' field containing the total number of results and 'totalPages' containing the total number of queries.
The page object is only returned if the request contained paging parameters

##### Example
Request:
```https://molgenis.mydomain.example/api/permissions/v1/entityType?q=user==Cardiologist,role==CARDIOLOGY&page=1&pageSize=10```
Response:
```
{
    "page": {
        "size": 10,
        "totalElements": 2,
        "totalPages": 1,
        "number": 1
    },
    "links": {
        "self": "/api/permissions/v1/entityType?q=user==Cardiologist,role==CARDIOLOGY&page=1&pageSize=10"
    },
    "data": {
        "identityPermissions": [
            {
                "identifier": "hospital_cardiology_patients",
                "label": "patients",
                "permissions": [
                    {
                        "role": "CARDIOLOGY",
                        "permission": "WRITE"
                    }
                ]
            },
            {
                "identifier": "hospital_cardiology_results",
                "label": "results",
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



### Getting all permissions for one or more users and/or roles
##### Endpoint
```
GET https://molgenis.mydomain.example/api/permissions/v1
```
##### Parameters
URL: none
Query: 
[Query for user or role](##Query for user or role)

##### Response
A list of permissions on resources per resource class.
For every resource type a object is returned containing the classId, a label for the classId, a label for the resource type and a list of permissions per resource.
These lists of permissions contain object containing the identifier and the label for the resource, the user or role that has this permission if any, and a list of [inherited permissions](###Permission inheritance).
The user and role of a permission can be absent if permission is only derived from inherited permissions.


##### Example 
Request:
```https://molgenis.mydomain.example/api/permissions/v1?q=user==Cardiologist&includeInheritance=true```
Response:
```
{
    "classPermissions": [
        {
            "classId": "package",
            "label": "Package",
            "rowPermissions": [
                {
                    "identifier": "hospital_cardiology",
                    "label": "hospital_cardiology",
                    "permissions": [
                        {
                            "role": "CARDIOLOGY",
                            "permission": "READ"
                        }
                    ]
                }
            ]
        },
        {
            "classId": "entityType",
            "label": "Entity type",
            "rowPermissions": [
                {
                    "identifier": "hospital_cardiology_results",
                    "label": "results",
                    "permissions": [
                        {
                            "user": "Cardiologist",
                            "permission": "WRITE",
                            "inheritedPermissions": [
                                {
                                    "identifier": "hospital_cardiology",
                                    "label": "hospital_cardiology",
                                    "classLabel": "Package",
                                    "classId": "package",
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
                    ]
                }
            ]
        },
        {
            "classId": "plugin",
            "label": "Plugin",
            "rowPermissions": [
                {
                    "identifier": "dataexplorer",
                    "label": "dataexplorer",
                    "permissions": [
                        {
                            "role": "CARDIOLOGY",
                            "permission": "READ",
                            "inheritedPermissions": []
                        }
                    ]
                },
                {
                    "identifier": "navigator",
                    "label": "navigator",
                    "permissions": [
                        {
                            "role": "CARDIOLOGY",
                            "permission": "READ",
                            "inheritedPermissions": []
                        }
                    ]
                }
            ]
        }
    ]
}
```


## Granting permissions to users and roles

### Creating permissions for one or more users and/or roles for a resource
##### Endpoint
```
POST https://molgenis.mydomain.example/api/permissions/v1/{typeId}/{objectId}")
```
##### Request
The endpoint expects a list of permissions, each permission should contain a 'permission' and a 'user' or a 'role'.

##### Response
201 CREATED

##### Example request
URL:
https://molgenis.mydomain.example/api/permissions/v1/entityType/hospital_cardiology_patients
Body:
```{
   	permissions:[{
   		permission:READ,
   		role:CARDIOLOGY
   	},{
   		permission:WRITE,
   		user:Cardiologist
   	}]
   }
   ```

### Create permissions for one or more users and/or roles for resources of a certain type
```
POST https://molgenis.mydomain.example/api/permissions/v1/{typeId}")
``` 

##### Request
The endpoint expects a list of resources, each of which should contrain the identifier for the resource and a list of permissions, each of these permission should contain a 'permission' and a 'user' or a 'role'.

##### Response 
201 CREATED

##### Example
URL: https://molgenis.mydomain.example/api/permissions/v1/entity-hospital_neurology_patients
Body:
```
{
	rows:[{
			identifier:Patient1,
			permissions:[
			{
				role:CARDIOLOGY,
				permission:WRITE
			}
		]
	},{
			identifier:Patient2,
			permissions:[
			{
				user:Cardiologist,
				permission:WRITE
			},
			{
				user:CardioNurse,
				permission:READ
			}
		]}
	]
}
```

### Update permissions for one or more users and/or roles for a resource type
##### Endpoint
```
PATCH https://molgenis.mydomain.example/api/permissions/v1/{typeId}/{objectId}")
```
Request: 
The endpoint expects a list of permissions, each permission should contain a 'permission' and a 'user' or a 'role'.

##### Response
200 OK

##### Example 
Request:
URL: https://molgenis.mydomain.example/api/permissions/v1/entityType/hospital_cardiology_patients
Body:
```{
   	permissions:[{
   		permission:WRITE,
   		role:CARDIOLOGY
   	},{
   		permission:WRITEMETA,
   		user:Cardiologist
   	}]
   }
   ```

### Update permissions for one or more users and/or roles for resources of a certain type
```
PATCH https://molgenis.mydomain.example/api/permissions/v1/{typeId}")
``` 

##### Request
The endpoint expects a list of resources, each of which should contrain the identifier for the resource and a list of permissions, each of these permission should contain a 'permission' and a 'user' or a 'role'.

##### Response 
200 OK

##### Example
URL: https://molgenis.mydomain.example/api/permissions/v1/entity-hospital_neurology_patients
Body:
```
{
	rows:[{
			identifier:Patient1,
			permissions:[
			{
				role:CARDIOLOGY,
				permission:WRITE
			}
		]
	},{
			identifier:Patient2,
			permissions:[
			{
				user:Cardiologist,
				permission:WRITE
			},
			{
				user:CardioNurse,
				permission:READ
			}
		]}
	]
}
```

### Removing permissions for one or more users and/or roles for a resource

##### Endpoint
```
DELETE https://molgenis.mydomain.example/api/permissions/v1/{typeId}/{objectId}")
```

##### Parameters
- 'typeId' as described in the [parameters section](##Parameters)
- 'identifier' as described in the [parameters section](##Parameters)
Body: 
a json object with either a ['user' or a 'role'](##Query for user or role) field for the user/role which the permission should be deleted.
The field takes a single user or role. 

##### Response: 
200 OK

##### Example 
Request:

URL 
```
https://molgenis.mydomain.example/api/permissions/v1/entityType/hospital_cardiology_patients
```

Body 
```
{
  user:Cardiologist
}
```
