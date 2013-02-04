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
		orientation = "portrait"

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
<#list entities as entity><#if entities?seq_contains(entity) && !entity.system>
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
                
                        label = "{${JavaName(entity)}}"
                ]

</#if></#list>
/*entities inside modules*/
<#assign color = 0>
<#list model.modules as m>
<#assign color = color + 1>
/*        subgraph cluster_${m_index} {
        		rankdir = "TB"
        		pagedir = "TB"
                label = "${name(m)}"
                labelloc = bottom
                colorscheme = pastel19
                fillcolor = ${color}
                style="filled"*/

<#list m.entities as entity><#if entities?seq_contains(entity)>
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
                
                        label = "{${JavaName(entity)}}"
                ]
</#if></#list>
/*        }  */
</#list>

/*inheritance relationships*/
        edge [
                arrowhead = "empty"
                color = "black"
        ]
<#list entities as entity>
	<#if entities?seq_contains(entity) && entity.hasAncestor()>  
        "${JavaName(entity)}" -> "${JavaName(entity.ancestor)}"
    </#if>
</#list>

/*interface relationships*/
        edge [
                color = "#808080"
        ]
<#list entities as entity>
    <#if entity.hasImplements()>
    	<#list entity.implements as interface><#if entities?seq_contains(interface)>
    	"${JavaName(entity)}" -> "${JavaName(interface)}"
    	</#if></#list>
    </#if>
</#list>

/*one to many 'xref' foreign key relationships*/
        edge [
                arrowhead = "open"
                arrowsize = 0.6
                color = "black"
        ]
<#list entities as entity>
	<#if !entity.system>
		<#list entity.fields as f>
			<#if f.type=="xref">
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
             constraint = false
        ]
<#-- to check for duplicates -->
<#assign mref_names = []>
<#list entities as entity>
	<#if !entity.system>
		<#list entity.fields as f >
			<#if f.type=="mref">	
		"${JavaName(f.xrefEntity)}" -> "${JavaName(entity)}"[
			]
					<#assign mref_names = mref_names + [f.getMrefName()]>
				</#if>	
		</#list>
	</#if>
</#list>
}