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
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;

public class ${JavaName(entity)}MetaData extends DefaultEntityMetaData
{
	public ${JavaName(entity)}MetaData()
	{
		super("${JavaName(entity)}", ${JavaName(entity)}.class);
		setLabel("${entity.label}");
		setDescription("${entity.description?j_string}");
		
<#list entity.allFields as f>
		DefaultAttributeMetaData ${name(f)} = new DefaultAttributeMetaData("${f.name}", ${f.type.enumType});
		<#if f.defaultValue?has_content>
			<#if f.type.enumType == 'STRING' || f.type.enumType == 'TEXT' || f.type.enumType == 'CATEGORICAL' || f.type.enumType == 'EMAIL' ||
				f.type.enumType == 'ENUM' || f.type.enumType == 'FILE' || f.type.enumType == 'HTML' || f.type.enumType == 'HYPERLINK' ||
				f.type.enumType == 'IMAGE'>
		${name(f)}.setDefaultValue("${f.defaultValue}");
			<#else>
		${name(f)}.setDefaultValue(${f.defaultValue});
			</#if>
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
		Long min = <#if f.minRange??>${f.minRange?c}l<#else>null</#if>;
		Long max = <#if f.maxRange??>${f.maxRange?c}l<#else>null</#if>;
		${name(f)}.setRange(new org.molgenis.data.Range(min, max));
		</#if>
		addAttributeMetaData(${name(f)});	
</#list>
		
	}
	
	
}