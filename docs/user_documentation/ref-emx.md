**
EMX (entity model extensible) is a flexible spreadsheet format to easily upload any tabular data using Excel or a zipfile of tab-delimited *.tsv files. This works because you can tell MOLGENIS the 'model' of your data via a special sheet named 'attributes'. Optionally, you can also add metadata on entities (i.e., classes, tables), and packages (i.e, models and submodels)
**

# Minimal example 
([download](../data/simple_data_example_v20160915.xlsx))

For example, if you want to upload an Excel with sheet 'patients':

| displayName | firstName | lastName | birthdate  | birthplace |
|-------------|-----------|----------|------------|------------|
| john_doe    | john      | doe      | 1976-03-13 | new york   |
| jane_doe    | jane      | doe      |            | metropolis |
| papa_doe    | papa      | doe      |            | new york   |

Then you must provide a model of your 'patients' via Excel with sheet named 'attributes':

| name        | entity   | dataType | idAttribute | nillable | description             |
|-------------|----------|----------|-------------|----------|-------------------------|
| displayName | patients |          | TRUE        | FALSE    | name                    |
| firstName   | patients |          |             | FALSE    | first name              |
| lastName    | patients |          |             | FALSE    | family name             |
| birthdate   | patients | date     |             | FALSE    | day of birth            |
| birthplace  | patients |          |             | FALSE    | place of birth          |

'entity' should show the name of your data sheet. Each attribute the column headers in your data. Default dataType is 'string' so you only need to provide non-string values (int, date, decimal, etc). And you must always provide one idAttribute that has 'nillable' = 'FALSE'.

You can first upload the 'model' and then the 'data'. Or you can put the both into one file and upload in one go. What you prefer :-) [todo: provide example files for download]

# Advanced example 
([download](../data/advanced_data_example_v20171206.xlsx))

Lets assume we want to upload multiple data sheets, with relations between them:

Cities:

| cityName   |lat       | lng       |
|------------|----------|-----------|
| new_york   | 40,712784|-74,005941 |
| metropolis | 37,151165|-88,731998 |

Patients:

| displayName | firstName | lastName | birthdate  | birthplace | children           | disease |
|-------------|-----------|----------|------------|------------|--------------------|---------|
| john_doe    | john      | doe      | 1976-03-13 | new_york   |                    | none    |
| jane_doe    | jane      | doe      |            | metropolis |                    | none    |
| papa_doe    | papa      | doe      |            | new_york   | john_doe, jane_doe | cardio  |

Notes: birthplace refers to elements in the cityName values in the cities table. children contains comma separated values referring to another patient via displayName (trailing spaces will be removed).
Warning: when using excel, be sure your decimal separator is a ".", instead of ",", otherwise mrefs might be seen as decimals when their id is a number, this causes molgenis to see a dot between your references and the importer to fail when uploading.

Users:

| userName | active | displayName | firstName | lastName |
|----------|--------|-------------|-----------|----------|
| jdoe     | TRUE   | john_doe    | john      | doe      |
| jdoe2    |        | jane_doe    | jane      | doe      |
| pdoe     |        | papa_doe    | papa      | doe      |

Note: users looks similar patients, i.e. they are also persons having 'displayName', 'firstName', and 'lastName'. We will use this in the model below. 

To model the data advanced data example, again you need to provide the 'attributes' (i.e., columns, properties). Optionally, you can also describe entities (i.e., classes, tables), and packages (i.e, models and submodels) which gives you some advanced options.

Attributes:

