DIR="$( cd "$( dirname "<#noparse>${BASH_SOURCE[0]}</#noparse>" )" && pwd )"
touch $DIR/compute.started

<#foreach t in tasks>
#${t.name}
${t.name}=$(qsub -N ${t.name}<#if t.previousTasks?size &gt; 0> -W depend=afterok<#foreach d in t.previousTasks>:$${d}</#foreach></#if> ${t.name}.sh)
echo $${t.name}
sleep 0
</#foreach>