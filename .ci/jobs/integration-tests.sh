#!/bin/bash

export MAVEN_SKIP_RC=true
export MAVEN_OPTS="-XX:TieredStopAtLevel=1 -noverify"

# run the integration and migration tests
mvn verify -pl molgenis-platform-integration-tests \
  -pl molgenis-data-migrate --batch-mode --quiet \
  -Dmaven.test.redirectTestOutputToFile=true \
  -Dit_db_user=postgres \
  -Dit_db_password=molgenis \
  -Dit_db_port=5432