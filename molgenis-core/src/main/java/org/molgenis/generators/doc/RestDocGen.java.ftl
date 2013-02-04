<#--generates documentation of the REST API-->
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
<html>

<head>
	<title>REST API: ${model.name}</title>
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
<h1>REST web service programming interface <a name="_top_of_page">"${model.name}"</a></h1>
<p>
This documents the REST scriptable web service interface that can be used for 'AJAX' type web pages and/or use of this MOLGENIS system from scripting languages.
A typical example is javascript:


</p>
<h2>Entities:</h2>
<ol>
<#list model.entities as entity>
<li><a href="#${entity.name}">${entity.name}</a>
</#list>
</ol>
<#list model.entities as entity>
<h2>Table: <a name="${entity.name}">${entity.name}</a><#if entity.isAbstract()> (interface).</#if> 
</h2>
<p>${entity.description}</p>
<table style="width:100%; cellspacing:0px; border:1px #000000">
	<!-- table header -->
	<tr><th class="tablehead" colspan="6">${entity.name}<#if entity.hasAncestor()> extends ${entity.getAncestor().getName()}</#if>
<#if entity.hasImplements()> implements ${csv(entity.getImplements())}</#if></th></tr>	
	<!-- column headers -->
	<tr>
		<th>attribute</th>
		<th>type</th>
		<th>NULL?</th>
		<th>AUTO?</th>
		<th>constraints</th>
		<th>description</th>
	</tr>
	
	<!-- all the fields -->
<#list entity.fields as field>
	<tr>
		<td style="width:150px">${field.name}</td>
		<td style="width:50px">${field.type}</td>
		<td style="width:20px"><#if field.isNillable()>Y<#else>&nbsp;</#if></td>
		<td style="width:20px"><#if field.isAuto()>Y<#else>&nbsp;</#if></td>
		<td width="">
<#if field.type == "enum">
		ENUM options: ${csv( field.getEnumOptions() )}
<#elseif field.type=="xref">
		References(${field.xrefEntityName})
<#elseif field.type=="mref">
		References(${field.xrefEntityName}) via linktable.	
<#else>
		&nbsp;
</#if>				
		</td>
		<td>${field.description}</td>
	</tr>
</#list>
	
	<!-- all the uniques -->
<#assign index = 0>
<#list entity.keys as key>
	<tr>
		<td colspan="5"><#if index == 0>Primary key<#else>Secondary key</#if>(${csv(key.fields)})</td>
		<#assign index = index + 1>
		<td>&nbsp;</td>
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
<a href="#_top_of_page">go to top</a>
<p />
</#list>

</body></html>

