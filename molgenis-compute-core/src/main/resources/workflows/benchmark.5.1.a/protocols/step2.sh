#string guest
#list strings
#list strings2

echo "Result of step1.sh:"
for s in "${strings[@]}"
do
    echo ${s}
done

for s in "${strings2[@]}"
do
    echo ${s}
done



echo "(FOR TESTING PURPOSES: your runid is ${runid})"
