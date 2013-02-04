<#--helper functions-->
<#include "GeneratorHelper.ftl">

<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* 
 * 
 * generator:   ${generator} ${version}
 *
 * 
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */
package ${package};

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.io.csv.CsvWriter;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.EntityTuple;

<#list entities as entity>
	<#if !entity.abstract && entity.association==false>
	import ${entity.namespace}.${JavaName(entity)};
	</#if>
</#list>

public class CsvEntityExporter
{
	private static final Logger logger = Logger.getLogger(CsvEntityExporter.class);
		
	/**
	 * Default export all using a target directory and a database to export
	 * @param directory
	 * @param db
	 * @throws Exception
	 */
	public void exportAll(File directory, Database db) throws Exception
	{
		exportAll(directory, db, true, new QueryRule[]{});
	}
	
	/**
	 * Export all using a set of QueryRules used for all entities if applicable to that entity
	 * @param directory
	 * @param db
	 * @param rules
	 * @throws Exception
	 */
	public void exportAll(File directory, Database db, QueryRule ... rules) throws Exception
	{
		exportAll(directory, db, true, rules);
	}
	
	/**
	 * Export all where a boolean skip autoid fields forces an ignore of the auto id field ("id")
	 * @param directory
	 * @param db
	 * @param skipAutoId
	 * @throws Exception
	 */
	public void exportAll(File directory, Database db, boolean skipAutoId) throws Exception
	{
		exportAll(directory, db, skipAutoId, new QueryRule[]{});
	}
	
