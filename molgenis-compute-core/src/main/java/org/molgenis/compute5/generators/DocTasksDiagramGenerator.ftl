<#assign color = 0>
digraph G {
node [fontname = "Arial"
            fontsize = 8
            shape = "record"
            color = "#808080"
            style="filled"
            fillcolor = "white"
            layer = "2"
            colorscheme = pastel19]
            
<#list tasks as task>
<#assign color = color + 1/>
<#if color == 9 ><#assign color = 1/></#if>
${task.name}[
 	fillcolor =  "${color}"
 	label = "{ ${task.name} | <#list task.parameters?keys as col><#if !task.parameters[col]?is_hash >${col}=<#if task.parameters[col]?is_sequence>\[<#list task.parameters[col] as val><#if val_index &gt; 0>,</#if>${val?html}</#list>\]<#else>${task.parameters[col]?html}</#if>\n</#if></#list>}"]
<#list task.previousTasks as previous>
${previous} -> ${task.name}
</#list>
</#list>
}