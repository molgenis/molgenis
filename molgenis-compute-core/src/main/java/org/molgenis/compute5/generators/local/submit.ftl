<#foreach t in tasks>
echo "--- begin step: ${t.name} ---"
echo " "
sh ${t.name}.sh
echo " "
echo "--- end step: ${t.name} ---"
</#foreach>