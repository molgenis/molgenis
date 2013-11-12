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
import org.molgenis.data.support.EntityMetaDataCache;

public class ${JavaName(entity)}MetaData extends DefaultEntityMetaData
{
	public  ${JavaName(entity)}MetaData()
	{
		super("${JavaName(entity)}");
		EntityMetaDataCache.add(this);
		
		setLabel("${entity.label}");
		
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
		<#if f.description??>
		${name(f)}.setDescription("${f.description!}");
		</#if>
		<#if entity.primaryKey.name == f.name >
		${name(f)}.setIdAttribute(true);
		${name(f)}.setUnique(true);
		<#else>
			<#list entity.keys as k>
				<#if k.fields?size == 1 && k.fields?first.name == f.name>
		${name(f)}.setUnique(true);
				</#if>
			</#list>
		</#if>
		${name(f)}.setNillable(${f.nillable?string('true', 'false')});
		${name(f)}.setReadOnly(${f.readOnly?string('true', 'false')});
		<#if f.isXRef()>
		${name(f)}.setRefEntityName("${f.xrefEntity.name}");
		</#if>
		addAttributeMetaData(${name(f)});
			
</#list>
		
	}
	
	
}