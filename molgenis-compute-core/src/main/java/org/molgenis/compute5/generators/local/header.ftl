#
## General header
#

set -e # exit if any subcommand or pipeline returns a non-zero status
set -u # exit if any uninitialised variable is used

# Set location of *.env files
ENVIRONMENT_DIR="."

# If you detect an error, then exit your script by calling this function
exitWithError(){
	errorCode=$1
	errorMessage=$2
	echo "$errorCode: $errorMessage --- TASK '${taskId}.sh' --- ON $(date +"%Y-%m-%d %T"), AFTER $(( ($(date +%s) - $MOLGENIS_START) / 60 )) MINUTES" >> $ENVIRONMENT_DIR/molgenis.error.log
	exit $errorCode
}

# For bookkeeping how long your task takes
MOLGENIS_START=$(date +%s)

# Skip this step if step finished already successfully
if [ -f $ENVIRONMENT_DIR/${taskId}.sh.finished ]; then
	exitWithError 0 "Skipped."
fi

# Show that the task has started
touch $ENVIRONMENT_DIR/${taskId}.sh.started