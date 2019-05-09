# Upload your data using the Import Wizard (Advanced data import)
In this section you will learn how to upload your data. 
Let us not wait any longer! 
After a quick re-cap of terminology and formats we will describe how you can import your data.

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
The easiest way is to [create a group](../admin-features/guide-ref-security) first.

> N.B. You cannot create the group after importing the package!

The group's manager or a superuser can then use the import wizard to upload new entity types into the group's package.

The group's editors can use the import wizard to add or update entities of existing entity types in
their group's package.

## Formats
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

### CSV
The number of values on a row must match the number of column headers on the first row. Empty lines at the end of the file are ignored.
 
# Overview
The upload module is the place in MOLGENIS where you can upload your data into the MOLGENIS application. If you have the permissions, you will see the upload menu item.

![Upload menu item](../../images/upload/upload-menu-item.png?raw=true, "upload menu item")

The different pages will be explained by uploading the<a name="advanced-data-example"></a> "Advanced data example" ([download](/data/advanced_data_example_v20171206.xlsx)) example file.

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

# Upload file

1. Select a file to upload.
2. Click on the next button.

![Upload file screen](../../images/upload/upload-file-screen.png?raw=true, "Upload file")

# Choose options
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

![Upload file screen](../../images/upload/options-screen.png?raw=true, "Options")

# Choose packages
If some Entity types do not yet have a package specified, you must choose where you want to create them.

![Upload file screen](../../images/upload/packages-screen.png?raw=true, "Packages")

# Check validation
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

![Upload file screen](../images/upload/validation-screen.png?raw=true, "Validation")

# Review results

When this page is shown with the "import succes" message, then you know that your data and metadata have been uploaded correctly.

After the data is uploaded into MOLGENIS, you can change the permissions for the entities.

In the permissions view you can do this repeatedely for multiple groups:

1. Select a group: which user group will get these permissions.
2. Select permission for an entity (table). You can choose between: Edit, View, Count and None. For more information about permissions visit the [Admin guide](guide-admin)

![Upload file screen](../../images/upload/result-screen.png?raw=true, "Result")

# What next?

To learn more you could read the [EMX reference](ref-emx.md).
Alternatively directly move on to [explore your data](../finding-data/guide-explore.md)

# Import API

## Endpoints

[SERVER URL]/plugin/importwizard/importFile/
This endpoint can be used to import a file into MOLGENIS by providing the file by for example the use of a webform.

##### Required parameter:
MultipartFile file

[SERVER URL]/plugin/importwizard/importByUrl/
This endpoint can be used to import a file into MOLGENIS by providing a URL to the file.

##### Required parameter:
String url

##### optional parameters for both endpoints:
entityTypeId: The name of the entity, this only has effect for VCF files (.vcf and .vcf.gz)
	Default value is the filename.

packageId: The id of the package you want the imported file to be placed under. If package does not exist, an error will be thrown. Not supplying the package will place an imported file under the default package 'base'

Note: Both entityTypeId and packageId are only used when importing VCF files. EMX files containing their own packages and entities sheet will ignore these two arguments

action: the database action to import with, options are:
	ADD: add records, error on duplicate records
	ADD_UPDATE_EXISTING: add, update existing records
	UPDATE: update records, throw an error if records are missing in the database
	ADD_IGNORE_EXISTING: Adds new records, ignores existing records

Note that for VCF files only ADD is supported
Note that for EMX files only ADD, ADD_UPDATE_EXISTING and UPDATE are supported

Default value is "ADD".

notify: Boolean value to indicate of success and failure should be reported to the user by email.
	Default value is "false".

## Response:
The service responds with a statuscode **201 CREATED** if the preliminary checks went well and the import run has been started, it also returns a href to the metadata of the importrun.
The href can be used to poll the status of the import by checking the status field of the importrun, also the message field of the importrun entity gives some basic feedback on what was imported or what went wrong.
## Examples
### importByURL Example:

    https://molgenis01.gcc.rug.nl/plugin/importwizard/importByUrl
    notify=false&entityTypeId=demo&url=https://raw.githubusercontent.com/molgenis/molgenis/master/molgenis-data-vcf/src/test/resources/testFile.vcf
    
##### Response:

    201 CREATED
    /api/v2/ImportRun/[ImportRunID]

### importFile example using [cURL](https://curl.haxx.se):
    curl -H "x-molgenis-token:[TOKEN]" -X POST -F"file=@path/to/file/test.vcf" -Faction=update -FentityTypeId=newName
    -Fnotify=true https://[SERVER URL]/plugin/importwizard/importFile

A token can be obtained using:

    curl -H "Content-Type: application/json" -X POST -d '{"username"="USERNAME", "password"="YOURPASSWORD"}' https://[SERVER URL]/api/v1/login

##### Response:

    201 CREATED
    /api/v2/ImportRun/[ImportRunID]

## Exceptions:
In case anything went wrong even before starting the importrun a **400 BAD REQUEST** is returned.

#### Examples of known exceptions are:
* Invalid action: [ILLEGAL VALUE] valid values: ADD, ADD_UPDATE_EXISTING, UPDATE, ADD_IGNORE_EXISTING
* Update mode UPDATE is not supported, only ADD is supported for VCF
* A repository with name NewEntity already exists
