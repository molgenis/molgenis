<#foreach t in tasks>
echo "--- begin step: ${t.name} ---"
echo " "

bash ${t.name}.sh

echo " "
echo "--- end step: ${t.name} ---"
echo " "
echo " "
</#foreach>