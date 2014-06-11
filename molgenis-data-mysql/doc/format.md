# MOLGENIS upload format

## Introduction

MOLGENIS comes with a flexible spreadsheet format to model any tabular data set that can be uploaded directly using Excel or a zipfile containing tab-delimited *.txt files. The power comes from the meta data sections 'attributes' and 'entities' to define a custom data structure. 

This document describes how the format works based on an example: 

## Example data

For example, lets assume we want to upload the following data into MOLGENIS using either Excel (with sheets named 'cities', 'persons', 'users') or a zipped bundle of tab-delimited files (with files cities.txt, persons.txt, users.txt):


### Cities

| cityName   |
|------------|
| new york   |
| metropolis |

### Persons

| displayName | firstName | lastName | birthdate  | birthplace | children           |
|-------------|-----------|----------|------------|------------|--------------------|
| john doe    | john      | doe      | 1976-13-03 | new york   |                    |
| jane doe    | jane      | doe      |            | metropolis |                    |
| pape doe    | papa      | doe      |            | new york   | john doe, jane doe |

Notes: birthplace refers to elements in the cityName values in the cities table. children contains comma seperated values refering to other person instances via displayName (trailing spaces will be removed).


### Users


| userName | active | displayName | firstName | lastName | birthdate  | birthplace | children           |
|----------|--------|-------------|-----------|----------|------------|------------|--------------------|
| jdoe     | TRUE   | john doe    | john      | doe      | 1976-13-03 | new york   |                    |
| jdoe2    |        | jane doe    | jane      | doe      |            | metropolis |                    |
| pdoe     |        | papa doe    | papa      | doe      |            | new york   | john doe, jane doe |

Note: users looks exactly like person, except it has two columns more (userName, active).

## Example meta data

Using the Meta Data capabilities you can define your data structures. This section is optional (either because somebody else has already uploaded the meta data OR because you want MOLGENIS to guess the data structure for you).

### 'Attributes' sheet
The attributes sheet is used to define the elements of the data sheets. The example below defines a simple data structure with entities 'city', 'person' and 'user'. Note that 'user' had exactly the same attributes as 'person' so we will use 'object orientation' to say that 'user' is a special kind of 'person'.

| entity  | attribute   | dataType | nillable | refEntity | idAttribute | description             |
|---------|-------------|----------|----------|-----------|-------------|-------------------------|
| cities  | cityName    |          |          |           | TRUE        |  unique city name       |
| persons | displayName |          |          |           | TRUE        |  unique name            |
| person  | firstName   |          |          |           |             |  first name             |
| persons | lastName    |          |          |           |             |  family name            |
| persons | birthdate   | date     | TRUE     |           |             |  day of birth           |
| persons | children    | mref     | TRUE     | person    |             |  parent-child relation  |
| persons | birthplace  | xref     | TRUE     | city      |             |  place of birth         |
| users   | username    |          |          |           | TRUE        |  unique login name      |
| users   | active      | bool     | TRUE     |           |             |  whether user is active |


### 'Attributes' options
Required columns:

* entity : unique name of the entity.
* attribute : name of attribute, unique per entity

Optional columns (can be omitted):

* dataType: defines the data type (default: string)
  * string : character string of <255 characters
  * text : character string of unlimited length (usually <2Gb)
  * int : natural numbers like -1, 0, 3. Optionally use rangeMin and rangeMax
  * long : non-decimal number of type long
  * decimal : decimal numbers like -1.3, 0.5, 3.75 (float precision)
  * bool : yes/no choice
  * date : date in yyyy-mm-dd format
  * datetime : date in yyyy-mm-dd hh:mm:ss
  * xref : cross reference to another entity; requires refEntity to be provided
  * mref : many-to-many relation to another entity; requires refEntity to be provided
  * compound : way to assemble complex entities from building blocks (will be shown as tree in user interface); requires refEntity to be provided
* refEntity : used in combination with xref, mref or mref. Should refer to an entity.
* nillable : whether the column may be left empty. Default: false
* idAttribute : whether this field is the unique key for the entity. Default: false
* description : free text documentation describing the attribute
* unit : definition of the unit of this attribute. E.g. 'cm' [discussion: should be linked to valueset?]
* rangeMin : used to set range in case of int attributes
* rangeMax : used to set range in case of int attributes

### 'Entities' sheet (optional)
In most cases the 'attributes' sheet is all you need. However, in some cases you may want to add more details on the 'entity' or use object oriented data modeling concepts such as 'abstract' (for interfaces) and 'extends' (for inheritance).


| entity | extends | abstract | description                                                       |
|--------|---------|----------|-------------------------------------------------------------------|
| users  | persons |          | users extends persons, meaning it 'inherits' attribute definition |


### Entities options
Required columns:

* entity : unique name of the entity

Optional columns:

* extends : reference to another entity that is extended
* abstract : indicate if data can be provided for this entity (abstract entities are only used for data modeling purposes but cannot accept data)
* description : free text description of the entity

## Advanced topics

MOLGENIS has special recognition for the following entities (and extensions thereof).

TODO: categorical data. Currently solved via xref.
TODO: ontology data. Currently solved via xref.
TODO: genome position data. Currently defined by hand

## Wish list

Change or change documentation:

* 'required' and 'unique' (and xref_entity?) property for attribute?
* create separate 'unit' list?
* can we hload dataset without entity / attributes (auto load?)
* create 'category'
  * code, label, ismissing, description, ontology
  * use decorator to automatically produce identifier (optional)
* validation rules


