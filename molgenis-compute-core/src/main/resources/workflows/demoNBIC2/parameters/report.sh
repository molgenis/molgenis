#MOLGENIS walltime=00:05:00
#input workflowConstant
#input dis
#list sample
#list glucose
#list risk

for i in ${!sample[*]}; do
	echo "Sample ${sample[$i]} with glucose ${glucose[$i]} has risk ${risk[$i]}"
done