# Import api

This API provides programmatic access to the [Import guide](guide-data-import.md).

## Request

[SERVER URL]/plugin/importwizard/importFile/

This endpoint can be used to import a file into MOLGENIS by providing the file by for example the use of a webform.

### Required parameter: MultipartFile file

[SERVER URL]/plugin/importwizard/importByUrl/
This endpoint can be used to import a file into MOLGENIS by providing a URL to the file.

### Required parameter: String url

### optional parameters for both endpoints:
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

### Response:

    201 CREATED
    /api/v2/ImportRun/[ImportRunID]

### importFile example using [cURL](https://curl.haxx.se):
    curl -H "x-molgenis-token:[TOKEN]" -X POST -F"file=@path/to/file/test.vcf" -Faction=update -FentityTypeId=newName
    -Fnotify=true https://[SERVER URL]/plugin/importwizard/importFile

A token can be obtained using:

    curl -H "Content-Type: application/json" -X POST -d '{"username"="USERNAME", "password"="YOURPASSWORD"}' https://[SERVER URL]/api/v1/login

#### Response:

    201 CREATED
    /api/v2/ImportRun/[ImportRunID]

## Exceptions:
In case anything went wrong even before starting the importrun a **400 BAD REQUEST** is returned.

### Examples of known exceptions are:
* Invalid action: [ILLEGAL VALUE] valid values: ADD, ADD_UPDATE_EXISTING, UPDATE, ADD_IGNORE_EXISTING
* Update mode UPDATE is not supported, only ADD is supported for VCF
* A repository with name NewEntity already exists
