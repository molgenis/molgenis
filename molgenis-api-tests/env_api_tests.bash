#!/bin/bash

# Assign all variables needed for API tests
REST_TEST_HOST=http://localhost:8080
REST_TEST_ADMIN_NAME=admin
REST_TEST_ADMIN_PW=admin
MOLGENIS_TEST_UPLOAD_FOLDER=src/test/resources/public-data
MOLGENIS_TEST_UPLOAD_FILE=gonl_chr16_snps_indels_r5.vcf
MOLGENIS_TEST_UPLOAD_CHECK_URLS=menu/main/dataexplorer?entity=gonlchr16snps_indelSample
MOLGENIS_TEST_UPLOAD_PACKAGE_TO_REMOVE=gonl_chr16_snps_indels_r5,gonl_chr16_snps_indels_r5Sample

# Run the actual tests in MAVEN
mvn test -Dtest=ImportPublicDataIT