mkdir target
mkdir target/doc
for in in src/main/resources/*.asciidoc
do
	out=$(basename "$in")
    out=${out%.*}
    echo ${out}
	asciidoc -a navinfo1 -b bootstrap-original -o target/doc/${out}.html ${in}
done
