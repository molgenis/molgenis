<#include "GeneratorHelper.ftl">
<#setting number_format="#"/>
<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* File:        app/JUnitTest.java
 * Copyright:   GBIC 2000-${year?c}, all rights reserved
 * Date:        ${date}
 * 
 * generator:   ${generator} ${version}
 *
 * 
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */

package ${package};

import app.DatabaseFactory;
<#if databaseImp != 'jpa'>	
import app.JDBCDatabase;
<#else>
import javax.persistence.*;
import org.molgenis.framework.db.jpa.JpaDatabase;
import org.molgenis.framework.db.jpa.JpaUtil;
</#if>

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import org.molgenis.Molgenis;
import org.molgenis.util.Entity;
import org.molgenis.util.SimpleTuple;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.Query;
import org.molgenis.framework.db.DatabaseException;

import static  org.testng.AssertJUnit.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

<#list model.entities as entity>
import ${entity.namespace}.${JavaName(entity)};
</#list>

public class TestDatabase
{
	private static int total = 10;
	private static Database db;
	private static final Logger logger = Logger.getLogger(TestDatabase.class);
	DateFormat dateFormat = new SimpleDateFormat(SimpleTuple.DATEFORMAT, Locale.US);
	DateFormat dateTimeFormat = new SimpleDateFormat(SimpleTuple.DATETIMEFORMAT, Locale.US);	 

<#if databaseImp = 'jpa'>
	private static java.util.Map<String, Object> configOverrides = new java.util.HashMap<String, Object>();
	static {
		configOverrides.put("javax.persistence.jdbc.url", "${options.dbUri}_test");
		configOverrides.put("hibernate.hbm2ddl.auto", "create");		
	}
</#if>