| name        | entity   | dataType | nillable | refEntity | idAttribute | description             |
|-------------|----------|----------|----------|-----------|-------------|-------------------------|
| cityName    | cities   |          | FALSE    |           | TRUE        |  unique city name       |
| lat         | cities   | decimal  |          |           |             |  latitude in degrees    |
| lng         | cities   | decimal  |          |           |             |  longitude in degrees   |
| displayName | patients |          | FALSE    |           | TRUE        |  unique name            |
| firstName   | persons  |          |          |           |             |  first name             |
| lastName    | persons  |          |          |           |             |  family name            |
| children	  | patients | mref     |          | patients  |             |  children of a patient  |
| birthdate   | patients | date     |          |           |             |  day of birth           |
| birthplace  | patients | xref     |          | cities    |             |  place of birth         |
| disease     | patients |          |          |           |             |  disese description     |
| userName    | users    |          | FALSE    |           | TRUE        |  unique login name      |
| active      | users    | bool     |          |           |             |  whether user is active |

The example below defines the model for entities 'city', 'patient' and 'user'. Note that 'users' had some attributes shared with 'patients' so we will use 'object orientation' to say that both 'user' and 'patient' are both a special kind of 'persons'. This will be defined using the 'extends' relation defined in the 'entities' sheet below.

Entities:

| name     | package  | extends | abstract | description                                                       |
|----------|----------|---------|----------|-------------------------------------------------------------------|
| cities   | hospital |         |          | list of cities
| persons  | hospital |         | true     | person defines general attributes like firstName, lastName        |
| users    | hospital | persons |          | users extends persons, meaning it 'inherits' attribute definition |
| patients | hospital | persons |          | patient extends person, adding patientNumber                      |

In most cases the 'attributes' sheet is all you need. However, in some cases you may want to add more details on the 'entity'. Here we wanted to show use of 'abstract' (i.e., interfaces) to create model class 'persons' and 'extends' (i.e., subclass, inheritance) to define that 'user' and 'patient' have the same attributes as 'persons'. When data model become larger, or when many data sheets are loaded then the 'package' construct enables you to group your (meta)data. 

Packages:

| name     | description                                                   | parent |
|----------|---------------------------------------------------------------|--------|
| root     | my main package                                               |        |
| hospital | sub package holding entities to describe all kinds of persons | root   |

# Rules for technical names
For all technical names in the EMX format, the following rules apply:
- No special characters, except for; '_' and '#', only letters, numbers are allowed.
- No names starting with digits. 
- The keywords: "login", "logout", "csv", "base", "exist", "meta", are not allowed.

These rules only apply to the technical names, labels are not limited by these rules.

# Attributes options

## Required columns
### entity 
Name of the entity this attribute is part of
### name
Name of attribute, unique per entity.

## Optional columns (can be omitted)

### dataType
Defines the data type (default: string)

