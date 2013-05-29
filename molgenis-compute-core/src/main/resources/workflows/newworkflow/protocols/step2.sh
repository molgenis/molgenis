#input wf
#input date
#list strings

echo "Workflow name: ${wf}"
echo "Created: ${date}"

echo "Result of step1.sh:"
for s in "${strings[@]}"
do
    echo ${s}
done