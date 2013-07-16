#string mytool

echo "I am using "${mytool}

for s in "${mytool[@]}"
do
    echo ${s}
done