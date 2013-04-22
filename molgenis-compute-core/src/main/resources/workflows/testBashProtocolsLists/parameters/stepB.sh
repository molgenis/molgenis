#string constant
#list experiment
#list nSamples

echo "We are now in stepB.ftl and show number of samples per experiment:"
echo "Experiment $experiment has following samples:"
for (( i=1; i<$(( ${#experiment[@]}-1 )); i++ ))
do
	echo "Experiment ${experiment[i]} has ${nSamples[i]} samples."
done