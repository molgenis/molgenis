<#include "GeneratorHelper.ftl">
<#function csv items>
	<#local result = "">
	<#list items as item>
		<#if item_index != 0>
			<#local result =  result + ",">
		</#if>
		<#if item?is_hash>
			<#local result = result + "\""+item.name+"\"">
		<#else>
			<#local result = result + "\""+item+"\"">
		</#if>
	</#list>
	<#return result>
</#function>
<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* File:        ${model.getName()}/model/JDBCDatabase
 * Copyright:   Inventory 2000-${year?c}, GBIC 2002-${year?c}, all rights reserved
 * Date:        ${date}
 * 
 * generator:   ${generator} ${version}
 *
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */
package ${package};

import java.util.Arrays;
import java.util.Vector;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Model;
import org.molgenis.model.elements.Field;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.model.MolgenisModelValidator;
import org.molgenis.MolgenisOptions;

/**
 * This class is an in memory representation of the contents of your *_db.xml file
 * Utility of this class is to allow for dynamic querying and/or user interfacing
 * for example within a query tool or a security module.
 */
public class JDBCMetaDatabase extends Model
{
	public JDBCMetaDatabase() throws DatabaseException
	{
		super("${model.name}");
		<#if entities?size gt 0>
		try
		{
			<#list entities as entity>
				<#if !entity.association>
			//${entity.name}
			Entity ${name(entity)}_entity = new Entity("${entity.name}",this.getDatabase());
			${name(entity)}_entity.setSystem(<#if entity.isSystem()>true<#else>false</#if>);
			${name(entity)}_entity.setAbstract(<#if entity.isAbstract()>true<#else>false</#if>);
					<#if entity.hasImplements()>
			${name(entity)}_entity.setImplements(new String[]{${csv(entity.implements)}});
					</#if>
					<#if entity.hasAncestor()>
			${name(entity)}_entity.setParents(new String[]{"${entity.getAncestor().name}"});
					</#if>
					<#if entity.getDescription()?exists || entity.getDescription() != ''>
			${name(entity)}_entity.setDescription("${entity.description?j_string}");
					</#if>						
					<#if entity.xrefLabels?exists>
			${name(entity)}_entity.setXrefLabels(Arrays.asList(new String[]{${csv(entity.xrefLabels)}}));
					</#if>			
					<#list entity.getFields() as field>
						<#if field.name != typefield()>
			Field ${name(entity)}_${name(field)}_field = new Field(${name(entity)}_entity, "${field.name}", MolgenisFieldTypes.getType("${field.type}"));
							<#if field.auto>
			${name(entity)}_${name(field)}_field.setAuto(true);
							</#if>
							<#if field.type == 'enum'>
			Vector<String> ${name(entity)}_${name(field)}_field_enumoptions = new Vector<String>();
								<#list field.enumOptions as enumoption>
			${name(entity)}_${name(field)}_field_enumoptions.add("${enumoption}");
								</#list>
			${name(entity)}_${name(field)}_field.setEnumOptions(${name(entity)}_${name(field)}_field_enumoptions);
							</#if>
							<#if field.defaultValue != ''>
			${name(entity)}_${name(field)}_field.setDevaultValue("${field.defaultValue}");
							</#if>
							<#if field.description != ''>
			${name(entity)}_${name(field)}_field.setDescription("${field.description?j_string}");
							</#if>				
			${name(entity)}_${name(field)}_field.setNillable(<#if field.isNillable() == true>true<#else>false</#if>);
							<#if field.type == "xref" || field.type == "mref">
			${name(entity)}_${name(field)}_field.setXRefVariables("${field.xrefEntityName}", "${field.xrefFieldName}",Arrays.asList(new String[]{${csv(field.xrefLabelNames)}}));
							</#if>
			${name(entity)}_entity.addField(${name(entity)}_${name(field)}_field);
						</#if>
					</#list>
					<#list entity.keys as key>
			${name(entity)}_entity.addKey(Arrays.asList(new String[]{${csv(key.fields)}}),<#if key.isSubclass()>true<#else>false</#if>,"");
					</#list>
			
				</#if>
			</#list>
			
			//disabled validation, this means above must be perfect!
			//new MolgenisModelValidator();
			//MolgenisModelValidator.validate(this, new MolgenisOptions());

		} catch (MolgenisModelException e)
		{
			throw new DatabaseException(e);
		}
		</#if>
	}
}