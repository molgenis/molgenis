<#function csv items>
	<#local result = "">
	<#list items as item>
		<#if item_index != 0>
			<#local result = result + ",">
		</#if>
		<#if item?is_hash>
			<#local result = result + item.name>
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
<#function hsql_type model field>
	<#switch field.type>
		<#case "hyperlink">
			<#return "VARCHAR(256)">
		<#case "text">
			<#return "VARCHAR(1024)">
		<#case "long">
			<#return "BIGINT">			
		<#default>
			<#return helper.getHsqlType(field)/>
	</#switch>
</#function>