| Data type    | Description                                                                                   | Expected formatting                                              |
|--------------|-----------------------------------------------------------------------------------------------|------------------------------------------------------------------|
| string       | The default data type in  MOLGENIS, describing a character string of <255 characters.         | A string of characters with a length of less than 255            |
| text         | When having a string with a length of more than 255 characters, this data type is recommended.| A string with characters with a maximum length of 65535		  |
| int          | Integers. Natural numbers like 1, 2, 3, -1, -2, -3. rangeMin and rangeMax can be defined.     | Non decimal numbers in range[-2^31 , 2^31 -1]                    |
| long         | Non-decimal number of type long                                                               | Non decimal numbers in range[-2^63 , 2^63 -1]                    |
| decimal      | Decimal numbers/floats.                                                                       | Decimal numbers with [double-precision](https://en.wikipedia.org/wiki/Double-precision_floating-point_format) ('NaN' not allowed)                        |
| bool         | A boolean value: true/false                                                                   | TRUE/FALSE                                                       |
| date         | A date without a time-zone in the ISO-8601 calendar system                                    | yyyy-mm-dd                                                       |
| datetime     | An instant in time. Time zone information may be used to specify the instant but is not stored. | yyyy-mm-ddThh:mm:ss+timezone e.g. 1985-08-12T11:12:13+0500       |
| xref         | Reference to an attribute of another entity. Using this type requires another entity (refEntity) with information that is linked to the selected entity. Although you should always refer to the id of an attribute defined as xref, in your data explorer the label of the refEntity will be presented instead. When label is not specified, id will be used as label. When searching for a specific xref value in the filter wizard, the value has to be typed partly and then selected out of a list with suggestions.| Id of the attribute you wish to link, this id should always be available in the refEntity. |
| mref         | Reference to several attributes of another entity. Using this type requires another entity (refEntity) with information that is linked to the selected entity. Although you should always refer to the id of an attribute defined as mref, in your visualisation, the defined label for the refEntity will be presented, when label is not given, id will be used as label. When searching for a specific mref value in the filter wizard, the value has to be typed partly and then selected out of a list with suggestions.| A comma separated list of id’s, these id’s should always be available in the refEntity. |
| categorical  | Reference to an attribute of another entity. Using this type requires another entity (refEntity) with information that is linked to the selected entity. Although you should always refer to the id of an attribute defined as categorical, in your visualisation, the defined label for the refEntity will be presented, when label is not given, id will be used as label. This type is typically used when answers are fixed like: “Yes”, “No”, “Unknown”. When searching for a specific categorical value in the filter wizard of molgenis a checkbox can be marked.| Id of the attribute you wish to link, this id should always be available in the refEntity. |
| categorical_mref| Reference to several attributes of another entity. Using this type requires another entity (refEntity) with information that is linked to the selected entity. Although you should always refer to the id of an attribute defined as categorical_mref, in your visualisation, the defined label for the refEntity will be presented, when label is not given, id will be used as label. When searching for a specific categorical value in the filter wizard of molgenis a checkbox can be marked.| A comma separated list of id’, these id’s should always be available in the refEntity.|
| compound     | This type can be used to group parts of your data together. Your dataset will consist of several compounds all containing certain attributes. Don’t forget to specify the partOfAttribute column for the attributes you wish to put in the compound.| Nothing|
| file         | A file. Create a column of the 'file' data type requires refEntity FileMeta.| [How to](https://github.com/molgenis/molgenis/wiki/File-datatype)|
| email        | An e-mail adress                                                                              | E-mail adress                                                    |
| enum         | An item chosen from a fixed list of options that can be selected for this data type. The options should be given in an extra column called “enumOptions”. These options cannot be updated without changing meta-data (so deleting the data and meta data and a new upload are required in MOLGENIS 1.x). | A value chosen from the enumOptions list specified as a comma separated list of options in the model.|
| hyperlink    | A link to a website                                                                           | A link to a website                                              |
| one_to_many  | This data type is only supported in MOLGENIS 2.0. A data type that defines the one to many relationship between two columns in two separate tables. Having this data types requires having another table with an xref column which is linked to the one_to_many. The one_to_many requires a refEntity like the other referring data types, but unlike the others, it also requires a “mappedBy” column. In this column the name of the xref in the other column should be specified. For instance, an author can write several books. Books are stored in one table (called “books”) with “author” as xref and as refEntity “authors”. In the authors table authors are stored with books as one_to_many. The books attribute has the refEntity “books” and is mappedBy “author”. | A comma separated list of id’s. Requires having an xref to this attribute in another table. Since one_to_many attributes are mapped by a xref attribute they cannot be specified in data sheets.    |

### refEntity 
Used in combination with xref, mref, categorical, categorical_mref or one_to_many. Should refer to an entity.

### nillable 
Whether the column may be left empty. Default: false

### idAttribute 
Whether this field is the unique key for the entity. Default: false. Use 'AUTO' for auto generated (string) identifiers.

### description 
Free text documentation describing the attribute

### description-{languageCode} 
Description for specified language (can be multiple languages, example: description-nl)

### rangeMin 
Used to set range in case of int or long attributes

### rangeMax 
Used to set range in case of int or long attributes

### lookupAttribute 
true/false, default false

Indicates if this attribute should appear in the xref/mref search dropdown in the dataexplorer.
A lookupAttribute must be visible.
An entity inherits the lookupAttributes from the entity it extends.

If an entity has no lookupAttributes, the labelAttribute is used in the dropdown.

### label 
optional human readable name of the attribute

### label-{languageCode}
label for specified language (can be multiple languages, example: label-nl)

### aggregateable 
true/false to indicate if the user can use this atrribute in an aggregate query

### labelAttribute 
true/false to indicate that the value of this attribute should be used as label for the entity (in the dataexplorer when used in xref/mref). Default: false.
A labelAttribute must be visible. If an entity's idAttribute is not visible, it should have a labelAttribute.

### readOnly
true/false to indicate a readOnly attribute

### tags 
ability to tag the data referring to the tags sections, described below

### validationExpression 
javascript validation expression that must return a bool. Must return true if valid and false if invalid. See the [Expressions](ref-expressions.md) section for a syntax description.

### defaultValue
value that will be filled in in the forms when a new entity instance is created. Not yet supported for mref and xref values. For categorical_mref, should be a comma separated list of ids. For xref should be the of the refEntity. For bool should be true or false. For datetime should be a string in the format YYYY-MM-DDTHH:mm:ssZZ. For date should be a string in the format YYYY-MM-DD.

### partOfAttribute
is used to group attributes into a compound attribute. Put here the name of the compound attribute.

### expression
is used to create computed attributes.

**Computed string example: "xref as label attribute" (config attributes table)**
  1. Create a new target attribute into the "myEntity" entity, that will become the new computed attribute (in the example: "myLabel")
  2. Add in the expression column of the new attribute "myLabel": "the name of attribute to convert from" (in example: expression -> "myXref")

| name    | entity	  | label	       | dataType	| idAttribute	| refEntity	  | nillable	| visible	| labelAttribute	| expression |
|---------|----------|--------------|----------|-------------|-------------|----------|---------|----------------|------------|
| id      | myEntity | Id	          | int	     | TRUE	       |             |FALSE		   | FALSE	  | FALSE          |            |
| myXref	 | myEntity | Other Entity	| xref	    | FALSE	      | otherEntity |TRUE      | TRUE    | FALSE	         |            |
| myLabel | myEntity | Label	       | string	  | FALSE	      |             |TRUE		    | FALSE	  | TRUE           | myXref     |

**Computed object example: "computed myXref" (config attributes table)**

  1. Create a two new target attributes (attr1, attr2) in a new entity (newEntity).
  2. Create a xref attribute (myXref) to contain the computed entity.
  3. Add in the expression column of new xref attribute (myXref) the next script: "{attr1: myAttr1, attr2: myAttr2}"
  4. The name of the attributes to convert from should be in the same entity as the new xref attribute (myEntity).



| name    | entity	   | label	       | dataType	| idAttribute	| refEntity	  | nillable	| visible	| labelAttribute	| expression |
|---------|-----------|--------------|----------|-------------|-------------|----------|---------|----------------|------------|
| id      | myEntity  | Id	          | int	     | TRUE	       |             |FALSE		   | FALSE	  | FALSE          |            |
| myXref	 | myEntity  | New Entity  	| xref	    | FALSE	      | newEntity   |TRUE      | TRUE    | FALSE	         |{attr1: myAttr1, attr2: myAttr2}|
| myAttr1 | myEntity  | My Attr 1    | date   	 | FALSE	      |             |TRUE		    | FALSE	  | TRUE           |            |
| myAttr2 | myEntity  | My Attr 2    | int      | FALSE	      |             |TRUE		    | FALSE	  | TRUE           |            |
| attr1   | newEntity |    Attr 1    | string   | FALSE	      |             |TRUE		    | FALSE	  | TRUE           |            |
| attr2   | newEntity |    Attr 2    | string   | TRUE 	      |             |TRUE		    | FALSE	  | TRUE           |            |


# Entities options
## Required columns

### entity 
unique name of the entity. If packages are provided, name must be unique within a package.

## Optional columns

### extends 
reference to another entity that is extended

### package 
name of the group this entity is part of

### abstract 
indicate if data can be provided for this entity (abstract entities are only used for data modeling purposes but cannot accept data)

### description 
free text description of the entity

### description-{languageCode} 
description for specified language (can be multiple languages, example: description-nl)

### backend
the backend (database) to store the entities in (currently only PostgreSQL)

### tags 
ability to tag the data referring to the tags sections, described below

# Packages options

## Required columns

### name 
unique name of the package. If parent package is provided the name is unique within the parent.

## Optional columns

### description 
free text description of the package

### parent 
use when packages is a sub-package of another package

### tags 
mechanism to add flexible meta data such as ontology references, hyperlinks

# Tags options (BETA)

Optionally, additional information can be provided beyond the standard meta data described above. Therefore all meta-data elements can be tagged in simple or advanced ways (equivalent to using RDF triples). For example, above in the packages example there is a 'homepage' tag provided. For example:

| identifier | label                   | objectIRI               | relationLabel          | codeSystem | relationIRI |
|------------|-------------------------|-------------------------|------------------------|------------|-------------|
| like       | like                    |                         |                        |            |             |
| homepage   | http://www.molgenis.org | http://www.molgenis.org | homepage               |            |             |
| docs       | http://some.url         | http://www.molgenis.org | Documentation and Help | EDAM       | http://edamontology.org/topic_3061 |

## Required columns

### identifier 
unique name of this tag, such that it can be referenced

### label 
the human readable label of the tag (e.g. the 'like' tag as shown above).

## Optional columns

### objectIRI
url to the value object (will become an hyperlink in the user interface)

### relationLabel
human readible label of the relation, e.g. 'Documentation and Help'
 
### relationIRI
url to the relation definition, e.g. http://edamontology.org/topic_3061

### codeSystem
name of the code system used, e.g. EDAM

# Internationalization

You can internationalize attribute labels and descriptions, entity labels and descriptions and
you can define internationalized versions of entity attributes.

### entities

description-{languageCode} : description for specified language (can be multiple languages)
label-{languageCode} : label for specified language (can be multiple languages)

Example:

| name     | package  | description-en  | description-nl     | label-en | label-nl |
|----------|----------|-----------------|--------------------|----------|----------|
| cities   | hospital | list of cities  | lijst van steden   | Cities   | Steden   |
| persons  | hospital | list of persons | lijst van personen | Persons  | Personen |


### attributes

description-{languageCode} : description for specified language (can be multiple languages)
label-{languageCode} : label for specified language (can be multiple languages)

Example:

| name        | entity   | idAttribute | description-en           | description-nl                | label-en        | label-nl       |
|-------------|----------|-------------|--------------------------|-------------------------------|-----------------|----------------|
| displayName | patients | TRUE        | Patient name             | Naam van de patient           | name            | naam           |
| firstName   | patients |             | Patient first name       | Voornaam van de patient       | first name      | voornaam       |
| lastName    | patients |             | Patient family name      | Achternaam van de patient     | family name     | achternaam     |

### Language depended entity attributes

You can internationalize attributes by postfixing the name with -{countryCode}.

If this is the label attribute,
you must set all city-xx labelAttribute values to 'TRUE' on the 'entities' tab.

Example:

**entities:**

| name           | entity   | idAttribute | label-nl    | label-de      | labelAttribute |
|----------------|----------|-------------|-------------|---------------|----------------|
| name           | gender   | TRUE        |             |               |                |
| genderlabel-nl | gender   |             | Label (nl)  | Etikette (nl) | TRUE           |
| genderlabel-de | gender   |             | Label (de)  | Etikette (de) | TRUE           |


**gender:**

| name    | genderlabel-nl   | genderlabel-de |
|---------|------------------|----------------|
| Male    | Man              | Man            |
| Female  | Vrouw            | Frau           |
| Unknown | Onbekend         | Unbekannt      |

