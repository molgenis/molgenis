# Molgenis API Test

[![Build Status](https://molgenis50.gcc.rug.nl/jenkins/buildStatus/icon?job=Nightly%20live%20API%20Tests)](http://www.molgenis.org/jenkins/job/Nightly%20live%20API%20Tests/)

Collection of integration tests to run against a live server. Tests use a java http client to test the molgenis API.

##### Maven properties:

`REST_TEST_HOST=[url_of_live_molgenis_to_test_against]` // default http://localhost:8080  
`REST_TEST_ADMIN_NAME=[name_of_admin_user]` // user name of user that acts as 'admin' user, this user is used to for test setup and teardown  
`REST_TEST_ADMIN_PW=[password_for_admin_users]`

##### Running on local machine:

`mvn test -Dtest=[RestControllerIT,...comma-separated-list-of-test-classes] -DREST_TEST_ADMIN_NAME="[admin_name]" -DREST_TEST_ADMIN_PW="[admin_pw]"` 


### Public data upload test

The class `ImportPublicDataIT` is used as a template for running tests that upload public customer data to live molgenis server. 

The test uploads a file and checks if the upload was successful. After the test has run the imported data is removed using the supplied 
package or list of entities. 

Supported file types: .xlsx and .vcf (make sure the file name is a valid molgenis entity id)

##### Properties and environment variables:

All of the above mentioned maven properties + the following environment variables  
`molgenis.test.upload.folder`  // path to folder to load data files from  
`molgenis.test.upload.file`  // file to use in test  
`molgenis.test.upload.package.to.remove`  // optional package to delete after test (success or failure)  
`molgenis.test.upload.entities.to.remove` // optional comma separated list of entities to remove after test  (success or failure) 
`molgenis.test.upload.check.urls`  // comma separated list of urls to test via GET for testing successful upload  

##### Running on local machine:

set environment variables   
`mvn test -Dtest=ImportPublicDataIT -DREST_TEST_ADMIN_NAME="[admin_name]" -DREST_TEST_ADMIN_PW="[admin_pw]"` 

##### Running on a CI-server

We run separate builds for each individual customer. To add a job for a new customer please check the existing builds. You can copy a job and update the configuration.

Check CI-jobs: [![Import public data job](https://molgenis50.gcc.rug.nl/jenkins/buildStatus/icon?job=Nightly%20live%20API%20Tests)]()
