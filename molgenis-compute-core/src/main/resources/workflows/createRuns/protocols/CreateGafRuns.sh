#
# =========================================================================================
# Create folder structure, config files and generate scripts using MOLGENIS/compute
# for the GAF workflow for BCL to FastQ conversion using Illumina's bcl2fastq convertor.
# =========================================================================================
#

#MOLGENIS walltime=00:10:00
#
##
### parameters declaration
##
#
#string umask
#string runJobsDir
#string runResultsDir
#string scriptsDir
#string McWorksheet
#string run
#string bcl2FastqWorkflowDir
#string gafDir
#input arrayFile

echo $taskId

#
# Initialize: resource usage requests + workflow control
#


#
# Change permissions.
#


umask ${umask}

#
# Setup environment for tools we need.
#
module load molgenis_compute/
module list

#
# Create run dirs.
#
mkdir -p ${runJobsDir}
mkdir -p ${runResultsDir}

#
# Create subset of samples for this project.
#
export PERL5LIB=${scriptsDir}/
${scriptsDir}/extract_samples_from_GAF_list.pl --i ${McWorksheet} --o ${runJobsDir}/${run}.csv --c run --q ${run}

#
# Execute MOLGENIS/compute to create job scripts to analyse this project.
#
#sh ${McDir}/molgenis_compute.sh \
#-worksheet=${runJobsDir}/${run}.csv \
#-inputdir=${bcl2FastqWorkflowDir} \
#-templates=${McTemplates}/ \
#-outputdir=${runJobsDir}/ \
#-id=${McId}

sh molgenis_compute.sh \
-p ${bcl2FastqWorkflowDir}/parameters_v5.csv \
-p ${gafDir}/generatedscripts/PathToWorksheet.csv \
-w ${bcl2FastqWorkflowDir}/workflow.csv \
-rundir ${runJobsDir}\
-runid ${runid}
