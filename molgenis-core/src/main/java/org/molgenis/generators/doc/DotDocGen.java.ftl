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

/* entities outside modules*/
<#list entities as entity><#if entities?seq_contains(entity) && (!entity.system || rendersystem) && !entity.association>
               "${JavaName(entity)}" [
                	    style = "filled"
                	    fillcolor =  "white"
			        	<#if entity.abstract>
			        	fontname = "Arial-Italic"
			        	fontcolor = "dimgrey"
			        	color = "dimgrey"
			        	<#else>
			        	fontname = "Arial"
			        	fontcolor = "black"
			        	color = "black"
			        	</#if>
                
                        label = "{<#if entity.abstract>Interface:</#if>${JavaName(entity)}<#if entity.hasImplements()>\n implements ${csv(entity.getImplements())}</#if><#if entity.hasAncestor()>\n extends ${name(entity.getAncestor())}</#if>|<#list entity.allFields as f><#if !f.system>${name(f)} : ${f.type}<#if f.type=="xref" || f.type="mref">-&gt;${name(f.xrefEntity)}</#if><#if !f.nillable>*</#if>\l</#if></#list>}"
                ]

</#if></#list>
/*entities inside modules*/
<#assign colorscheme = "pastel19"/>
<#assign color = 0>
<#list model.modules as m>
<#assign color = color + 1>
<#if color == 9 ><#assign color = 1/><#assign colorscheme = "set39"/></#if>
/*        subgraph cluster_${m_index} {
        		rankdir = "TB"
        		pagedir = "TB"
                label = "${name(m)}"
                labelloc = bottom
				fillcolor = "white"
                style="filled"*/

<#list m.entities as entity><#if entities?seq_contains(entity) && (!entity.system || rendersystem) && !entity.association>
                "${JavaName(entity)}" [
                	    style = "filled"
                	    fillcolor =  "${color}"
			        	<#if entity.abstract>
			        	fontname = "Arial-Italic"
			        	fontcolor = "dimgrey"
			        	color = "dimgrey"
			        	<#else>
			        	fontname = "Arial"
			        	fontcolor = "black"
			        	color = "black"
			        	</#if>
                
                        label = "{<#if entity.abstract>Interface:</#if>${JavaName(entity)}<#if entity.hasImplements()>\n implements ${csv(entity.getImplements())}</#if><#if entity.hasAncestor()>\n extends ${entity.getAncestor().getName()}</#if>|<#list entity.allFields as f><#if !f.system>${name(f)} : ${f.type}<#if f.type=="xref" || f.type="mref">-&gt;${name(f.xrefEntity)}</#if><#if !f.nillable>*</#if>\l</#if></#list>}"
                ]
</#if></#list>
/*        }  */
</#list>

/*interface relationships*/
        edge [
                arrowhead = "empty"
                color = "#808080"
        ]
 <#-- we don't render interface relationships as that seems to be confusing        
<#list entities as entity>
    <#if entity.hasImplements()>
    	<#list entity.implements as interface><#if entities?seq_contains(interface) && (!interface.system || rendersystem)>
    	"${JavaName(entity)}" -> "${JavaName(interface)}"
    	</#if></#list>
    </#if>
</#list>
-->

/*inheritance relationships*/
        edge [
                arrowhead = "empty"
                color = "black"
        ]
        
<#list entities as entity>
	<#if entities?seq_contains(entity) && entity.hasAncestor() && (!entity.ancestor.system || rendersystem)>  
        "${JavaName(entity)}" -> "${JavaName(entity.ancestor)}"
    </#if>
</#list>

/*one to many 'xref' foreign key relationships*/
        edge [
                arrowhead = "open"
                arrowsize = 0.6
        ]
 <#-- we don't render interface relationships as that seems to be confusing-->       
<#list entities as entity>
	<#if !entity.abstract && (!entity.system || rendersystem)>
		<#list entity.implementedFields as f>
			<#if f.type=="xref" && (!f.xrefEntity.system || rendersystem)>
		"${JavaName(entity)}" -> "${JavaName(f.xrefEntity)}" [
			headlabel = "<#if f.nillable>0..</#if>1"
			taillabel = "*"
		]
			</#if>
		</#list>
	</#if>
</#list>
        
        
/*many to many 'mref' foreign key relationships*/
        edge [
             arrowtail = "open"
             color = "black"
             headlabel = "*"
             taillabel = "*"
             arrowsize = 0.6
        ]
<#-- to check for duplicates -->
<#-- we don't render interface relationships as that seems to be confusing-->
<#assign mref_names = []>
<#list entities as entity>
	<#if !entity.abstract && (!entity.system || rendersystem)>
		<#list entity.implementedFields as f >
			<#if f.type=="mref" && (!f.xrefEntity.system || rendersystem)>	
		"${JavaName(entity)}" -> "${JavaName(f.xrefEntity)}"[
			]
					<#assign mref_names = mref_names + [f.getMrefName()]>
				</#if>	
		</#list>
	</#if>
</#list>
}