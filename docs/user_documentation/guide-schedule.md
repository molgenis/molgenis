# Scheduled Jobs
A scheduled job is a job that is scheduled to run at specific moments in time.
The Scheduled Jobs plugin allows you to schedule jobs.
You can find it in the Admin menu, under Scheduled Jobs.

The table at the top shows you all of the scheduled jobs on your server.

## Add a scheduled job
You can add a new scheduled job by pressing the green plus button and filling in the form.
The `parameters` field of the form should be entered as a [JSON](http://www.json.org) object,
with a name-value pair for each parameter. The format of the JSON depends on the
type of the job you're scheduling.

## Job type
The job type determines what will happen when the job is triggered.

### File ingest
Downloads and imports data from a CSV file to a single existing entity.
The file's columns should match the entity's attribute names.

#### parameters
| name           | description                                                       |
|----------------|-------------------------------------------------------------------|
| url            | URL to download the file to ingest from                           |
| loader         | Loader to use to ingest the file, currently only `CSV` is allowed |
| targetEntityId | ID of the entity to import to

So for example:
```json
{
  "url"            : "http://filesource.mydomain.example/city.csv",
  "loader"         : "CSV",
  "targetEntityId" : "base_city"
}
```

### Amazon Bucket file ingest
Files stored in an [Amazon Bucket](http://docs.aws.amazon.com/AmazonS3/latest/dev/UsingBucket.html) can be imported automatically using a Bucket Job.
The [AWS CLI](https://aws.amazon.com/cli/) needs to be installed on the server where MOLGENIS is running for this to work.
A profile should be configured for bucket access:

    >aws configure --profile yourprofilename
    AWS Access Key ID [None]: secret
    AWS Secret Access Key [None]: ultrasecret
    Default region name [None]:
    Default output format [None]:
    
#### parameters
| name           | description                                                       |
|----------------|-------------------------------------------------------------------|
| bucket         | the name of the bucket to download from                           |
| key            | the key for the file you wish to download from the bucket         |
| expression     | boolean stating if the key was filled out as an exact match or a regular expression. If the key is an expression the most recent matching file is imported. |
| profile        | your amazon bucket profile |
| targetEntityId | the name of the entity to import to, this is an optional field used for files that have a different sheet name instead of the targeted entityType |

### Mapping
Runs an existing Mapping Service project.
#### parameters
| name               | description                                                     |
|--------------------|-----------------------------------------------------------------|
| mappingProjectId   | The ID of the mapping project                                   |
| targetEntityTypeId | The ID of the created EntityType, may be an existing EntityType |
| addSourceAttribute | Indicates if a source attribute should be added to the EntityType, ignored when mapping to an existing EntityType |
| packageId          | The ID of the target package, ignored when mapping to an existing EntityType |
| label              | The label of the target EntityType, ignored when mapping to an existing EntityType |

### Script
Runs an existing Script, defined in the Script plugin.

#### parameters
| name       | description                                                         |
|------------|---------------------------------------------------------------------|
| name       | The name of the Script to run                                       |
| parameters | A String containing a JSON object describing the Script parameters. |
So for instance, to run a Script named "concat" with parameters a = "foo" and b="bar, you'd fill in
```json
{
  "name"       : "concat",
  "parameters" : "{\"a\" : \"foo\", \"b\" : \"bar\"}"
}
```