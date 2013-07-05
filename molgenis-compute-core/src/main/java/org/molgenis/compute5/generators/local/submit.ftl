# First cd to the directory with the *.sh and *.finished scripts
<#noparse>
MOLGENIS_scriptsDir=$( cd -P "$( dirname "$0" )" && pwd )
echo "cd $MOLGENIS_scriptsDir"
cd $MOLGENIS_scriptsDir
</#noparse>

# Use this to indicate that we skip a step
skip(){
	echo "0: Skipped --- TASK '$1' --- ON $(date +"%Y-%m-%d %T")" >> molgenis.skipped.log
}

<#foreach t in tasks>
# Skip this step if step finished already successfully
if [ -f ${t.name}.sh.finished ]; then
	skip ${t.name}.sh
	echo "Skipped ${t.name}.sh"
else
	echo "--- begin step: ${t.name} ---"
	echo " "
	bash ${t.name}.sh
	echo " "
	echo "--- end step: ${t.name} ---"
	echo " "
	echo " "
fi
</#foreach>