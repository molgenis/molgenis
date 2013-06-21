#
## General header
#

set -e # exit if any subcommand or pipeline returns a non-zero status
set -u # exit if any uninitialised variable is used

# If you detect an error, then exit your script by calling this function
exitWithError(){
	errorCode=$1
	errorMessage=$2
	echo "$errorCode: $errorMessage" >> molgenis.error.log
	exit $errorCode
}

# Set location of *.env files
ENVIRONMENT_DIR="."

# Show that the task has started
touch $ENVIRONMENT_DIR/${taskId}.sh.started

# Skip this step if step finished already successfully
if [ -f $ENVIRONMENT_DIR/${taskId}.sh.finished ]; then
	exitWithError 0 "Skipped ${taskId}.sh"
fi