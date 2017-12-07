# Metadata manager
The metadata manager can be used to manage your metadata.

![1](../images/metadata-manager/full-edit-overview.png?raw=true)

This entails changing descriptions, changing the labels, changing the package in which the EntityType lives, but it also 
means you can add and remove attributes (columns), or even change the order in which they are shown in the data explorer.

The metadata manager also allows you to create new EntityTypes (tables) from scratch! 
No longer do you need to understand the EMX format and import excel sheets via the importer, you can create new data tables 
from scratch using the metadata manager user interface.

## Creating a new EntityType
To create a new EntityType, press the blue 'plus' button next to the EntityType select dropdown.

![2](../images/metadata-manager/entity-edit-form-new.png?raw=true)

You will get an empty template, allowing you to build your EntityType from scratch. 
If you are familiar with relational database concepts, you will probably recognize most of the inputs. 
In any case, here we will take you through the different fields and explain their use.

__Extends__ 
: The extends dropdown lets you choose another EntityType which your EntityType will extend. 
This means that any attributes that EntityType might have, will be inherited by your EntityType.

An EntityType __has__ to be abstract in order for you to extend it.

Abstract EntityType _Job_

| Salary | Company name |
|-----------------------|

Concrete EntityType _Doctor_

| Patients | Working hours |
|--------------------------|

Now, if _Doctor_ extends _Job_, you will get the following table

| Salary | Company name | Patients | Working hours |
|--------------------------------------------------|

__Abstract__
: This switch will allow you to set your EntityType to abstract. Creating an abstract EntityType means that it will __not__ contain any
data. It is useful if you want to create multiple tables that share common attributes.

__Label__
: Piece of text allowing you to set a human readable name for your EntityType.

__Description__
: Piece of text describing your EntityType.

__Package__
: Setting a Package allows you to group EntityTypes together under a single namespace. 
_Note_: Packages can not be created via the metadata manager, you have to create them either in the dataexplorer or upload them via the importer

__ID attribute__
: The attribute that will serve as the ID, or primary key, within your EntityType.

__Label__
: The label attribute will determine which attribute of an EntityType is shown when you search for values _in_ an EntityType.

__Lookup attributes__
: The lookup attributes determine which columns are used to search when doing specific queries.

When talking about ID attributes and lookup attributes, you might wonder: where do I get these attributes?
The answer is: in the Attribute tree!

![3](../images/metadata-manager/attribute-tree-new.png?raw=true)

The tree allows you to do three things:
- Create new attributes
- Remove existing attributes
- Change the order of attributes

For tutorials sake, I created three attributes: _id_, _label_, and _country_

Now we will add a new attribute. I click the blue plus button next to attributes.

![4](../images/metadata-manager/attribute-edit-form-new.png?raw=true)

just like with a new EntityType, you get an empty template. This gives you complete control on how you want your attribute to look like.


As you probably noticed, there are __a lot__ of options to set. Luckily, we can get away with the default settings 50% of the time.
For more advanced use cases, we explain the every input field below.

__Name__
: A unique identifier for attributes _within_ this EntityType (You can have multiple 'id' attributes across different EntityTypes).

__Label__
: A label for your attribute. This is meant to be a human readable name, and will be column names people see when they look at your data.

__Description__
: A description for your attribute.

__Type__
: Now the type field is a very important one. It determines which type of data will be allowed in this column. 
This can be strings and numbers, or more advanced types like dates, email, enum, or the very MOLGENIS specific xref or mref types.
Some of the types will reveal extra fields on selection, we will go through these special fields now.

_Select int or long_
: Selecting a numerical type will give you the added options _minimum range_ and _maximum_ range. 
As the names suggest, it will allow you to set rules that number values in this column have to adhere to.

_Selecting categorical, categorical_mref, xref, mref_
: These are partially MOLGENIS specific datatypes. What they have in common is that they allow an attribute to reference to another EntityType.
You will have to set a 'reference entity'. This means that you have to select another EntityType.

