<#-- see http://www.graphviz.org/doc/info/colors.html -->
<#include "GeneratorHelper.ftl">
<#function csv items>
	<#local result = "">
	<#list items as item>
		<#if item_index != 0>
			<#local result = result + ", ">
		</#if>
		<#if item?is_hash>
			<#local result = result + item.name>
		<#else>
			<#local result = result + "'"+item+"'">
		</#if>
	</#list>
	<#return result>
</#function>
digraph G {
        color = "white"
		compound = true
		fontname = "Bitstream Vera Sans"
		fontsize = 8
		pagedir="TB"
		rankdir="BT"
		bgcolor = "lightyellow"  
		labelfloat = "true"
		mode = "hier"
		overlap = "false"
		splines = "true"
		layers = "1:2"
		clusterrank = "local"
		outputorder="edgesfirst"

        node [
			fontname = "Arial"
            fontsize = 8
            shape = "record"
            color = "#808080"
            style="filled"
            fillcolor = "white"
            layer = "2"
            colorscheme = pastel19
        ]

        edge [
                fontname = "Bitstream Vera Sans"
                fontsize = 8
                layer = "1"
        ]

<#list rules?keys as rule>
        edge [
             taillabel = "${rules[rule]}"
        ]
	${rule}
</#list>
}