<#--move generator superclass using http://freemarker.sourceforge.net/docs/pgui_datamodel_method.html -->
<#--classname is always first to uppercase-->
<#function JavaName value>
	<#if value?is_hash>
		<#return helper.getJavaName(value.getName())>
	<#else>
		<#return helper.getJavaName(value)>
	</#if>
</#function>
<#function SqlName value>
	<#if value?is_hash>
		<#return value.getName()>
	<#else>
		<#return value>
	</#if>
</#function>
<#function RName value>
	<#if value?is_hash> 
		<#return helper.toLower(value.getName())>
	<#else>
		<#return helper.toLower(value)>
	</#if>
</#function>

<#function name value="VALUE WAS NULL">
	<#--<#return helper.toLower(value.getName())>-->
	<#if value?is_hash>
		<#return helper.firstToLower(value.getName())>
	<#else>
		<#return helper.firstToLower(value)>
	</#if>
</#function>
<#function isPrimaryKey field entity>
	<#return helper.isPrimaryKey(field,entity)>
</#function>
<#function Name value>
	<#if value?is_hash>
		<#return helper.firstToUpper(value.getName())>
	<#else>
		<#return helper.firstToUpper(value)>
	</#if>
</#function>
<#function PluralName value>
	<#if value?is_hash>
		<#return helper.firstToUpper(helper.pluralOf(value.getName()))>
	<#else>
		<#return helper.firstToUpper(helper.pluralOf(value))>
	</#if>
</#function>
<#function pluralName value>
	<#if value?is_hash>
		<#return helper.pluralOf(value.getName())?lower_case>
	<#else>
		<#return helper.pluralOf(value)?lower_case>
	</#if>
</#function>
<#function type field>
	<#return helper.getType(field)>
</#function>
<#function JavaType field>
	<#return helper.getType(field)>
</#function>
<#function ptype field size>
	<#return helper.getPType(model,field,size)>
</#function>
<#function arraytype field size="">
	<#return helper.getPType(model,field,size)>
</#function>
<#function gettertype field>
	<#return helper.getGetType(model,field)>
</#function>
<#function gettype field>
	<#return helper.getGetType(field)>
</#function>
<#function settertype field>
	<#return helper.getSetType(model,field)>
</#function>
<#function default field>
	<#return helper.getDefault(model,field)>
</#function>
<#function importVector entity>
	<#foreach field in entity.getFields()>
		<#local type_label = field.getType().toString()>	
		<#if type_label == "user" || type_label == "xref" || type_label == "mref" || type_label == "enum">
			<#return "import java.util.Vector;">
		</#if>
	</#foreach>
	<#return "">
</#function>
<#function importList entity>
	<#foreach field in entity.getFields()>
		<#local type_label = field.getType().toString()>	
		<#if type_label == "user" || type_label == "xref" || type_label == "mref" || type_label == "enum">
			<#return "import java.util.ArrayList;\nimport java.util.List;">
		</#if>
	</#foreach>
	<#return "">
</#function>
<#function importFile entity>
	<#foreach field in entity.getFields()>
		<#local type_label = field.getType().toString()>	
		<#if type_label == "file">
			<#return "import java.io.File; import org.apache.commons.io.FileUtils;">
		</#if>
	</#foreach>
	<#return "">
</#function>
<#function hasFiles entity>
	<#foreach field in entity.getFields()>
		<#local type_label = field.getType().toString()>	
		<#if type_label == "file" || type_label="image">
			<#return true>
		</#if>
	</#foreach>
	<#return false>
</#function>
<#function importValueLabel entity>
	<#foreach field in entity.getFields()>
		<#local type_label = field.getType().toString()>
		<#if type_label == "user" || type_label == "xref" || type_label == "mref" || type_label == "enum">
			<#return "import org.molgenis.framework.util.ValueLabel;\nimport java.util.ArrayList;">
		</#if>
	</#foreach>
	<#return "">
</#function>
<#function addFieldsIncKey entity>
	<#return helper.getAddFields(entity, true)>
