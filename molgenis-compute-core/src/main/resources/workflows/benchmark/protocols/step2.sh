#string wf
#string date
#list strings

echo "Workflow name: ${wf}"
echo "Created: ${date}"

echo "Result of step1.sh:"
for s in "${strings[@]}"
do
    echo ${s}
done


echo "(FOR TESTING PURPOSES: your runid is ${runid})"