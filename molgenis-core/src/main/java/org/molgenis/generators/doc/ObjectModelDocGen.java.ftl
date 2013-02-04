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
<#macro render_entity entity>
<h3><a href="${entity.name}">${entity.name}</a><#if entity.isAbstract()> (interface).</#if></h3>
<p style="margin-top: 0px; margin-bottom: 0px;">
<#if entity.hasAncestor()><span style="font-style:italic"> extends ${entity.getAncestor().getName()}</span><br></#if>
<#if entity.hasImplements()><span style="font-style:italic"> implements ${csv(entity.getImplements())}</span><br></#if>
</p>
<p>	
${entity.description}
</p>
<#assign associations = false>
<#assign attributes = false>
<#assign inherited_attributes = false>
<#assign unique_constraints = false>
<#assign color = "style=\"color:black;\"">
<#list entity.fields as field>
<#if field.type == "xref" || field.type=="mref">
<#assign associations = true>
<#elseif !field.hidden && !field.system>
<#assign attributes = true>
<#assign color = "style=\"color:#333333;\"">
</#if>
</#list>
<#list entity.inheritedFields as field>
<#if !field.system><#assign inherited_attributes = true></#if>
</#list>
<#list entity.keys as key>
<#assign unique_constraints= true>
</#list>
<#if inherited_attributes>
<p style="text-decoration:underline">
Inherited attributes:
</p>
<br>
<#list entity.inheritedFields as field><#if !field.system> 
${field.name}, 
</#if></#list>
</#if>
<#if attributes == true>
<p style="text-decoration:underline">
Attributes:
</p>
<table>	
<#list entity.fields as field>
<#if !field.system && field.type != "xref" && field.type != "mref">
<#assign color = "1"/>
<#if field.entity.name != entity.name><#assign color = "style=\"color:#333333;\""/></#if>
<tr>
<td ${color} >
<span style="font-weight:bold">${field.name}</span>: ${field.type} 
(<#if field.nillable == false>required<#else>optional</#if>)
</td>
</tr>
<#if field.name != field.description><tr><td style="padding-left: 50px;">${field.description}
<#if field.type == "enum">
<br>enum_options: ${csv( field.getEnumOptions() )}
</#if></td></tr></#if>
</#if>
</#list>
</table>
</#if>
<#if associations==true>
<p style="text-decoration:underline">
Associations:
<table>	
<#list entity.fields as field><#if !field.system && (field.type == "xref" || field.type == "mref")>
<#if field.entity.name != entity.name><#assign color = "style=\"color:#333333;\""/></#if>
<tr>
<td ${color}>
<p style="text-decoration:underline">${field.name}: <#if field.type=="xref">
${field.xrefEntity.name} (<#if field.nillable>0<#else>1</#if>..1)
<#elseif field.type=="mref">
${field.xrefEntity.name} (<#if field.nillable>0<#else>1</#if>..n)</#if>
</td>
</tr>
<#if field.name != field.description><tr><td style="padding-left: 50px;">${field.description}</td></tr></#if>
</#if>
</#list>
</table>
</#if>
<#if unique_constraints>
<p style="text-decoration:underline">
Constraints:
</p>
<table>	
<#list entity.keys as key>
<tr>
<td ${color} style="font-weight:bold">
unique(${csv(key.fields)}): 
</td>
</tr>
<tr><td style="padding-left: 50px;">
<#if key.description?exists>${key.description}<#elseif key.fields?size &gt; 1>The combination of fields ${csv(key.fields)} is unique within an ${entity.name}<#else>Field ${csv(key.fields)} is unique within an ${entity.name}</#if>.
</td></tr>
</#list>
</table>
</#if>
</#macro>
<html>

<head>
	<title>${model.label} Documentation</title>
	
	<meta name="keywords"			content="">
	<meta name="description"		content="">
	<meta http-equiv="Contect-Type"	content="text/html; charset-UTF-8">
	
	<style type="text/css">
		body
		{
			background:		#ffffff;
			color:			#000000;
			font-family:	arial, sans-serif;
			font-size:		10pt;
			margin-left: 50px;
			margin-right: 50px;
		}
		p {
			max-width: 1000px;
			text-align: left;
		}
		h1{
			margin-top 20px;
		}
		h3{
			text-decoration: underline;
			margin-bottom: 0px;
			padding-bottom: 0px;
		}
		td
		{
			font-family:	arial, sans-serif;
			font-size:		10pt;
			vertical-align: top;
		}
		.tablehead
		{
			background:		#888888;
			color:			#ffffff;
			text-align:		left;
			font-family:	arial, sans-serif;
			font-size:		10pt;
			font-weight:	bold;
		}
		th
		{
			font-family:	arial, sans-serif;
			font-size:		10pt;
			font-weight:	bold;
		}
	</style>
</head>



<body>
<h1><a href="#_top_of_page">${model.label}</a> documentation.</h1>
<#if model.version?exists><p>${model.version}</p></#if>
<#if model.getDBDescription()?exists>${model.getDBDescription()}</#if>


<h2>Table of contents</h2>
<table style="width:100%"><tr>
<#list modules as module>
<td>
<a href="#${module.name}_package">${module.name}</a> <span style="font-weight:bold">package:
<ul>
<#list module.entities as entity><#if !entity.association>
<li><a href="#${entity.name}">${entity.name}</a></li>
</#if></#list>
</ul>
</td>
</#list>
<#if model.entities?size &gt; 0>
<td>
<a href="#${model.name}_package"><span style="font-weight:bold">All entities in ${model.name}</span></a>:
<ul>
<#list model.entities as entity><#if !entity.association>
<li><a href="#${entity.name}">${entity.name}</a></li>
</#if></#list>
</ul>
</td>
</#if>
</tr></table>
<br>
<a href="#__figure_of_complete_schema">Supplementary figure: complete data model</a>


<#list modules as module>
<h1><a href="#${module.name}_package">${module.name} package</a></h1>
<#if module.description?exists><p>${module.description}</p></#if>
<img src="objectmodel-uml-diagram-${name(module)}.dot.png" style="border: solid thin black;">
<a href="objectmodel-uml-diagram-${name(module)}.dot.png" target="_blank">show fullscreen</a>
<#list module.entities as entity><#if !entity.association>
<@render_entity entity/>
</#if></#list>
<br>
<br>
</#list>

<#if model.entities?size &gt; 0>
<h1><a href="${model.name}_package">${model.name} package</a></h1>
<#list model.entities as entity><#if !entity.association>
<@render_entity entity/>
</#if></#list>
<br>
<br>
</#if>

<h1>Supplementary figure: complete data model</h1>
<a href="__figure_of_complete_schema"></a><br>
<img src="objectmodel-uml-diagram.dot.png" style="border: solid double black;">
<br>
<a href="#_top_of_page">go to top</a>

<div align="center">Documentation generated on ${date} by MOLGENIS <a href="http://www.molgenis.org">http://www.molgenis.org</a></div>
</body></html>



