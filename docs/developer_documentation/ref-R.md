** 
The MOLGENIS R API client allows you to retrieve, create, update and delete entities from within the [R](http://r-project.org) environment.
**

Just add 
```
source("http://molgenis.mydomain.example/molgenis.R")
``` 
at the top of your script and you can connect to a MOLGENIS server. Typically the first thing you do is login and the last thing is logout.

**NOTE:** For https connections use 
```
eval(expr = parse(text = getURL("https://molgenis.mydomain.example/molgenis.R?molgenis-token=${molgenisToken}")))
```  

**NOTE:** The MOLGENIS R-api client supports up to R-version 3.2.x

# Overview example

```
source("http://molgenis.mydomain.example/molgenis.R")


molgenis.login("admin", "admin")

df <- molgenis.get("celiacsprue", 
                   q = "celiac_weight>80 and celiac_height>180",
                   num = 1000,
                   attributes = c("celiac_weight", "celiac_height", "celiac_gender"))
                   
plot(df$Celiac_Height ~ df$Celiac_Weight, col=df$Celiac_Gender)

molgenis.logout()
```

# Methods
## login
```
molgenis.login(username,password)
```
Login to the MOLGENIS REST API

## logout
```
molgenis.logout()
```
Logout from the MOLGENIS REST API and destroy the session.

##get
```
molgenis.get (entity, q = NULL, start = 0, num = 1000, attributes = NULL)
```

Retrieves entities and returns the result in a dataframe.

Parameter   | Description                                       | Required | Default
------------|---------------------------------------------------|----------|--------
`entity`    | The entity name                                   | yes      |
`q`         | Query string in rsql/fiql format (see below)      | No       | NULL
`start`	    | The index of the thirst row to return             | No       | 0
`num`       | The maximum number of rows to return (max 10000) | No       | 1000
`attributes`| Vector of attributenames(columns) to return       | No       | All attributes
`sortColumn`| attributeName of the column to sort on            | No       | NULL
`sortOrder` | sort order, 'ASC' of 'DESC'                       | No       | NULL


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

```
molgenis.get("celiacsprue")
molgenis.get("celiacsprue", num = 100000, start = 1000)
molgenis.get("celiacsprue", attributes = c("Individual", "celiac_gender"))
molgenis.get("celiacsprue", q = "(celiac_weight>=80 and celiac_height<180) or (celiac_gender==Female)")
molgenis.get("celiacsprue", q = "(celiac_weight>=80;celiac_height<180),(celiac_gender==Female)")

```

<br />
## add
```
molgenis.add(entity, ...)
```

Creates a new instance of an entity (i.e. a new row of the entity data table) and returns the id.

Parameter|Description|Required
---------|-----------|--------
entity| The entity name of the entity to create|yes
...| Var arg list of attribute names and values|yes

**Example**

```
id <- molgenis.add(entity = "Person", firstName = "Piet", lastName = "Paulusma")
```

## addAll
```
molgenis.addAll(entity, rows)
```

Creates new instances of an entity (i.e. adds new rows to the entity data table) and returns the ids.

Parameter|Description|Required
---------|-----------|--------
entity| The entity name of the entity to create|yes
rows| data frame where each row represents an entity instance|yes

**Example**

```
firstName <- c("Piet", "Paulusma")
lastName <- c("Klaas", "de Vries")
df <- data.frame(firstName, lastName)

molgenis.addAll("Person", df)
```

<br />
## update
```
molgenis.update(entity, id, ...)
```

Updates un existing entity

Parameter|Description|Required
---------|-----------|--------
entity| The entity name|yes
id| The id of the entity|Yes
...| Var arg list of attribute names and values|yes

**Example**

```
molgenis.update(entity = "Person", id = 8, firstName = "Pietje", lastName = "Paulusma")
```

## delete
```
molgenis.delete(entity, id)
```

Deletes an entity.

Parameter|Description|Required
---------|-----------|--------
entity| The entity name|yes
id| The id of the entity|Yes

**Example**

```
molgenis.delete(entity = "Person", id = 8)
```

## deleteList
```
molgenis.deleteList(entity, c("id1", "id2"))
```

Deletes a list of entities in an entityType.

Parameter|Description|Required
---------|-----------|--------
entity| The entityType name|yes
rows| List with ids of the rows|yes

**Example**

```
molgenis.deleteList(entity = "Person", rows = c("1", "2", "3"))
```

## getEntityMetaData
```
molgenis.getEntityMetaData(entity)
```

Gets the entity metadata as list.

**Example**

```
meta <- molgenis.getEntityMetaData("celiacsprue")
meta$label
```

## getAttributeMetaData
```
molgenis.getAttributeMetaData(entity, attribute)
```

Gets attribute metadata as list.

Parameter|Description|Required
---------|-----------|--------
entity| The entity name|yes
attribute| The name of the attribute|Yes

**Example**

```
attr <- molgenis.getAttributeMetaData(entity = "celiacsprue", attribute = "celiac_gender")
attr$fieldType
```
