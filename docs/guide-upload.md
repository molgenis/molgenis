# Import data

In this section you will learn how to systematically upload using meta-data annotated spreadsheets and strict formats.
Let us not wait any longer! After a quick re-cap of terminology and formats we will describe how you can import your data.

## Terminology
In this section we introduce and explain the terminology (for example datastructure) of MOLGENIS.

* Package: Each group has a root package where it can store its data.
Packages can have child packages to logically subdivide that root package into a tree structure,
like folders on a hard drive.
* Entity Type: An entity type is the metadata of a data collection, like a table in a database.
* Entity: The actual data that is collected based on the template from an entity type, like a table row
in a database.
* Attribute: An attribute describes the characteristics of a data item in an entity type, like a column
in a database

## Permissions
When we're uploading a new entity type, we need a package to add it to.
The easiest way is to [create a group](guide-groups-roles.md) first.

> N.B. You cannot create the group after importing the package!

The group's manager or a superuser can then use the import wizard to upload new entity types into the group's package.

The group's editors can use the import wizard to add or update entities of existing entity types in
their group's package.

## Upload formats
The MOLGENIS upload module supports the following file formats and data:

|file format		|file extention             |data formats     |
|-------------------|---------------------------|-----------------|
|CSV              	|".csv" ".txt" ".tsv" ".zip"|EMX              |
|Excel            	|".xls" ".xlsx"             |EMX              |
|OBO                |".obo.zip"                 |OBO
|OWL              	|".owl.zip"                 |OWL              |
|VCF (version 4.0)	|"vcf" ".vcf.gz"            |VCF (version 4.0)|

Abbreviations:

* CSV: Comma Separated Value
* OWL: Web Ontology Language
* VCF: Variant Call Format
* EMX: Entity Model eXtensible

Regarding CSV: The number of values on a row must match the number of column headers on the first row. Empty lines at the end of the file are ignored.

# Import overview
The upload module is the place in MOLGENIS where you can upload your data into the MOLGENIS application. If you have the permissions, you will see the upload menu item.

![Upload menu item](images/upload/upload-menu-item.png?raw=true, "upload menu item")

The different pages will be explained by uploading the<a name="advanced-data-example"></a> "Advanced data example" ([download](data/advanced_data_example_v20171206.xlsx)) example file.

The steps are:

1. Upload file
2. Options
3. Packages
4. Validation
5. Result

Navigation buttons at the bottom of the pages:

* Previous: Go to the previous page.
* Next: Go to the next page.
* Restart: Push this button when you want to start importing a new file. It will redirect you to the start of this wizard. Pushing this button will restart the wizard. The upload job continues to upload the data set.
* Finish: The same as Restart.

### Upload file

1. Select a file to upload.
2. Click on the next button.

![Upload file screen](images/upload/upload-file-screen.png?raw=true, "Upload file")

### Choose options
Select a data upload option. On this page you can select the rules of how to upload your data into MOLGENIS.
Because this dataset is an new data set to the application we leave the default option "Add entities" selected.
In tabular data sets, the term entities refers to data-rows.

Metadata options:
* Create new metadata
* Update existing metadata
* Create new metadata or update existing metadata
* Ignore metadata

The metadata options panel is displayed if more than one option is available based on the input file.

Data options:
* Add entities: Importer adds new entities or fails if entity exists.
* Add entities / update existing: Importer adds new entities or updates existing entities.
* Update entities: Importer updates existing entities or fails if entity does not exist.

![Upload file screen](images/upload/options-screen.png?raw=true, "Options")

### Choose packages
If some Entity types do not yet have a package specified, you must choose where you want to create them.

![Upload file screen](images/upload/packages-screen.png?raw=true, "Packages")

### Check validation

When you see this page the validation is already done. This page validates the structure of the meta data.

"Entities" table where all the entity types are defined.

* Name: Name of entity
* Importable: Is this entity importable or not. Two options (Yes, No)

"Entity fields" table that will contain information about the fields of an entity (Columns of the table)

* Name: Name of entity
* Detected: A comma separated list of fields that were found for this entity
* Required: Are there required fields defined in the meta data that are missing in the entity?
* Available: Are there fields in the meta data that are optionel and were not found in the entity?
* Unknown: Are there fields defined in the entity that were undefined in the meta data?

![Upload file screen](images/upload/validation-screen.png?raw=true, "Validation")

### Review results

When this page is shown with the "import succes" message, then you know that your data and metadata have been uploaded correctly.

After the data is uploaded into MOLGENIS, you can change the permissions for the entities.

In the permissions view you can do this repeatedely for multiple groups:

1. Select a group: which user group will get these permissions.
2. Select permission for an entity (table). You can choose between: Edit, View, Count and None. For more information about permissions visit the [fine grained permission guide](guide-permission-manager.md)

![Upload file screen](images/upload/result-screen.png?raw=true, "Result")

## What next?

To learn more you could read the [EMX reference](guide-emx.md).
Alternatively directly move on to [explore your data](guide-explore.md)
