<#include "GeneratorHelper.ftl">
<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* File:        ${file}
 * Generator:   ${generator} ${version}
 *
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */
package ${package};

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.*;

import org.springframework.stereotype.Component;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;

/**
 * <#if JavaName(entity) == "RuntimeProperty">@deprecated replaced by setting classes that derive from {@link org.molgenis.data.settings.DefaultSettingsEntity}</#if>
 */
<#if JavaName(entity) == "RuntimeProperty">@Deprecated</#if>
@Component
public class ${JavaName(entity)}MetaData extends DefaultEntityMetaData
{
	public ${JavaName(entity)}MetaData()
	{
		super("${JavaName(entity)}", ${JavaName(entity)}.class);
		setLabel("${entity.label}");
		setDescription("${entity.description?j_string}");
		setBackend("JPA");
		
<#list entity.allFields as f>
		DefaultAttributeMetaData ${name(f)} = new DefaultAttributeMetaData("${f.name}", ${f.type.enumType});
		<#if f.defaultValue?has_content>
			${name(f)}.setDefaultValue("${f.defaultValue}");
		</#if>
		<#if f.label??>
		${name(f)}.setLabel("${f.label!}");
		</#if>
		<#if f.description??>
		${name(f)}.setDescription("${f.description!}");
		</#if>
		${name(f)}.setIdAttribute(${(entity.primaryKey.name == f.name)?string('true', 'false')});
		${name(f)}.setNillable(${f.nillable?string('true', 'false')});
		${name(f)}.setReadOnly(${f.readOnly?string('true', 'false')});
		${name(f)}.setUnique(${f.unique?string('true', 'false')});
		${name(f)}.setAuto(${f.auto?string('true', 'false')});
		<#if f.isEnum()>
        ${name(f)}.setEnumOptions(java.util.Arrays.asList(<#list f.enumOptions as option>"${option}"<#if option_has_next>, </#if></#list>));
        </#if>
        <#if f.isXRef()>
			<#if f.xrefEntity.name == entity.name>
		${name(f)}.setRefEntity(this);	
			<#else>
		${name(f)}.setRefEntity(new ${f.xrefEntity.namespace?lower_case}.${JavaName(f.xrefEntity)}MetaData());
			</#if>
		</#if>
		<#list entity.getXrefLabels() as xrefLabel>
			<#if xrefLabel == f.name>
		${name(f)}.setLabelAttribute(true);
			</#if>
		</#list>
		<#list entity.getXrefLookupFields() as xrefLookup>
			<#if xrefLookup == f.name>
		${name(f)}.setLookupAttribute(true);
			</#if>
		</#list>	
		<#if f.hidden || f.system>
		${name(f)}.setVisible(false);
		</#if>
		${name(f)}.setAggregateable(${f.aggregateable?string('true', 'false')});
		<#if f.minRange?? || f.maxRange??>
		Long ${name(f)}Min = <#if f.minRange??>${f.minRange?c}l<#else>null</#if>;
		Long ${name(f)}Max = <#if f.maxRange??>${f.maxRange?c}l<#else>null</#if>;
		${name(f)}.setRange(new org.molgenis.data.Range(${name(f)}Min, ${name(f)}Max));
		</#if>
		addAttributeMetaData(${name(f)});	
</#list>
		
	}
	
	
}