# General header

#
## Set location of *.env files
#
ENVIRONMENT_DIR="."

#
## Load functions to handle errors
#
exitWithError(){
	errorCode=$1
	errorMessage=$2
	echo "$errorCode: $errorMessage" >> molgenis.error.log
	exit $1
}