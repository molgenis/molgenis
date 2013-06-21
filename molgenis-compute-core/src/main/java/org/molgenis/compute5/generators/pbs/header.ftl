#PBS -N ${taskId}
#PBS -q ${queue}
#PBS -l nodes=${nodes}:ppn=${ppn}
#PBS -l walltime=${walltime}
#PBS -l mem=${mem}
#PBS -e ${taskId}.err
#PBS -o ${taskId}.out
#PBS -W umask=0007

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
ENVIRONMENT_DIR="$PBS_O_WORKDIR"

# Show that the task has started
touch $ENVIRONMENT_DIR/${taskId}.sh.started

# Skip this step if step finished already successfully
if [ -f $ENVIRONMENT_DIR/${taskId}.sh.finished ]; then
	exitWithError 0 "Skipped ${taskId}.sh"
fi

# Define the root to all your tools and data
WORKDIR=/target/gpfs2/gcc/


# Source getFile, putFile, inputs, alloutputsexist
include () {
	if [[ -f "$1" ]]; then
		source "$1"
		echo "sourced $1"
	else
		echo "File not found: $1"
	fi		
}
include $WORKDIR/gcc.bashrc
include $WORKDIR/tools/scripts/transferData.sh
include $WORKDIR/tools/scripts/import.sh