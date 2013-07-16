#string workflowName
#list mytool

for s in "${mytool[@]}"
do
    echo "tool1"
    echo ${s}
done