#MOLGENIS walltime=15:00:00 nodes=1 cores=4 mem=6
#description BWA tool
#string bwaVersion version of BWA
#string indexFile
#string in_fqgz
#output bwaCores ${cores}
#output out_bam ${in_fqgz}.bam

module load bwa/${bwaVersion}

getFile ${indexFile}
getFile ${in_fqgz}
alloutputsexist "${out_bam}"

bwa aln \
${indexFile} \
${in_fqgz} \
-t ${bwaCores} \
-f ${out_bam}


putFile ${out_bam}