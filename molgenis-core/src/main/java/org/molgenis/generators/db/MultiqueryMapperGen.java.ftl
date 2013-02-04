<#include "GeneratorHelper.ftl">
<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* File:        ${model.getName()}/model/${entity.getName()}.java
 * Copyright:   GBIC 2000-${year?c}, all rights reserved
 * Date:        ${date}
 * Template:	${template}
 * generator:   ${generator} ${version}
 *
 * Using "subclass per table" strategy
 *
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */

package ${package};

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.jdbc.AbstractJDBCMapper;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.fieldtypes.*;

<#list allFields(entity) as f><#if f.type == "file" || f.type == "image">
import org.apache.commons.io.FileUtils;
<#break>
</#if></#list>

import org.molgenis.framework.db.jdbc.JDBCDatabase;
import org.molgenis.framework.db.QueryRule;
<#list allFields(entity) as f><#if f.type == "mref" || f.type="xref">
import org.molgenis.util.ValueLabel;
<#break>
</#if></#list>
import ${entity.getNamespace()}.${JavaName(entity)};

<#if entity.hasAncestor()>
import ${entity.getAncestor().getNamespace()}.${JavaName(entity.getAncestor())};
import ${entity.getAncestor().getNamespace()}.db.${JavaName(entity.getAncestor())}Mapper;
</#if>
<#list entity.getAllFields() as f>
	<#if f.type=="xref" || f.type=="mref">
		<#assign xref_entity = f.xrefEntity> 
import ${xref_entity.getNamespace()}.${JavaName(xref_entity)};
		<#if f.type=="mref"><#assign mref_entity = model.getEntity(f.mrefName)>
import ${mref_entity.getNamespace()}.${JavaName(mref_entity)};
		</#if>
	</#if>
</#list>

