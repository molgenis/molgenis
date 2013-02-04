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
<h3><a name="${entity.name}">${entity.name}</a><#if entity.isAbstract()> (interface).</#if> 
</h3>
<p>${entity.description}</p>
<table style="width:100%; cellspacing:0px; border:1px #000000">
	<!-- table header -->
	<tr><th class="tablehead" colspan="7">${entity.name}<#if entity.hasAncestor()><br><i> extends ${entity.getAncestor().getName()}</i></#if>
<#if entity.hasImplements()><br><i> implements ${csv(entity.getImplements())}</i></#if></th></tr>	
	<!-- column headers -->
	<tr>
		<th>field</th>
		<th>type</th>
		<th>description</th>
		<th>constraints</th>
	</tr>
	
	<!-- all the fields -->
<#list entity.allFields as field><#if !field.system>
	<#assign color = "style=\"color:#000000\""/>
	<#if field.entity.name != entity.name><#assign color = "style=\"color:#333333; font-style:italic;\""/></#if>
	<tr  >
		<td ${color}>${field.name}<#if field.entity.name != entity.name>*</#if></td>
		<td ${color}>${field.type}</td>
		<td ${color}><#if field.name != field.label>Label=${field.label}:</#if><#if field.name != field.description>${field.description}<#else>&nbsp;</#if></td>
		<td ${color}>
<#if field.type=="xref">
references(${field.xrefEntityName}.${field.xrefFieldName}),
<#elseif field.type=="mref">
references-many(${field.xrefEntityName}.${field.xrefFieldName}),</#if>
<#if field.isNillable()><#else>not null, </#if>
<#if field.isAuto()>auto, <#else></#if>
<#if field.type == "enum">
		enum_options: ${csv( field.getEnumOptions() )}
</#if>			
		</td>
	</tr>
</#if></#list>
	
	<!-- all the uniques -->
<#assign index = 0>
<#list entity.keys as key>
	<tr>
		<td colspan="2"><#if index == 0>primary </#if>key(${csv(key.fields)})</td>
		<td colspan="2"><#if key.description?exists>${key.description}<#else>&nbsp;</#if></td>
		<#assign index = index + 1>
	</tr>
</#list>
	<!-- all the indices -->
<#--list entity.indices as index>
	<tr>
		<td>Index</td>
		<td>${index.getName()}</td>
		<td colspan="2"><#list index.getFields() as field>${field.getName()}, </#list></td>
	</tr>
</#list-->
	
</table>
*inherited field<br>
<a href="#_top_of_page">go to top</a>
<p />
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
<h1><a href="_top_of_page">${model.label}</a> documentation.</h1>
<#if model.getDBDescription()?exists>${model.getDBDescription()}</#if>
<h2>Table of contents</h2>
<table><tr>
<#list modules as module>
<td>
<b><a href="#${module.name}_package">${module.name}</a></b> package:
<ul>
<#list module.entities as entity><#if !entity.association>
<li><a href="#${entity.name}">${entity.name}</a></li>
</#if></#list>
</ul>
</td>
</#list>
</tr></table>
<br>
<a href="#__figure_of_complete_schema">Supplementary figure: complete data model</a>


<#list modules as module>
<h1><a name="${module.name}_package">${module.name} package</a></h1>
<#if module.description?exists><p>${module.description}</p></#if>
<img src="entity-uml-diagram-${name(module)}.dot.png" style="border: solid thin black;">
<a href="entity-uml-diagram-${name(module)}.dot.png" target="_blank">show fullscreen</a>
<#list module.entities as entity><#if !entity.association>
<@render_entity entity/>
</#if></#list>
<br>
<br>
</#list>

<h1>Entities:</h1>
<#list entities as entity><#if !entity.association && !entity.module?exists>
<a href="#${entity.name}">${entity.name}</a><br>
</#if></#list>
<#list entities as entity><#if !entity.association>
<@render_entity entity/>
</#if></#list>

<h1>Supplementary figure: complete data model</h1>
<a href="__figure_of_complete_schema"></a><br>
<img src="entity-uml-diagram.dot.png" style="border: solid double black;">
<br>
<a href="#_top_of_page">go to top</a>

<div align="center">Documentation generated on ${date} by MOLGENIS <a href="http://www.molgenis.org">http://www.molgenis.org</a></div>
</body></html>



