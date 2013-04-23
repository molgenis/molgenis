#string experiment
#list sample
#output descriptiveSampleNamesOUT "Use this to copy sample names"

echo "stepA.sh: create more descriptive sample names for samples in experiment $experiment."

for s in ${sample[@]}
do
	descriptiveSampleNamesOUT+=( "${s}_from_${experiment}" )
done