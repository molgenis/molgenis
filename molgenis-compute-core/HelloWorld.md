# MOLGENIS Compute 5.x Hello World example

Born from bioinformatics and life sciences, MOLGENIS compute is a flexible shell script framework to generate big data workflows that can run parallel on clusters and grids. Following this tutorial, you will create a workflow and run it on your own PC.

## Deploy the downloaded distribution
	unzip molgenis-compute-core-0.0.1-SNAPSHOT-distribution.zip
	cd molgenis-compute-core-0.0.1-SNAPSHOT
	chmod +x molgenis_compute.sh
	PATH=${PATH}:${PWD}

## Show the help
The following commands show the help.

	molgenis_compute.sh
	molgenis_compute.sh -h
	
## Create your first workflow
	molgenis_compute.sh --create ~/tmp/demoMolgenisCompute/helloWorld

## Inspect the workflow
	cd ~/tmp/demoMolgenisCompute/helloWorld

### Inspect the two analysis protocols in your workflow:
	cat protocols/step1.sh
	cat protocols/step2.sh

### Inspect workflow and its parameters
	cat workflow.csv
	cat workflow.defaults.csv

### Inspect the parameters
	cat parameters.csv

### Inspect the MOLGENIS Compute parameter defaults
	cat compute.properties

## Generate a workflow with scripts for your parameters
The following commands are equivalent. Parameters that are not specified are retrieved from the compute.properties file.

	molgenis_compute.sh -g
	molgenis_compute.sh -g -p parameters.csv 
	molgenis_compute.sh    -p parameters.csv -w workflow.csv

## Let's run the generated scripts
	
	cd rundir

If you want to, you may inspect the generated scripts first. After which you can run them as follows.

	sh submit.sh
	
# Adapt the workflow for your own case
To be continued...