	/*
	 * Create a database to use
	 */
	@BeforeClass(alwaysRun = true)
	public static void oneTimeSetUp()   
	{
		try
		{
		//bad: test expects an existing, but empty database.
		//this means the previous test will need to end with e.g.
		//new emptyDatabase(new MolgenisServlet().getDatabase(), false);	
		<#if databaseImp = 'jpa'>		
			db = DatabaseFactory.create(configOverrides);
			JpaUtil.createTables(db, true, configOverrides);
		<#else>
			<#if db_mode = 'standalone'>
			//db = new MolgenisServlet().getDatabase();
                        db = DatabaseFactory.createTest("${options.molgenis_properties}"); //correct?	
			<#else>
			db = DatabaseFactory.createTest("${options.molgenis_properties}");	
			//create the database
			new Molgenis("${options.molgenis_properties}").updateDb();
			</#if>
		</#if>	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		logger.info("Database created");
	}
<#if databaseImp = 'jpa'>		
	@AfterClass(alwaysRun = true)
	public static void destroy() {
		JpaUtil.dropTables((JpaDatabase)db, configOverrides);
	}	
</#if>		
		
<#list entities as entity><#if !entity.abstract && !entity.association>
<#assign dependson = entity.getDependencies()/>
<#if dependson?size &gt; 0>
	@Test(dependsOnMethods = {<#list dependson as d><#if d_index &gt; 0>,</#if>"test${JavaName(d)}"</#list>})
<#else>
	@Test
</#if>
	<#assign dependson = "test" + JavaName(entity)/>
	public void test${JavaName(entity)}() throws DatabaseException
	{
		//create entities
		List<${JavaName(entity)}> entities = new ArrayList<${JavaName(entity)}>();

		//retrieve xref entity candidates
<#list entity.allFields as f><#if !f.auto>
	<#if f.type == "xref" || f.type == "mref">
		List<${JavaName(f.xrefEntity)}> ${name(f)}Xrefs = db.query(${JavaName(f.xrefEntity)}.class)<#if f.xrefEntity.hasAncestor()>.eq("${typefield()}",${JavaName(f.xrefEntity)}.class.getSimpleName())</#if>.find();	
	</#if></#if>
</#list>		

		for(Integer i = 0; i < total; i++)
		{
			${JavaName(entity)} e = new ${JavaName(entity)}();
			<#list entity.allFields as f><#if !f.auto>
			<#if f.type == "xref">
			if(${name(f)}Xrefs.size() > 0) e.set${JavaName(f)}_${JavaName(f.xrefField)}( ${name(f)}Xrefs.get(i).get${JavaName(f.xrefField)}() );
			<#elseif f.type == "mref">
			if(${name(f)}Xrefs.size() > 0)
			{
				e.get${JavaName(f)}_${JavaName(f.xrefField)}().add( ${name(f)}Xrefs.get(i).get${JavaName(f.xrefField)}() );
				//e.get${JavaName(f)}().add( random(${name(f)}Xrefs).get${JavaName(f.xrefField)}() );
			}
			<#elseif f.type=="bool">
			e.set${JavaName(f)}(randomBool(i));
			<#elseif f.type=="date">
			e.set${JavaName(f)}(new java.sql.Date(new java.util.Date().getTime()));
			<#elseif f.type=="datetime">
			e.set${JavaName(f)}(new java.sql.Timestamp(new java.util.Date().getTime()));
			<#elseif f.type=="enum">
			e.set${JavaName(f)}(randomEnum(new String[]{<#list f.enumOptions as option><#if option_index &gt; 0>,</#if>"${option}"</#list>}));
			<#elseif f.type=="decimal">
			e.set${JavaName(f)}(i.doubleValue());
			<#elseif f.type == "int">
			e.set${JavaName(f)}(i);
			<#elseif f.type == "long">
			e.set${JavaName(f)}(i.longValue());
			<#elseif f.type == "string">
			e.set${JavaName(f)}(truncate("${entity.name?lower_case}_${f.name?lower_case}_"+i, ${f.length?c}));
			<#else>
			e.set${JavaName(f)}("${entity.name?lower_case}_${f.name?lower_case}_"+i);
			</#if></#if></#list>	
				
			entities.add(e);
		}

			
		//add entities and check counts
		db.add(entities);
		Query<${JavaName(entity)}> q = db.query(${JavaName(entity)}.class)<#if entity.hasAncestor() || entity.hasDescendants()>.eq("${typefield()}",${JavaName(entity)}.class.getSimpleName())</#if>;
		assertEquals(total, q.count());
		List<${JavaName(entity)}> entitiesDb = q.sortASC("${pkey(entity).name}").find();
		assertEquals(total, entitiesDb.size());
<#if databaseImp != 'jpa'>		
		//compare entities against insert (assumes sorting by id)
		for(int i = 0; i < total; i++)
		{
			assertNotNull(entities.get(i).get${JavaName(pkey(entity))}());
	<#list entity.allFields as f><#if pkey(entity).name != f.name && !f.auto><#if f.type == "date">
			//check formatted because of milliseconds rounding
			assertEquals(dateFormat.format(entities.get(i).get${JavaName(f)}()), dateFormat.format(entitiesDb.get(i).get${JavaName(f)}()));
	<#elseif f.type == "datetime">
			//check formatted because of milliseconds rounding
			assertEquals(dateTimeFormat.format(entities.get(i).get${JavaName(f)}()),dateTimeFormat.format(entitiesDb.get(i).get${JavaName(f)}()));
	<#elseif f.type == "xref" || f.type == "mref">
			assertEquals(entities.get(i).get${JavaName(f)}_${JavaName(f.xrefField)}(), entitiesDb.get(i).get${JavaName(f)}_${JavaName(f.xrefField)}());
	<#else>
			assertEquals(entities.get(i).get${JavaName(f)}(), entitiesDb.get(i).get${JavaName(f)}());
	</#if>
	</#if></#list>		
		}	
</#if>
		
		//test the query capabilities by finding on all fields
		for(${JavaName(entity)} entity: entitiesDb)
		{
<#list entity.allFields as f>
<#assign types = ["int", "string", "text", "varchar", "xref"]>
<#if types?seq_contains(f.type)>
<#if f.type == "xref" && databaseImp != 'jpa'>
	<#assign fieldGetter = JavaName(f) + "_" + JavaName(f.xrefField)>
<#else>
	<#assign fieldGetter = JavaName(f)>
</#if>
			//test field '${f.name}', type '${f.type}'
			{
				Query<${JavaName(entity)}> q2 = db.query(${JavaName(entity)}.class);
				q2.equals("${name(f)}",entity.get${fieldGetter}());
				List<${JavaName(entity)}> results = q2.find();
<#if pkey(entity) == f>
				assertEquals(results.size(),1);
</#if>			
				for(${JavaName(entity)} r: results)
				{
					<#if f.type == "xref">
					assertEquals(r.get${JavaName(f)}_${JavaName(f.xrefField)}(), entity.get${JavaName(f)}_${JavaName(f.xrefField)}());
					<#else>
					assertEquals(r.get${JavaName(f)}(),entity.get${JavaName(f)}());
					</#if>
				}
			}
			//test operator 'in' for field '${f.name}'
			{
				Query<${JavaName(entity)}> q2 = db.query(${JavaName(entity)}.class);
				java.util.List<Object> inList = new ArrayList<Object>();
				inList.add(entity.get${fieldGetter}());
				q2.in("${name(f)}", inList);
				List<${JavaName(entity)}> results = q2.find();
<#if pkey(entity) == f>
				assertEquals(results.size(),1);
</#if>			
				for(${JavaName(entity)} r: results)
				{
					<#if f.type == "xref">
					assertEquals(r.get${JavaName(f)}_${JavaName(f.xrefField)}(), entity.get${JavaName(f)}_${JavaName(f.xrefField)}());
					<#else>
					assertEquals(r.get${JavaName(f)}(),entity.get${JavaName(f)}());
					</#if>
				}
			}
<#if f.type == "string" || f.type == "text" || f.type == "varchar">
			//test operator 'like' for field '${f.name}'
			{
				Query<${JavaName(entity)}> q2 = db.query(${JavaName(entity)}.class);
				q2.like("${name(f)}", entity.get${fieldGetter}() + "%");
				q2.sortASC("${name(f)}");
				List<${JavaName(entity)}> results = q2.find();
				for(${JavaName(entity)} r: results)
				{
					assertTrue(org.apache.commons.lang.StringUtils.startsWith(r.get${JavaName(f)}(), entity.get${JavaName(f)}()));
				}
			}
<#elseif f.type == "int">
			//test operator 'lessOrEqual' for field '${f.name}'
			{
				Query<${JavaName(entity)}> q2 = db.query(${JavaName(entity)}.class);
				q2.lessOrEqual("${name(f)}", entity.get${fieldGetter}());
				q2.sortASC("${name(f)}");
				List<${JavaName(entity)}> results = q2.find();
				for(${JavaName(entity)} r: results)
				{
					assertTrue(r.get${JavaName(f)}().compareTo(entity.get${JavaName(f)}()) < 1);
				}
			}
			//test operator 'greaterOrEqual' for field '${f.name}'
			{
				Query<${JavaName(entity)}> q2 = db.query(${JavaName(entity)}.class);
				q2.greaterOrEqual("${name(f)}", entity.get${fieldGetter}());
				q2.sortDESC("${name(f)}");
				List<${JavaName(entity)}> results = q2.find();
				for(${JavaName(entity)} r: results)
				{
					assertTrue(r.get${JavaName(f)}().compareTo(entity.get${JavaName(f)}()) > -1);
				}
			}
<#elseif f.type == "xref">
<#list f.xrefLabelNames as label>
			//test operator 'equals' for implicit join field '${f.name}_${label}'
			{
				Query<${JavaName(entity)}> q2 = db.query(${JavaName(entity)}.class);
				q2.equals("${name(f)}_${label}",entity.get${JavaName(f)}_${JavaName(label)}());
				List<${JavaName(entity)}> results = q2.find();
<#if pkey(entity) == f>
				assertEquals(results.size(),1);
</#if>			
				for(${JavaName(entity)} r: results)
				{
					<#if f.type == "xref">
					assertEquals(r.get${JavaName(f)}_${JavaName(f.xrefField)}(), entity.get${JavaName(f)}_${JavaName(f.xrefField)}());
					<#else>
					assertEquals(r.get${JavaName(f)}(),entity.get${JavaName(f)}());
					</#if>
				}
			}
			//test operator 'in' for implicit join field '${f.name}_${label}'
			{
				Query<${JavaName(entity)}> q2 = db.query(${JavaName(entity)}.class);
				java.util.List<Object> inList = new ArrayList<Object>();
				inList.add(entity.get${JavaName(f)}_${JavaName(label)}());
				q2.in("${name(f)}_${label}", inList);
				q2.sortDESC("${name(f)}_${label}");
				List<${JavaName(entity)}> results = q2.find();
<#if pkey(entity) == f>
				assertEquals(results.size(),1);
</#if>			
				for(${JavaName(entity)} r: results)
				{
					<#if f.type == "xref">
					assertEquals(r.get${JavaName(f)}_${JavaName(f.xrefField)}(), entity.get${JavaName(f)}_${JavaName(f.xrefField)}());
					<#else>
					assertEquals(r.get${JavaName(f)}(),entity.get${JavaName(f)}());
					</#if>
				}
			}
</#list>
</#if>

</#if>
</#list>
		}
	}

</#if></#list>	
	
	/** Helper to get random element from a list */
	public <E extends Entity> E random(List<E> entities)
	{
		return entities.get( Long.valueOf( Math.round( Math.random() * (entities.size() - 1) )).intValue() );
	}
	
	public Boolean randomBool(int i)
	{
		return i % 2 == 0 ? true : false;
	}
	
	public String randomEnum(String[] options)
	{
		Integer index = Long.valueOf(Math.round(Math.random() * (options.length - 1) )).intValue();
		return options[index];
	}
	
	public String truncate(String value, int length)
	{
	   if (value != null && value.length() > length)
          value = value.substring(0, length-1);
       return value;
	}
	
	 
	 
}