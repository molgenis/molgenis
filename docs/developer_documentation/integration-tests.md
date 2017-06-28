# Running the platform integration tests
If you've checked out the code and made changes to the platform, you may want to run the 
platform integration tests.
These are some instructions on how to run the integration tests on your developer machine.

## in Maven
To run integration tests in maven, call `mvn clean verify`. By default:
* The build will expect a standard postgres database server to be running on default port that is accessible for user `molgenis`
with password `molgenis` and database creation privileges
* A new postgres database will be created for each run with timestamped name `molgenis_test_yyMMddHHmmssSSS`
* A new empty elasticsearch cluster will be launched on random ports with timestamped clustername `molgenisyyMMddHHmmssSSS`.
* The elasticsearch instance is configured from config files specified in `src/test/resources/conf/elasticsearch`. Since the tests use relatively small amounts of data, the configuration uses only 500 MB memory.
* Both database and cluster will be removed by maven after the integration tests have finished

### Using a pre-existing database
To run the integration tests on a pre-existing database, disable the `create-it-db` maven profile: `mvn clean verify -P'!create-it-db'`.

By default the build will connect to database `molgenis_test` on a local postgres installation, the username and password will both be `molgenis`.
You can override these settings in the molgenis-platform-integration-tests pom.xml file or in command line parameters:
```
mvn clean verify -P'!create-it-db' -Dit_db_name=myname -Dit_db_user=postgres -Dit_db_password` 
```
(No value means a blank password.)

### Using a pre-existing elasticsearch cluster
To run the integration tests on a pre-existing elasticsearch cluster, disable the `create-it-es` maven profile: `mvn clean verify -P'!create-it-es'`

By default the index name will be `molgenis`, and the transport address will be `localhost:9300`.
You can override these settings in the molgenis-platform-integration-tests pom.xml file or in  command line parameters:
```
mvn clean verify -P'!create-it-es' -Delasticsearch.transport.addresses=localhost:12345 -Delasticsearch.cluster.name=testcluster`
```

## In IntelliJ
When developing, you can run the integration tests as usual.
The test code expects a postgres database to be running.
You can configure the database credentials and jdbc url in `src/test/resources/conf/molgenis.properties`

The code will not make any attempts to delete the integration test database and index.
(But you can easily create and call a cleanup script yourself to use when needed.)

### system properties
By default, IntelliJ reads system properties from your pom.xml and adds those to the
test runs. 

> **This won't work!** 

You should disable this feature:
1. Go to preferences / Build, Execution, Deployment / Maven / Running Tests 
2. Uncheck the box labelled `systemPropertyVariables`

## When building pull requests

A travis config file is added that shows travis how to build molgenis and execute the
integration tests.
These will be run on travis-ci.org when you make a pull request to the github molgenis/molgenis
repository.
The integration tests run in maven in the postgres 9.6 database provided by travis.
user is travis, database name is travis, password is blank
The build script uses the default options to create a elasticsearch cluster.

## Concurrent builds on a single machine
The maven build can handle concurrent builds on a single machine.
It'll launch a new elasticsearch instance for each run.
However, starting up additional elasticsearch instances can become rather slow for concurrent builds.