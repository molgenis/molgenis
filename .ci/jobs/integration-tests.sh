#!/bin/bash

travis_wait

# run the integration tests
./mvnw test -pl molgenis-platform-integration-tests --batch-mode \
  -Dit_db_user=postgres \
  -Dit_db_password=molgenis \
  -Dit_db_port=5433