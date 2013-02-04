<#include "GeneratorHelper.ftl">
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

import app.CsvExport;
import app.CsvImport;
import app.DatabaseFactory;

<#if databaseImp != 'jpa'>	
import app.JDBCDatabase;
<#else>
import javax.persistence.*;
import org.molgenis.framework.db.jpa.JpaDatabase;
import org.molgenis.framework.db.jpa.JpaUtil;
</#if>

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Arrays;

import org.apache.log4j.Logger;

import org.molgenis.Molgenis;
import org.molgenis.util.*;
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

/**
 * This procecure tests file import and export
 * - create csv set1 in tmp
 * - TEST load set1 via CsvImport (should be error free)
 * - export it to set2 via CsvExport
 * - query all of set1 into memory (as lists)
 * - empty the database
 * - import set2 via CsvImport
 * - query all of set2 into memory (as lists)
 * - TEST set1 and set2 should be 'Set' equivalent 
 * - export it to set3 via CsvExport
 * - TEST files of set2 and set3 to be identical on disk
 */
public class TestCsv
{
	private static int total = 10;
	private static Database db;
	private static final Logger logger = Logger.getLogger(TestCsv.class);
	DateFormat dateFormat = new SimpleDateFormat(SimpleTuple.DATEFORMAT, Locale.US);
	DateFormat dateTimeFormat = new SimpleDateFormat(SimpleTuple.DATETIMEFORMAT, Locale.US);	 

<#if databaseImp = 'jpa'>	
	private static java.util.Map<String, Object> configOverrides = new java.util.HashMap<String, Object>();
	static {
		configOverrides.put("javax.persistence.jdbc.url", "${options.dbUri}_test");
		configOverrides.put("hibernate.hbm2ddl.auto", "create-drop");
	}
</#if>
	
<#if databaseImp = 'jpa'>		
	@BeforeClass
	public static void oneTimeSetUp()   
	{
		try
		{		
            db = DatabaseFactory.create(configOverrides);
	<#if !options.getAuthLoginclass()?ends_with("SimpleLogin")>
			app.FillMetadata.fillMetadata(db);
	</#if>			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		logger.info("Database created");
	}
	@AfterClass
	public static void destory() {
            JpaUtil.dropTables((JpaDatabase)db, configOverrides);		
	}	
<#else>
	@BeforeClass
	public static void oneTimeSetUp()   
	{
		try
		{
        		
			db = DatabaseFactory.createTest("${options.molgenis_properties}");
			new Molgenis("${options.molgenis_properties}");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		logger.info("Database created");
	}

	@AfterClass
	public static void destory() {
            
	}
</#if>		
	
	@Test
	public void testCsv1()  throws Exception
	{	
		//create tem working directory
		File dir = File.createTempFile("molgenis","");		
		dir.delete(); //delete the file, need dir
		
		//create a test set1
		<#if databaseImp = 'jpa'>
        TestDataSet set1 = new TestDataSet(50,5);		
        JpaUtil.dropAndCreateTables((JpaDatabase)db, configOverrides);
<#if !options.getAuthLoginclass()?ends_with("SimpleLogin")>
			app.FillMetadata.fillMetadata(db);
</#if>			
        <#else>
        TestDataSet set1 = new TestDataSet(50,5);
        </#if>
                


		CsvEntityExporter entityExporter = new CsvEntityExporter();
		CsvEntityImporter entityImporter = new CsvEntityImporterImpl();
		
		//export set1 from memory to dir1
		File dir1 = new File(dir + "/dir1");
		dir1.mkdirs();
		entityExporter.exportAll(dir1<#if model.entities?size gt 0>, <#list model.entities as entity>set1.${name(entity)}<#if entity_has_next>,</#if></#list></#if>);
	
		//import dir1 into database
		entityImporter.importAll(dir1, db, null);
		
		//copy database into memory as set2
		TestDataSet set2 = copyDb(db);
		
		//TODO compare set1 and set2 except automatic fields
		
		//export set1 from database to dir2
		File dir2 = new File(dir + "/dir2");
		dir2.mkdirs();
		entityExporter.exportAll(dir2,db);
	
		//clean database
		<#if databaseImp = 'jpa'>
            JpaUtil.dropAndCreateTables((JpaDatabase)db, configOverrides);
<#if !options.getAuthLoginclass()?ends_with("SimpleLogin")>
			app.FillMetadata.fillMetadata(db);
</#if>			
		<#else>
			new Molgenis("${options.molgenis_properties}").updateDb();
		</#if>
		
		//import dir2 into database
		entityImporter.importAll(dir2, db, null);
		
		//copy database into memory as set3
		TestDataSet set3 = copyDb(db);

		//TODO compare set2 and set3			
		
		//export database to dir3
		File dir3 = new File(dir + "/dir3");
		dir3.mkdirs();
		entityExporter.exportAll(dir3,db);
		
		//clean database
		<#if databaseImp = 'jpa'>
            JpaUtil.dropAndCreateTables((JpaDatabase)db, configOverrides);
<#if !options.getAuthLoginclass()?ends_with("SimpleLogin")>
			app.FillMetadata.fillMetadata(db);
</#if>			
		<#else>
			new Molgenis("${options.molgenis_properties}").updateDb();
		</#if>
		
		//import dir3 into database
		entityImporter.importAll(dir3, db, null);
		
		//copy database into memory as set4
		TestDataSet set4 = copyDb(db);

		//TODO compare set3 and set4			
		
		//export database to dir4
		File dir4 = new File(dir + "/dir4");
		dir4.mkdirs();
		entityExporter.exportAll(dir4,db);
		
		//compare dir3 and dir4 cause should be equals because roundtrip
		logger.debug("Comparing "+dir3+" to "+dir4);
//		assertTrue(compareDirs(dir3,dir4));
//		assertEquals(set3,set4);
	}
	
	private TestDataSet copyDb(Database db) throws DatabaseException
	{
		TestDataSet copy = new TestDataSet();
<#list model.entities as entity><#if !entity.abstract && !entity.association>
		copy.${name(entity)} = db.find(${JavaName(entity)}.class);
</#if></#list>	
		return copy;	
	}
	
	private boolean compareDirs(File dir1, File dir2) throws IOException
	{
		if(dir1.listFiles().length != dir2.listFiles().length) {
			logger.error(String.format("Difference amount of files in between %s and %s",dir1.getName(), dir2.getName()));
			return false;
		}
		if(!Arrays.equals(dir1.list(), dir2.list())) {
			logger.error(String.format("Difference files in %s and %s",dir1.getName(), dir2.getName()));
			return false;
		}
		
		
		
		String errorMessage = "";
		for(File f: dir1.listFiles())
		{
			File f2 = new File(dir2.getAbsolutePath()+File.separator+f.getName());
			boolean result = CompareCSV.compareCSVFilesByContent(f, f2);
			if(!result) {
				logger.error(f + " and " + f2 + " differ");
				return false;
			}
		}
		return true;
	}
	
    public static void main(String[] args) throws Exception
    {
        oneTimeSetUp();
	new TestCsv().testCsv1();
        destory();
    }
}