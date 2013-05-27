#PBS -N ${taskId}
#PBS -q ${queue}
#PBS -l nodes=${nodes}:ppn=${ppn}
#PBS -l walltime=${walltime}
#PBS -l mem=${mem}
#PBS -e ${taskId}.err
#PBS -o ${taskId}.out
#PBS -W umask=0007

#
## Load functions to handle errors
#
exitWithError(){
	errorCode=$1
	errorMessage=$2
	echo "$errorCode: $errorMessage" >> molgenis.error.log
	exit $1
}