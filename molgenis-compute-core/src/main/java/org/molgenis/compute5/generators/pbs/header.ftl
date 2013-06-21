#PBS -N ${taskId}
#PBS -q ${queue}
#PBS -l nodes=${nodes}:ppn=${ppn}
#PBS -l walltime=${walltime}
#PBS -l mem=${mem}
#PBS -e ${taskId}.err
#PBS -o ${taskId}.out
#PBS -W umask=0007

# Define the root to all your tools and data
WORKDIR=/target/gpfs2/gcc/

#
## source getFile, putFile, inputs, alloutputsexist
#
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

#
## Load functions to handle errors
#
exitWithError(){
	errorCode=$1
	errorMessage=$2
	echo "$errorCode: $errorMessage" >> molgenis.error.log
	exit $1
}

#
## Set location of *.env files
#
ENVIRONMENT_DIR="$PBS_O_WORKDIR"
