#string experiment
#list sample
#output expPrinted "The experiment for which we printed samples"
#output nSamples "Number samples in experiment"
#output sampleCopy "Use this to copy sample names"

samplesCopy=()
echo "Experiment $experiment has following samples:"
for value in ${sample[@]}
do
	echo "$value"
	sampleCopy+=( "${value}" )
done

expPrinted=$experiment
nSamples=${#sample[@]}