#input workflowConstant
#input sample
#list glucose
#output risk

for i in ${!glucose[*]}; do
	if ((10 < ${glucose[$i]}));
		then risk[$i]="yes";
		else risk[$i]="no";
	fi;
done

