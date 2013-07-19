#string workflowName
#string creationDate
#list out

echo "Workflow name: ${workflowName}"
echo "Created: ${creationDate}"

echo "Result of step1.sh:"
for s in "${out[@]}"
do
    echo ${s}
done


echo "(FOR TESTING PURPOSES: your runid is ${runid})"