#RESOURCES mem=10G cpu=4 file=10G
#string inputMapPed "Prefix of input files in ped/map format"
#output outputMapPed ${inputMapPed}.out

getFile ${inputMapPed}.map
getFile ${inputMapPed}.ped

plink --file ${inputMapPed} --out ${SCRIPT_TMP_DIR}/outputMapPed

#Get return code from last program call
returnCode=$?

#why MOVE_FROM_TMP???
if [ $returnCode -eq 0 ]
then
	#MOVE_FROM_TMP ${SCRIPT_TMP_DIR}/outputMapPed.map ${outputMapPed}.map
	#MOVE_FROM_TMP ${SCRIPT_TMP_DIR}/outputMapPed.ped ${outputMapPed}.ped
else
	exitWithError Error running plink
fi

putFile ${outputMapPed}.map
putFile ${outputMapPed}.ped

