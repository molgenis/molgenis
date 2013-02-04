<#include "GeneratorHelper.ftl">
<#function csv items>
	<#local result = "">
	<#list items as item>
		<#if item_index != 0>
			<#local result = result + ", ">
		</#if>
		<#if item?is_hash>
			<#local result = result + item.name?lower_case>
		<#else>
			<#local result = result + item?lower_case>
		</#if>
	</#list>
	<#return result>
</#function>

<#macro render_field field>
	<#if field.type == "xref" || field.type =="mref">
	<tr>
		<td style="width:150px"><#list field.xrefLabelNames as label>${field.name?lower_case}_${label}<br></#list></td>
		<td style="width:50px">${field.type}</td>
		<td style="width:20px"><#if field.isAuto()>&nbsp;<#elseif !field.isNillable() && (!field.defaultValue?exists || field.defaultValue == "")>YES<#else>&nbsp;</#if></td>
		<td style="width:50px">&nbsp;</td>
		<td><#if field.description != "">${field.description}.</#if><#if field.xrefLabelNames?size &gt; 1>This ${field.type} 
		uses the combination of {<#list field.xrefLabelNames as label><#if label_index &gt; 0>,</#if>${field.name?lower_case}_${label}</#list>} to find related elements in <a href="#${name(field.xrefEntity)}_entity">${name(field.xrefEntity)}.txt</a> based on unique columns {${csv(field.xrefLabelNames)?lower_case}}.<#else>
		This ${field.type} uses {${field.name?lower_case}_${csv(field.xrefLabelNames)?lower_case}} to find related elements in file <a href="#${name(field.xrefEntity)}_entity">${name(field.xrefEntity)}.txt</a> based on unique column {${csv(field.xrefLabelNames)?lower_case}}.
		</#if> 
		<#if field.type="mref">. More than one reference can be added separated by '|', for example: ref1|ref2|ref3.</#if></td>
	</tr>	
	<#else>
	<tr>
		<td style="width:150px">${field.name?lower_case}</td>
		<td style="width:50px">${field.type}</td>
		<td style="width:20px"><#if field.isAuto()>&nbsp;<#elseif !field.isNillable() && ( !field.defaultValue?exists || field.defaultValue == "" )>YES<#else>&nbsp;</#if></td>
		<td style="width:50px"><#if field.isAuto()><#if field.type="int">n+1<#else>today</#if><#elseif field.defaultValue?exists && field.defaultValue != "">${field.defaultValue}<#else>&nbsp;</#if></td>
		<td><#if field.description != "">${field.description}<#if !field.description?trim?ends_with('.')>.</#if><br></#if></td>
	</tr>
	</#if>
</#macro>


<#macro render_entity entity>
<h3 id="${name(entity)}_entity">File: ${entity.name?lower_case}.txt</h3> 
<#if entity.description != "" ><p>Contents:<br> ${entity.description}<#if !entity.description?trim?ends_with('.')>.</#if></p></#if>
<p>Structure:<br>
<table style="width:100%; cellspacing:0px; border:1px #000000;">
	<!-- table header -->
	<tr>
		<th>column name</th>
		<th>type</th>
		<th>required?</th>
		<th>auto/default</th>
		<th>description</th>
	</tr>
	
	<!-- all required fields -->
<#list entity.allFields as field><#if !field.hidden && field.name != typefield()>
	<@render_field field/>
</#if></#list>
	<!-- all optional fields -->
<#--list entity.allFields as field><#if field.name != typefield() && (field.isNillable() || field.isAuto() || field.defaultValue != "") >
	<@render_field field/>
</#if></#list-->
	<!-- all the uniques -->
<#assign index = 0>
<#list entity.allKeys as key><#if key_index &gt; 0>
	<#if key.fields?size &gt; 1>
<tr><td colspan="5">Contraint: values in the combined columns (${csv(key.fields)}) should be unique.</td></tr>
	<#else>
<tr><td colspan="5">Constraint: values in column ${csv(key.fields)} should unique.</td></tr>
	</#if>
</#if></#list>
</table>
</p>

<#assign example = helper.renderExampleData(entity.name + ".txt")>
<#if example != ""><p>Example:<br>
<pre>
${example}
</pre>
</p></#if>
</#macro>	

<html>

<head>
	<title>${model.name} TAB exchange format reference documentation</title>
	
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
<h1>${model.label} file format reference</h1>
<p>
This is documentation on the data exchange format for the '${model.label}' system. <#if model.getDBDescription()?exists>${model.getDBDescription()}</#if>
</p>
<p>To ease data exchange this system comes with a simple 'tab separated values' file format. 
In such text files the data is formatted in tables with the columns separated using tabs, colons, or semi-colons.
Advantage is that these files can be easily created and parsed using common spreadsheet tools like Excel. 
An example of such tab delimited file is shown below:
<pre>
name	description	date
Experiment1	This is my first experiment	2010-01-19
Experiment2	This is my second experiment	2010-01-20
</pre>
This document describes what file types and columns are defined for the '${model.label}' system. 
Data in this format can be uploaded to the database via the user interface using the 'File' menu). 
Alternatively, a whole directory of such files can be loaded in batch using the CsvImport program. 
The following files are currently recognized by this program (grouped by topic):
</p>
<!-- per module -->
<ul>
<#list model.modules as module>
<li><b><#if module.label??>${module.label}<#else>${module.name}</#if></b> files:
<ul>
<#list module.entities as entity><#if !entity.abstract && !entity.association>
<li><a href="#${name(entity)}_entity">${entity.name?lower_case}.txt</a>
</#if></#list>
</ul>
</li>
</#list>
<!-- outside module -->
<#if model.rootEntities?size gt 0>
<li><b>${model.label}</b> files:
<ul>
<#list model.rootEntities as entity><#if !entity.abstract && !entity.association>
<li><a href="#${name(entity)}_entity">${entity.name?lower_case}.txt</a>
</#if></#list>
</ul>
</li>
</#if>
</ul>
Below, the columns for each of these file types are detailed as well as example data shown (if available).


<!-- entities inside modules -->
<#list model.modules as module>
<h2><#if module.label??>${module.label}<#else>${module.name}</#if> file types</h2>
<#if module.description?exists><p>${module.description}</p></#if>
<#list module.entities as entity><#if !entity.abstract && !entity.association>
<@render_entity entity/>
</#if></#list>
</#list>

<!-- entities outside modules -->
<#if model.rootEntities?size gt 0>
<h2>${model.name} file types</h2>
<#list model.rootEntities as entity><#if !entity.abstract && !entity.association>
<@render_entity entity/>
</#if></#list>
</#if>

<h1>Appendix: documentation of the mref tables</h1>
<#if model.entities?size &gt; 0>
<h2>${model.name} file types</h2>
<#list model.entities as entity><#if entity.association>
<@render_entity entity/>
</#if></#list>
</#if>

</body></html>

