** 
The MOLGENIS python API client allows you to retrieve, create, update and delete entities using python.
**

Download molgenis.py from a molgenis server for instance using wget in the folder you want to run your molgenis api
```
wget https://molgenis.mydomain.example/molgenis.py
``` 
Now you can create a python script. 
To get started, you should import the python api, connect to a molgenis server and login:
```python
import molgenis
session = molgenis.Session.("https://molgenis.mydomain.example/api/")
session.login("username","password")
```
Always put the import and molgenis.Session in your script to make the api work. 

# Overview example
```python
import molgenis
session = molgenis.Session.("https://molgenis.mydomain.example/api/")
session.login("username","password")
my_table = session.get("package_entityName")
print(my_table)
```
To get a full overview of all functions in the python api, download the following zip:
[MolgenisPythonFunctions.zip](../data/MolgenisPythonFunctions.zip)
# Methods
## login
```python
session.login(username,password)
```
Login to the MOLGENIS REST API<br/><br/>

Argument    | Description                                       | Required | Default
------------|---------------------------------------------------|----------|--------
`username`  | Username for a registered molgenis user           | yes      |
`password`  | Password for the user                             | yes      | 

## logout
```python
session.logout()
```
Logout from the MOLGENIS REST API and destroy the session.


##getById
```python
session.getById("tableId", "rowId")
```
Retrieves a single entity row from an entity repository.<br/><br/>

Argument    | Description                                       | Required | Default
------------|---------------------------------------------------|----------|--------
`entity`    | Fully qualified name of the entity                | yes      |
`id`        | The value for the idAttribute of the entity       | yes      | 
`attributes`|  The list of attributes to retrieve               | no       | All attributes
`expand`    | The attributes to expand                          | no       | None


##get
```python
session.get("package_entityName")
```

Retrieves entities and returns the result in a dataframe.

Argument    | Description                                          | Required | Default
------------|------------------------------------------------------|----------|--------
`entity`    | Fully qualified name of the entity                   | yes      |
`q`         | Query string in rsql/fiql format (see below)         | no       | None
`start`	    | The index of the first row to retrieve (zero indexed)| no       | 0
`num`       | The maximum number of rows to return (max 10000)     | no       | 100
`attributes`| The list of attributes to retrieve                   | no       | All attributes
`sortColumn`| attributeName of the column to sort on               | no       | None
`sortOrder` | The order to sort in                                 | no       | None


**Supported RSQL/FIQL query operators (see [https://github.com/jirutka/rsql-parser](https://github.com/jirutka/rsql-parser))**

Operator|Symbol
--------|------
Logical AND | `;` or `and`
Logical OR	| `,` or `or`
Group | `(` and `)`
Equal to | `==`
Less then | `=lt=` or `<`
Less then or equal to | `=le=` or `<=`
Greater than | `=gt=` or `>`
Greater tha or equal to | `=ge=` or `>=`

Argument can be a single value, or multiple values in parenthesis separated by comma. Value that doesn’t contain any reserved character or a white space can be unquoted, other arguments must be enclosed in single or double quotes.			
			
**Examples**

```python
session.get("celiacsprue")
session.get("celiacsprue", num = 100000, start = 1000)
session.get("celiacsprue", attributes = c("Individual", "celiac_gender"))
session.get("celiacsprue", q = "(celiac_weight>=80 and celiac_height<180) or (celiac_gender==Female)")
session.get("celiacsprue", q = "(celiac_weight>=80;celiac_height<180),(celiac_gender==Female)")

```


## add
```python
session.add('Person', firstName='Jan', lastName='Klaassen')
```

Creates a new instance of an entity (i.e. a new row of the entity data table) and returns the id.

Argument    | Description                                          | Required | Default
------------|------------------------------------------------------|----------|--------
`entity`    | Fully qualified name of the entity                   | yes      |
`files`     | Dictionary containing file attribute values for the  | no       | Empty dictionary
            | entity row. The dictionary should for each file      |          |
            | attribute map the attribute name to a tuple          |          |
            | containing the file name and an input stream.        |          | 
`data`	    | Dictionary mapping attribute name to non-file        | no       | Empty dictionary
            | attribute value for the entity row, gets merged      |          |
            | with the kwargs argument                             |          |
`**kwargs`  | Keyword arguments get merged with the data argument  | no       |
            
**Examples**

```python
session.add('Person', {'firstName': 'Jan', 'lastName':'Klaassen'})
session.add('Plot', files={'image': ('expression.jpg', open('~/first-plot.jpg','rb')),
		'image2': ('expression-large.jpg', open('/Users/me/second-plot.jpg', 'rb'))},
		data={'name':'IBD-plot'})
```
## update_one
```python
session.update_one("entityType", "id", "attribute", "newValue")
```
Updates a value of a specified attribute in a specified row in a specified entityType.

Argument    | Description                                          | Required | Default
------------|------------------------------------------------------|----------|--------
`entity`    | Fully qualified name of the entity                   | yes      |
`id`        | The value for the idAttribute of the entity          | yes      | 
`attr`	    | Attribute to update                                  | yes      | 
`value`     | New value of the attribute                           | yes      | 
  
## add_all
```python
session.add_all(entity, entities)
```

Creates new instances of an entity (i.e. adds new rows to the entity data table) and returns the ids.

Argument    | Description                                              | Required | Default
------------|----------------------------------------------------------|----------|--------
`entity`    | Fully qualified name of the entity                       | yes      |
`entities`  | List of dictionaries with the attributes of the entities | yes      | 

**Example**

```python
update = [{'id': '157', 'type': 'gnome', 'year': '1998'},
          {'id': '158', 'type': 'fairy', 'year': '1998'},
          {'id': '159', 'type': 'unicorn', 'year': '1998'}]


session.add_all("demo_sightings", update)
```
## delete
```python
session.delete(entity, id)
```
Deletes row based on its id. 

Argument    | Description                                              | Required | Default
------------|----------------------------------------------------------|----------|--------
`entity`    | Fully qualified name of the entity                       | yes      |
`id`        | Id of the entity that should be deleted                  | yes      | 


## delete_list
```python
session.delete_list(entity, entities)
```
Deletes a number of rows based on a list of id's. 

Argument    | Description                                              | Required | Default
------------|----------------------------------------------------------|----------|--------
`entity`    | Fully qualified name of the entity                       | yes      |
`entities`  | List of id's of entities that should be deleted          | yes      | 

## upload_zip
```python
session.upload_zip("pathtozipfile")
```
This function uploads a zip file based on the EMX format.

Argument         | Description                                                  | Required | Default
-----------------|--------------------------------------------------------------|----------|--------
`meta_data_zip`  | A zip file containing an attribute, entities, packages file  | yes      |
                 | (tsv/csv) to specify the meta data and optionally data       |          |
                 | defined in the meta data files                               |          |
                             


## get_entity_meta_data
```python
session.get_entity_meta_data(entity)
```
Retrieves the metadata for an entity repository.

Argument    | Description                                              | Required | Default
------------|----------------------------------------------------------|----------|--------
`entity`    | Fully qualified name of the entity                       | yes      |

## get_attribute_meta_data
```python
session.get_attribute_meta_data(entity, attribute)
```
Retrieves the metadata for a single attribute of an entity repository.

Argument    | Description                                              | Required | Default
------------|----------------------------------------------------------|----------|--------
`entity`    | Fully qualified name of the entity                       | yes      |
`attribute` | Name of the attribute                                    | yes      |