</#function>
<#function addFields entity>
	<#return helper.getAddFields(entity)>
</#function>
<#function updateFields entity>
	<#return helper.getUpdateFields(entity)>
</#function>
<#function keyFields entity>
	<#return helper.getKeyFields(entity)>
</#function>
<#function xrefField model entity>
	<#return helper.getXrefField(model,entity)>
</#function>
<#function subclasses entity>
	<#return helper.getSubclasses(entity,model)>
</#function>
	<#function superclasses entity>
	<#return helper.getSuperclasses(entity,model)>
</#function>
<#function dbFields entity type="">
	<#return helper.getDbFields(entity,type)>
</#function>
<#function allFields entity type="">
	<#return helper.getAllFields(entity,type)>
</#function>
<#function viewFields entity type="">
	<#return helper.getViewFields(entity,type)>
</#function>
<#function imports model entity subpackage="" suffix="">
	<#return helper.getImports(model,entity,subpackage,suffix)>
</#function>
<#function PkeyName entity>
        <#return Name(entity.getPrimaryKey())>
</#function>
<#function pkey entity>
	<#return entity.getPrimaryKey()>
</#function>
<#function pkeyname entity>
        <#return name(entity.getPrimaryKey())>
</#function>
<#function pkeyField entity>
	<#foreach field in entity.allFields>
		<#if isPrimaryKey(field, entity)>
			<#return field/>
		</#if>
	</#foreach>
	<#return null/>
</#function>
<#function pkeyJavaType entity>
	<#foreach field in entity.allFields>
		<#if isPrimaryKey(field, entity)>
			<#return helper.getType(field)>
		</#if>
	</#foreach>
	<#return "KEY UNKNOWN FOR "+Name(entity)>
</#function>
<#-- returns a Vector<Unique> -->
<#function skeys entity>
	<#return helper.getSecondaryKeys(entity)>
</#function>
<#-- returns a Vector<Field> for the list of uniques. If two skeys share a Field, it is only included once -->
<#function key_fields list_of_uniques>
	<#return helper.getKeyFields(list_of_uniques)>
</#function>
<#function skey_fields entity>
	<#return helper.getSecondaryKeyFields(entity)>
</#function>

<#function escapeXml string>
	<#return helper.escapeXml(string)>
</#function>

<#-- Specific for Method -->
<#function method_returntype method>
	<#return method.getReturnType().getName()>
</#function>

<#function SqlType model field>
<#--fixme-->
	<#return helper.getMysqlType(model, field)/>
</#function>
<#function xsdType model field>
<#--fixme-->
	<#return helper.getXsdType(model, field)/>
</#function>
<#function csv items>
	<#local result = "">
	<#list items as item>
		<#if item_index != 0>
			<#local result =  result + ",">
		</#if>
		<#if item?is_hash>
			<#local result = result + item.name>
		<#else>
			<#local result = result +item>
		</#if>
	</#list>
	<#return result>
</#function>
<#function csvQuoted items>
	<#local result = "">
	<#list items as item>
		<#if item_index != 0>
			<#local result =  result + ",">
		</#if>
		<#if item?is_hash>
			<#local result = result + "\""+ item.name +"\"">
		<#else>
			<#local result = result + "\"" + item + "\"">
		</#if>
	</#list>
	<#return result>
</#function>
<#function csvQuotedEntity entity items>
	<#local result = "">
	<#list items as item>
		<#if item_index != 0>
			<#local result =  result + ",">
		</#if>
		<#if item?is_hash>
			<#local result = result + "\""+ entity +"_"+ item.name +"\"">
		<#else>
			<#local result = result + "\"" + entity +"_"+ item + "\"">
		</#if>
	</#list>
	<#return result>
</#function>
<#function typefield>
<#return helper.getTypeFieldName()/>
</#function>
<#function JavaAssignment field value>
	<#return helper.getJavaAssignment(field,value)/>
</#function>
<#function JavaTestValue field value>
	<#return helper.getJavaTestValue(field,value)/>
</#function>