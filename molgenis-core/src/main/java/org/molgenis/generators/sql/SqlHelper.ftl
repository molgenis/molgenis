<#function csv items>
	<#local result = "">
	<#list items as item>
		<#if item_index != 0>
			<#local result = result + ",">
		</#if>
		<#if item?is_hash>
			<#local result = result + SqlName(item)>
		<#else>
			<#local result = result + "'"+item+"'">
		</#if>
	</#list>
	<#return result>
</#function>
<#function enum items>
	<#local result = "">
	<#list items as item>
		<#if item_index != 0>
			<#local result = result + ",">
		</#if>
		<#if item?is_hash>
			<#local result = result + "'" + item.name+ "'">
		<#else>
			<#local result = result + "'"+item+"'">
		</#if>
	</#list>
	<#return result>
</#function>
<#function psql_type model field>
	<#switch field.type>
		<#case "bool">
			<#return "BOOL">
		<#case "date">
			<#return "DATE">
		<#case "datetime">
			<#return "TIMESTAMP">
		<#case "decimal">
			<#return "NUMERIC">
		<#case "enum">
			<#--in Postgresql, enums have to be defined as a custom type-->
			<#return "ENUM_"+name(field.entity)+"_"+name(field)>
		<#case "file">
			<#return "VARCHAR(1024)">
		<#case "int">
			<#return "INT">
		<#case "long">
			<#return "LONG">			
		<#case "text">
			<#return "TEXT">
		<#case "user">
			<#return "VARCHAR(32)">
		<#case "string">
			<#return "VARCHAR("+field.getVarCharLength()+")">				
		<#case "xref">
			<#return psql_type(model, field.getXrefField())>
		<#case "mref">
			<#return psql_type(model, field.getXrefField())>			
		<#case "hyperlink">
			<#return "TEXT">
		<#case "ontology">
			<#return "INT"><#-- id of ontologyTerm"-->
		<#default>
			<#return "UNKNOWN '"+field.type + "'">
	</#switch>
</#function>
<#function oracle_type model field>
	<#return helper.getOracleType(model, field)/>
</#function>
<#function mysql_type model field>
	<#return helper.getMysqlType(model, field)/>
</#function>