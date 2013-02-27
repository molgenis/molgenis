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
<#list workflow.steps as step>            
<#assign color = color + 1/>
<#if color == 9 ><#assign color = 1/></#if>
${step.name}[
 	fillcolor =  "${color}"
 	label = "{ ${step.name} | <#list step.parameters?keys as key>${key}=${step.parameters[key]}\n</#list> | <#list step.protocol.outputs as output>${output.name} ${output.value?rtf?xml}\n</#list> }"]
<#list step.previousSteps as previous>
${previous} -> ${step.name}
</#list>
</#list>
}