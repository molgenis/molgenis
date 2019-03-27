:: Assign all variables needed for API tests
SET REST_TEST_HOST=http://localhost:8080
SET REST_TEST_ADMIN_NAME=admin
SET REST_TEST_ADMIN_PW=admin
SET MOLGENIS_TEST_UPLOAD_FOLDER=src/test/resources/public-data
SET MOLGENIS_TEST_UPLOAD_FILE=gonl_chr16_snps_indels_r5.vcf
SET MOLGENIS_TEST_UPLOAD_CHECK_URLS=menu/main/dataexplorer?entity=gonlchr16snps_indelSample
SET MOLGENIS_TEST_UPLOAD_PACKAGE_TO_REMOVE=gonl_chr16_snps_indels_r5,gonl_chr16_snps_indels_r5Sample

:: Run the actual tests in MAVEN
mvn test -Dtest=ImportPublicDataIT