public class ${JavaName(entity)}Mapper extends AbstractJDBCMapper<${JavaName(entity)}>
{	
	Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public int executeAdd(List<? extends ${JavaName(entity)}> entities) throws DatabaseException
	{	
		<#if entity.hasAncestor()>
		//add superclass first
		this.getDatabase().getMapperFor(${entity.ancestor.namespace}.${JavaName(entity.ancestor)}.class).executeAdd(entities);
		</#if>
	
		Connection conn = getDatabase().getConnection();
		//create big mysql query
		StringBuffer sql = new StringBuffer("INSERT INTO ${SqlName(entity)} (<#list addFields(entity) as f>${SqlName(f)}<#if f_has_next>,</#if></#list>) VALUES ");
		{
		
			boolean first = true;
			for(${JavaName(entity)} e: entities)
			{
				// put the ,
				if(first)
					first = false;
				else
					sql.append(",");
					
				sql.append("(");			
<#list addFields(entity) as f>
				//${name(f)}
				<#if f.type == "xref" || f.type == "mref">
				if(e.get${JavaName(f)}_${JavaName(f.xrefField)}() != null){
				<#else>
				if(e.get${JavaName(f)}() != null){
				</#if>
								
				<#if f.type == "datetime">
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String mysqlDateTime = dateFormat.format(e.get${JavaName(f)}());
					sql.append("'"+this.escapeSql(mysqlDateTime)+"'"
				<#elseif f.type == "date">
					sql.append("'"+new java.sql.Date(e.get${JavaName(f)}().getTime()).toString()+"'"
				<#elseif f.type == "bool">
					sql.append(e.get${JavaName(f)}()
				<#elseif f.type == "xref" || f.type == "mref">					
					sql.append("'"+this.escapeSql(e.get${JavaName(f)}_${JavaName(f.xrefField)}().toString())+"'"
				<#else>
					sql.append("'"+this.escapeSql(e.get${JavaName(f)}().toString())+"'"
				</#if>
				<#if f_has_next>+","</#if>);
				}
				else{
					sql.append("null<#if f_has_next>,</#if>");
				}
</#list>
				sql.append(")");
			}
		}		
		
		//execute sql
		Statement stmt = null; 		
		try
		{			
			stmt = conn.createStatement();
			//logger.debug("created statement: "+sql.toString());
			int updatedRows = stmt.executeUpdate(sql.toString(), Statement.RETURN_GENERATED_KEYS);
			getGeneratedKeys(entities, stmt, 0);
			return updatedRows;			
		} catch (SQLException sqlEx) {
                    throw new DatabaseException(sqlEx);
                }
		finally
		{
			JDBCDatabase.closeStatement(stmt);
		}
	}

	@Override
	public int executeUpdate(List<? extends ${JavaName(entity)}> entities) throws DatabaseException
	{
<#if entity.hasAncestor()>	
		//update superclass first
		this.getDatabase().getMapperFor(${entity.ancestor.namespace}.${JavaName(entity.ancestor)}.class).executeUpdate(entities);
</#if>			
		Connection conn = getDatabase().getConnection();
		
		//create sql string
		StringBuffer sql = new StringBuffer("INSERT INTO ${SqlName(entity)} (<#list addFieldsIncKey(entity) as f>${SqlName(f)}<#if f_has_next>,</#if></#list>) VALUES ");		
		boolean first = true;
		for(${JavaName(entity)} e: entities)
		{
			// put the ,
			if(first)
				first = false;
			else
				sql.append(",");

			sql.append("(");
			
<#list addFieldsIncKey(entity) as f>
			//${name(f)}
		<#if f.isReadOnly() && !isPrimaryKey(f,entity) && f.type != "file" && f.type != "image">
			//readonly placeholder for insert-clause to prohibit not "null" errors: will be ignored in update
			sql.append("<@compress single_line=true>
				<#if f.type == "enum">
					'${f.getEnumOptions()?first}'
				<#elseif f.type == "date">
					'"+new java.sql.Date(e.get${JavaName(f)}().getTime()).toString()+"'" +"
				<#elseif f.type == "datetime">
					'"+new java.sql.Timestamp(e.get${JavaName(f)}().getTime()).toString()+"'" +"
				<#else>
					0
				</#if>
				<#if f_has_next>, </#if>
			</@compress>");	
		<#else>


			<#if f.type == "xref" || f.type == "mref">
			if(e.get${JavaName(f)}_${JavaName(f.xrefField)}() != null){
			<#else>
			if(e.get${JavaName(f)}() != null){
			</#if>
                sql.append(<@compress single_line=true>
				<#if f.type == "enum">
					<#-->"'${f.getEnumOptions()?first}'" causes fail -->
					"'"+this.escapeSql(e.get${JavaName(f)}())+"'"
				<#elseif f.type == "date">
					"'"+new java.sql.Date(e.get${JavaName(f)}().getTime()).toString()+"'"
				<#elseif f.type == "datetime">
					"'"+new java.sql.Timestamp(e.get${JavaName(f)}().getTime()).toString()+"'"
				<#elseif f.type == "bool">
					e.get${JavaName(f)}()
                <#elseif f.type == "xref" || f.type == "mref">					
					"'"+this.escapeSql(e.get${JavaName(f)}_${JavaName(f.xrefField)}()).toString()+"'"
				<#else>
					"'"+this.escapeSql(e.get${JavaName(f)}()).toString()+"'"
				</#if>
                <#if f_has_next>+","</#if></@compress>);
			} else {
				sql.append("null<#if f_has_next>,</#if>");
            }
		</#if>
		
</#list>				
			sql.append(")");
		}
		<#-- old: <#list updateFields(entity) as f>-->
		sql.append(" ON DUPLICATE KEY UPDATE <#list updateFields(entity) as f>${SqlName(f)}=<#if f.type = "int" && f.isAuto()>LAST_INSERT_ID<#else>VALUES</#if>(${SqlName(f)})<#if f_has_next>,</#if></#list>");

		//execute sql
		Statement stmt = null;	
		try
		{
			stmt = conn.createStatement();
			return stmt.executeUpdate(sql.toString())/2;	
		}
		catch(SQLException sqlEx){
                    logger.debug("Query that caused exception:" + sql.toString());                    
                    throw new DatabaseException(sqlEx);
		}
		finally
		{
			JDBCDatabase.closeStatement(stmt);
		}		
	}

	@Override
	public int executeRemove(List<? extends ${JavaName(entity)}> entities) throws DatabaseException
	{
		Connection conn = getDatabase().getConnection();
		int rowsAffected = 0;
		
		//create sql
		StringBuffer sql = new StringBuffer("DELETE FROM ${SqlName(entity)} WHERE ");
		
		<#--alert, might not work for complex key-->
<#list keyFields(entity) as f>
		//key $f_index: ${name(f)}
		{
			sql.append("${SqlName(f)} in (");
			boolean first = true;
			for(${JavaName(entity)} e: entities)
			{
				// put the ,
				if(first)
					first = false;
				else
					sql.append(",");			
				sql.append("'"+this.escapeSql(e.get${JavaName(f)}().toString())+"'");
			}				
			sql.append(") <#if f_has_next> AND </#if>");
		}
</#list>		
	
		//execute sql
		Statement stmt = null;
		try
		{	
			stmt = conn.createStatement();
			rowsAffected = stmt.executeUpdate(sql.toString());	
		} 
		catch (SQLException sqlEx) 
		{
			throw new DatabaseException(sqlEx);
		}
		finally
		{
			JDBCDatabase.closeStatement(stmt);
		}		
<#if entity.hasAncestor()>		
		//remove superclass after
		this.getDatabase().getMapperFor(${entity.ancestor.namespace}.${JavaName(entity.ancestor)}.class).executeRemove(entities);
</#if>		
		return rowsAffected;
	}
	
//Generated by MapperCommons.subclass_per_table.java.ftl
<#include "MapperCommons.subclass_per_table.java.ftl">

//Generated by MapperFileAttachments.java.ftl
<#include "MapperFileAttachments.java.ftl">

//Generated by MapperMrefs.java.ftl
<#include "MapperMrefs.java.ftl"/>

}