_Selecting enum_
: Enums offer a set list of values. You can fill these in via a comma separated string e.g. _enumOption1,enumOption2,enumOption3_

_Selecting onetomany_
: They one to many type is a reference type that allows a backreference. Meaning that not only will you be able to look from A to B, but also from B to A.
The mapped by and order by fields are now open to you. Mapped by is the same as an XREF, you select reference entity. Order by can be used to
determine how your values are sorted, e.g country;ASC will sort the reference values on the country column in ascending order.

__Parent__
: It is possible to nest an attribute under another attribute. 
The parent option shows you a dropdown with all the Compound attributes present in your EntityType.

__Nullable__
: If a column can be empty or not

__Auto__
: If the values should be auto generated or not. Mostly used for ID attributes

__Visible__
: Should the column be visible or not. Also used to hide auto generated identifiers

__Unique__
: Forces all values in this column to be unique. Automatically set for ID attributes.

__Read-only__
: The values for this column can not be edited when checked.

__Aggregatable__
: When checked, allows MOLGENIS to compute aggregation on the values of this attribute.

__Computed value expression__
: Possible Magma script that allows you to write computed expressions. e.g you have int column A and int column B. For column C you can create
the following expression: (A/B) * 2.

__Visible expression__
: Possible Magma script that allows you to write an expression that determines whether the column should be shown or not.
e.g. $('A').value === true will only show column B when the value in A is true. Useful for one line datasets.

__Validation expression__
: Possible magma script that allows you to write an expression that validates the values inside a column.
e.g. $('A').value() > 5 will throw a validation error when you try to add data that is lower then 5

After you have created some attributes, selected an ID attribute, and feel comfortable with your EntityType, you can hit the 'Save all changes' button.
If everything is correct, you will get a message saying save was successful. If something went wrong, you will get a message telling you which fields you forgot.

After saving, your new EntityType will be available in the dropdown for further editing, and you can start importing data
for it via the importer.

## Editing existing EntityTypes
You can select existing EntityTypes in the dropdown at the top of the screen

![5](../images/metadata-manager/header-select-open.png?raw=true)

On select, all the fields that were visible for creating a new EntityType are available to you.
After you are done changing things, you can hit the save all changes button.

### Conversion list of data types
This list describes the allowed conversion of data types in the metadata edit, and also some extra info and motivation.

| Origin Attribute Type | Allowed conversions |
|-----------------------|---------------------|
| BOOL                  | STRING, TEXT, INT   |
| CATEGORICAL           | STRING, INT, LONG, XREF |
| CATEGORICAL_MREF      | MREF                |
| COMPOUND              | STRING              |
| DATE                  | STRING, TEXT, DATE_TIME |
| DATE_TIME             | STRING, TEXT, DATE  |
| DECIMAL               | STRING, TEXT, INT, LONG, ENUM |
| EMAIL                 | STRING, TEXT, XREF, CATEGORICAL |
| ENUM                  | STRING, INT, LONG, TEXT |
| FILE                  | NONE                |
| HTML                  | STIRNG, TEXT, SCRIPT |
| HYPERLINK             | STRING, TEXT, XREF, CATEGORICAL |
| INT                   | STRING, TEXT, DECIMAL, LONG, BOOL, ENUM, XREF, CATEGORICAL |
| LONG                  | STRING, TEXT, INT, DECIMAL, ENUM, XREF, CATEGORICAL |
| MREF                  | CATEGORICAL_MREF    |
| ONE_TO_MANY           | NONE                |
| SCRIPT                | STRING, TEXT        |
| STRING                | ALL                 |
| TEXT                  | ALL                 |
| XREF                  | STRING, INT, LONG, CATEGORICAL |

## Deleting existing EntityTypes
If you want to remove an EntityType because you want to start over or had some experiments that are no longer necessary, you can go and
select that EntityType and hit the big 'Delete entity' button