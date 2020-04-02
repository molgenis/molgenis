# Running integration tests
If you've checked out the code and made changes to the platform, you may want to run the
platform integration tests.
These are some instructions on how to run the integration tests on your developer machine.

## In Maven
To run the integration tests in MAVEN you need to boot the backend services.

```
cd molgenis-platform-integration-tests/integ-test-env;
docker-compose up -d;
```

And then run the MAVEN command.
```
mvn verify -pl molgenis-platform-integration-tests --batch-mode \
  -Dmaven.test.redirectTestOutputToFile=true \
  -Dit_db_user=postgres \
  -Dit_db_password=postgres \
  -Dit_db_port=5432;
```

When finished please execute:
```
docker-compose down
```

> **IMPORTANT:** this is another stack than the one in `molgenis-app/dev-env`. Do NOT run these at the same time.

## In IntelliJ
When developing, you can run the integration tests as usual.

> **IMPORTANT:** make sure you have no MOLGENIS dev-env or other docker-compose stacks running. Check your "docker-tab" in IntelliJ or run `docker ps` on the commandline.

Please boot the backend services with the docker-compose stack in the source-tree.
* Navigate to molgenis-platform-integration-tests/integ-test-env
* Right click the `docker-compose.yaml`, click on *Create 'integ-test-env: Compose...'*
* Check the option `--build, force build images`
* Add the following variables to the environment:

```bash
MOLGENIS_FRONTEND=molgenis/molgenis-frontend:latest
# MacOS: host.docker.internal
# Windows: docker.for.win
# Linux: 172.17.0.1 (bridge) or 127.0.0.1 (host)
MOLGENIS_HOST=host.docker.internal
# Window & MacOS: bridge, Linux: host
NETWORK_MODE=bridge
```
* Run the 'integ-test-env: Compose...' Run configuration
* Then click on the ```molgenis-platform-integration-tests``` module and click *Run --> All Tests (TestNG)*

The code will not make any attempts to delete the integration test database and index.
(But you can easily create and call a cleanup script yourself to use when needed.)

### system properties
By default, IntelliJ reads system properties from your pom.xml and adds those to the
test runs.

> **This won't work!**

You should disable this feature:
- Navigate to the *Running Tests* properties
  - **For Windows**

    Go to *Settings / Build, Execution, Deployment / Build tools / Maven / Running Tests*

  - **For Mac**

    Go to *Preferences / Settings / Build, Execution, Deployment / Build tools / Maven / Running Tests*

- Uncheck the box labelled `systemPropertyVariables`

## When building pull requests
A travis config file is added that shows travis how to build molgenis and execute the
integration tests.
These will be run on travis-ci.org when you make a pull request to the github molgenis/molgenis
repository.
The integration tests run in maven in the postgres database provided by travis.
user is travis, database name is travis, password is blank
The build script uses the default options to create a elasticsearch cluster.

## Concurrent builds on a single machine
The maven build can handle concurrent builds on a single machine.
It'll launch a new elasticsearch instance for each run.
However, starting up additional elasticsearch instances can become rather slow for concurrent builds.
