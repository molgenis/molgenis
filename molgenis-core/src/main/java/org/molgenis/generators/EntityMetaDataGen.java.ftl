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
	public  ${JavaName(entity)}MetaData()
	{
		super("${JavaName(entity)}");
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
		${name(f)}.setIdAttribute(${(entity.primaryKey.name == f.name)?string('true', 'false')});
		${name(f)}.setNillable(${f.nillable?string('true', 'false')});
		${name(f)}.setReadOnly(${f.readOnly?string('true', 'false')});
		<#if f.isXRef()>
		${f.xrefEntity.namespace?lower_case}.${JavaName(f.xrefEntity)}MetaData ${name(f)}Xref = new ${f.xrefEntity.namespace?lower_case}.${JavaName(f.xrefEntity)}MetaData();
		${name(f)}.setRefEntity(${name(f)}Xref);
		${name(f)}.setRefAttribute(${name(f)}Xref.getAttribute("${f.xrefFieldName}"));
		</#if>
		addAttributeMetaData(${name(f)});	
		 
</#list>
	}
}