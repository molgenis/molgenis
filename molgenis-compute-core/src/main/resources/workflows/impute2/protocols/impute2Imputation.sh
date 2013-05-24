#MOLGENIS nodes=1 cores=1 mem=4

#input knownHapsG
#input m
#input h
#input l
#input additonalImpute2Param1
#input additonalImpute2Param2
#input additonalImpute2Param3
#input additonalImpute2Param4
#input chr
#input fromChrPos
#input toChrPos
#input imputationIntermediatesFolder
#input impute2Bin

#input impute2version
#input knownHapsG

#output impute2chunk
#output impute2chunkInfo

module load impute/${impute2version}


tmpOutput="${imputationIntermediatesFolder}/~chr${chr}_${fromChrPos}-${toChrPos}"
finalOutput="${imputationIntermediatesFolder}/chr${chr}_${fromChrPos}-${toChrPos}"

echo "knownHapsG: ${knownHapsG}";
echo "chr: ${chr}"
echo "fromChrPos: ${fromChrPos}"
echo "toChrPos: ${toChrPos}"
echo "interMediFolder: ${imputationIntermediatesFolder}"
echo "tmpOutput: ${tmpOutput}"
echo "finalOutput: ${finalOutput}"

impute2chunk=${finalOutput}
impute2chunk_info=${finalOutput}_info
impute2chunk_info_by_sample=${finalOutput}_info_by_sample
impute2chunk_summary=${finalOutput}_summary
impute2chunk_warnings=${finalOutput}_warnings

alloutputsexist \
	"${finalOutput}" \
	"${finalOutput}_info" \
	"${finalOutput}_info_by_sample" \
	"${finalOutput}_summary" \
	"${finalOutput}_warnings"

startTime=$(date +%s)

echo "tmpOutput: ${tmpOutput}"

getFile $knownHapsG
inputs $knownHapsG

getFile $m
inputs $m

# $h can be multiple files. Here we will check each file and do a getFile, if needed, for each file
for refH in $h
do
	echo "Reference haplotype file: ${refH}"
	getFile $refH
	inputs $refH
done

# $l can be multiple files. Here we will check each file and do a getFile, if needed, for each file
for refL in $l
do
	echo "Reference legend file: ${refL}"
	getFile $refL
	inputs $refL
done

# DECLARE POSSIBLE VALUES FOR additonalImpute2Param HERE
impute2FileArg[0]="-sample_g_ref"
impute2FileArg[1]="-exclude_samples_g"
impute2FileArg[2]="-exclude_snps_g"
impute2FileArg[3]="-sample_g"

# This function test if element is in array
# http://stackoverflow.com/questions/3685970/bash-check-if-an-array-contains-a-value
containsElement () {
  local e
  for e in "${@:2}"; do [[ "$e" == "$1" ]] && return 1; done
  return 0
}


aditionalArgsArray=($additonalImpute2Param1 $additonalImpute2Param2 $additonalImpute2Param3 $additonalImpute2Param4)

# Loop over all aditional args. If arg is encounterd that requeres file then do inputs and getFile on next element
for (( i=0; i<${#aditionalArgsArray[@]}; i++ ));
do
	currentArg=${aditionalArgsArray[$i]}
	containsElement $currentArg ${impute2FileArg[@]}
	if [[ $? -eq 1 ]]; 
	then 
		
		i=`expr $i + 1`
		
		file=${aditionalArgsArray[$i]}
		
		echo "File for this argument: $currentArg will get and is requered for this script to start $file"
		inputs $file
		get $file
		echo "Found additional Impute2 file: $file"
		
	fi
	
done


mkdir -p $imputationIntermediatesFolder


$impute2Bin \
	-known_haps_g $knownHapsG \
	-m $m \
	-h $h \
	-l $l \
	-int $fromChrPos $toChrPos \
	-o $tmpOutput \
	-use_prephased_g \
	$additonalImpute2Param1 \
	$additonalImpute2Param2 \
	$additonalImpute2Param3 \
	$additonalImpute2Param4
		
#Get return code from last program call
returnCode=$?

echo "returnCode impute2: ${returnCode}"

if [ $returnCode -eq 0 ]
then

	#If there are no SNPs in this bin we will create empty files 
	if [ ! -f ${tmpOutput}_info ]
	then
	
		echo "Impute2 did not output files. Usually this means there where no SNPs in this region so, generate empty files"
		echo "Touching file: ${tmpOutput}"
		echo "Touching file: ${tmpOutput}_info"
		echo "Touching file: ${tmpOutput}_info_by_sample"
	
		touch ${tmpOutput}
		touch ${tmpOutput}_info
		touch ${tmpOutput}_info_by_sample
	
	fi
	
		
	
	echo -e "\nMoving temp files to final files\n\n"

	for tempFile in ${tmpOutput}* ; do
		finalFile=`echo $tempFile | sed -e "s/~//g"`
		echo "Moving temp file: ${tempFile} to ${finalFile}"
		mv $tempFile $finalFile
		putFile $finalFile
	done
	
elif [ `grep "ERROR: There are no type 2 SNPs after applying the command-line settings for this run"  ${tmpOutput}_summary | wc -l | awk '{print $1}'` == 1 ]
then

	if [ ! -f ${tmpOutput}_info ]
	then
		echo "Impute2 found no type 2 SNPs in this region. We now create empty output"
		echo "Touching file: ${tmpOutput}"
		echo "Touching file: ${tmpOutput}_info"
		echo "Touching file: ${tmpOutput}_info_by_sample"
	
		touch ${tmpOutput}
		touch ${tmpOutput}_info
		touch ${tmpOutput}_info_by_sample
		
	fi
	
	echo -e "\nMoving temp files to final files\n\n"

	for tempFile in ${tmpOutput}* ; do
		finalFile=`echo $tempFile | sed -e "s/~//g"`
		echo "Moving temp file: ${tempFile} to ${finalFile}"
		mv $tempFile $finalFile
		putFile $finalFile
	done
		

else
  
	echo -e "\nNon zero return code not making files final. Existing temp files are kept for debugging purposes\n\n"
	#Return non zero return code
	exit 1

fi

endTime=$(date +%s)


#Source: http://stackoverflow.com/questions/12199631/convert-seconds-to-hours-minutes-seconds-in-bash

num=$endTime-$startTime
min=0
hour=0
day=0
if((num>59));then
    ((sec=num%60))
    ((num=num/60))
    if((num>59));then
        ((min=num%60))
        ((num=num/60))
        if((num>23));then
            ((hour=num%24))
            ((day=num/24))
        else
            ((hour=num))
        fi
    else
        ((min=num))
    fi
else
    ((sec=num))
fi
echo "Running time: ${day} days ${hour} hours ${min} mins ${sec} secs"