	/**
	 * Export all with both a boolean skipAutoId and a set of QueryRules to specify both the skipping of auto id, and applying of a filter
	 * @param directory
	 * @param db
	 * @param skipAutoId
	 * @param rules
	 * @throws Exception
	 */
	public void exportAll(File directory, Database db, boolean skipAutoId, QueryRule ... rules) throws Exception
	{				
		<#list entities as entity><#if !entity.abstract && entity.association==false>
		export${Name(entity)}(db, new File(directory+"/${entity.name?lower_case}.txt"), skipAutoId ? Arrays.asList(new String[]{<#assign first = true><#list entity.allFields as f><#if !(f.type = "int" && f.auto)><#if first><#assign first=false><#else>,</#if><#if f.type="mref" || f.type="xref"><#list f.xrefLabelNames as label>"${f.name}_${label}"<#if label_has_next>,</#if></#list><#else>"${f.name}"</#if></#if></#list>}) : null, rules);		
		</#if></#list>
			
		logger.debug("done");
	}
	
   /**
	* Export without system tables.
	*/
	public void exportRegular(File directory, Database db, boolean skipAutoId) throws Exception
	{
		exportRegular(directory, db, skipAutoId, new QueryRule[]{});
	}
	
   /**
	* Export without system tables.
	*/
	public void exportRegular(File directory, Database db, boolean skipAutoId, QueryRule ... rules) throws Exception
	{				
		<#list entities as entity><#if !entity.abstract && !entity.system && entity.association==false>
		export${Name(entity)}(db, new File(directory+"/${entity.name?lower_case}.txt"), skipAutoId ? Arrays.asList(new String[]{<#assign first = true><#list entity.allFields as f><#if !(f.type = "int" && f.auto)><#if first><#assign first=false><#else>,</#if><#if f.type="mref" || f.type="xref"><#list f.xrefLabelNames as label>"${f.name}_${label}"<#if label_has_next>,</#if></#list><#else>"${f.name}"</#if></#if></#list>}) : null, rules);		
		</#if></#list>
			
		logger.debug("done");
	}
	
	public void exportAll(File directory, List ... entityLists) throws Exception
	{				
		for(List<? extends Entity> l: entityLists) if(l.size()>0)
		{
			<#list entities as entity><#if !entity.abstract && entity.association==false>
			if(l.get(0).getClass().equals(${JavaName(entity)}.class))
				export${Name(entity)}(l, new File(directory+"/${entity.name?lower_case}.txt"));		
			</#if></#list>
		}
			
		logger.debug("done");
	}
	
	/**
	* Export while excluding or including certain entity types. Defaults set: skip autoId, no QueryRules.
	* If exclusion is set to true, the specialCases are used to exlude those entities from the export (entities not in list are exported).
	* If exclusion is set to false, the specialCases are used to include those entities in the export (only entities in list are exported).
	*/
	public void exportSpecial(File directory, Database db, List<Class<? extends Entity>> specialCases, boolean exclusion) throws Exception
	{
		exportSpecial(directory, db, true, specialCases, exclusion, new QueryRule[]{});
	}
	
	/**
	* Export while excluding or including certain entity types.
	* If exclusion is set to true, the specialCases are used to exlude those entities from the export (entities not in list are exported).
	* If exclusion is set to false, the specialCases are used to include those entities in the export (only entities in list are exported).
	* TODO: Could maybe replace exportAll(File directory, List ... entityLists) ?
	*/
	public void exportSpecial(File directory, Database db, boolean skipAutoId, List<Class<? extends Entity>> specialCases, boolean exclusion, QueryRule ... rules) throws Exception
	{
	<#list entities as entity><#if !entity.abstract && entity.association==false>
		if((exclusion && !specialCases.contains(${JavaName(entity)}.class)) || (!exclusion && specialCases.contains(${JavaName(entity)}.class)))
			{ export${Name(entity)}(db, new File(directory+"/${entity.name?lower_case}.txt"), skipAutoId ? Arrays.asList(new String[]{<#assign first = true><#list entity.allFields as f><#if !(f.type = "int" && f.auto)><#if first><#assign first=false><#else>,</#if><#if f.type="mref" || f.type="xref"><#list f.xrefLabelNames as label>"${f.name}_${label}"<#if label_has_next>,</#if></#list><#else>"${f.name}"</#if></#if></#list>}) : null, rules); }
	</#if></#list>
	
		logger.debug("done");
	}
	
	private QueryRule[] matchQueryRulesToEntity(org.molgenis.model.elements.Entity e, QueryRule ... rules) throws MolgenisModelException
	{
		ArrayList<QueryRule> tmpResult = new ArrayList<QueryRule>();
		for(QueryRule q : rules){
			if(!(e.getAllField(q.getField()) == null)){
				tmpResult.add(q); //field is okay for this entity
			}
			//special case: eg. investigation.name -> if current entity is 'investigation', use field 'name'
			String[] splitField = q.getField().split("\\.");
			if(splitField.length == 2){
				if(e.getName().equals(splitField[0])){
					QueryRule copy = new QueryRule(q);
					copy.setField(splitField[1]);
					tmpResult.add(copy);
				}
			}
		}
		QueryRule[] result = new QueryRule[tmpResult.size()];
		for(int i=0; i<result.length; i++){
			result[i] = tmpResult.get(i);
		}
		return result;
	}

<#list entities as entity><#if !entity.abstract && entity.association==false>
	/**
	 *	export ${Name(entity)} to file.
	 *  @param db the database to export from.
	 *  @param f the file to export to.
	 */
	public void export${Name(entity)}(Database db, File f, List<String> fieldsToExport, QueryRule ... rules) throws DatabaseException, IOException, ParseException, MolgenisModelException
	{
		if(db.count(${JavaName(entity)}.class<#if entity.hasAncestor() || entity.isRootAncestor()>, new QueryRule("${typefield()}",Operator.EQUALS, "${Name(entity)}")</#if>) > 0)
		{
			
			org.molgenis.framework.db.Query<${JavaName(entity)}> query = db.query(${JavaName(entity)}.class);
			<#if entity.hasAncestor() || entity.isRootAncestor()>QueryRule type = new QueryRule("${typefield()}",Operator.EQUALS, "${Name(entity)}");
			query.addRules(type);</#if>
			QueryRule[] newRules = matchQueryRulesToEntity(db.getMetaData().getEntity("${Name(entity)}"), rules);
			query.addRules(newRules);
			int count = query.count();
			if(count > 0){
				CsvWriter ${name(entity)}Writer = new CsvWriter(f);
				try
				{
					query.find(${name(entity)}Writer, fieldsToExport);
				}
				finally
				{
					${name(entity)}Writer.close();
				}
			}
			<#--db.find(${Name(entity)}.class, ${name(entity)}Writer<#if entity.hasAncestor() || entity.isRootAncestor()>, new QueryRule(${typefield()},Operator.EQUALS, "${Name(entity)}")</#if>);-->
		}
	}
	
	public void export${Name(entity)}(List<? extends Entity> entities, File file) throws IOException, MolgenisModelException
	{
		if(entities.size()>0)
		{
			//filter nulls
			List<String> fields = entities.get(0).getFields();
			List<String> notNulls = new ArrayList<String>();
			
			for(String f: fields)
			{
				for(Entity e: entities)
				{
					if(e.get(f) != null)
					{
						notNulls.add(f);
						break;
					}
				}
			}			
			
			//write
			CsvWriter ${name(entity)}Writer = new CsvWriter(file);
			try
			{
				${name(entity)}Writer.writeColNames(notNulls);
				for(Entity e: entities)
				{
					${name(entity)}Writer.write(new EntityTuple(e));
				}
			}
			finally {
				${name(entity)}Writer.close();
			}
		}
	}
</#if></#list>	
}