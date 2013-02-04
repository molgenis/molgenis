<#--helper functions-->
<#include "GeneratorHelper.ftl">

<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* Date:        ${date}
 * 
 * generator:   ${generator} ${version}
 *
 * 
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */
package ${package};

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.io.TableWriter;
import org.molgenis.io.TupleReader;
import org.molgenis.io.TupleWriter;
import org.molgenis.io.csv.CsvReader;
import org.molgenis.io.excel.ExcelWriter;
import org.molgenis.util.tuple.Tuple;

import com.google.common.collect.Lists;

public class ExcelEntityExporter
{
	private static Logger logger = Logger.getLogger(ExcelEntityExporter.class);
	
	/**
	 * Default export all using a target file and a database to export
	 * @param directory
	 * @param db
	 * @throws Exception
	 */
	public void exportAll(File excelFile, Database db) throws Exception
	{
		exportAll(excelFile, db, false, new QueryRule[]{});
	}
	
	/**
	 * Export all using a set of QueryRules used for all entities if applicable to that entity
	 * @param directory
	 * @param db
	 * @param rules
	 * @throws Exception
	 */
	public void exportAll(File excelFile, Database db, QueryRule ... rules) throws Exception
	{
		exportAll(excelFile, db, false, rules);
	}
	
	/**
	 * Export all where a boolean skipAutoId forces an ignore of the auto id field ("id")
	 * @param directory
	 * @param db
	 * @param skipAutoId
	 * @throws Exception
	 */
	public void exportAll(File excelFile, Database db, boolean skipAutoId) throws Exception
	{
		exportAll(excelFile, db, skipAutoId, new QueryRule[]{});
	}
	
	/**
	 * Export all with both a boolean skipAutoId and a set of QueryRules to specify both the skipping of auto id, and applying of a filter
	 * @param directory
	 * @param db
	 * @param skipAutoId
	 * @param rules
	 * @throws Exception
	 */
	public void exportAll(File excelFile, Database db, boolean skipAutoId, QueryRule ... rules) throws Exception
	{
		// Do checks on target file
		if(excelFile.exists()){
			throw new Exception("Target file " + excelFile.getAbsolutePath() + " already exists, will not proceed.");
		}
		boolean createSuccess = excelFile.createNewFile();
		if(!createSuccess){
			throw new Exception("Creation of target file " + excelFile.getAbsolutePath() + " failed, cannot proceed.");
		}
		
		// Create temporary directory
		File directory = new File(System.getProperty("java.io.tmpdir") + File.separator + "molgenis_export"+System.currentTimeMillis());
		directory.mkdir();
		
		// Export CSV to this directory
		CsvEntityExporter entityExporter = new CsvEntityExporter();
		entityExporter.exportAll(directory, db, skipAutoId, rules);
			
		// Create new Excel workbook
		TableWriter entitiesWriter = new ExcelWriter(excelFile);
		try
		{
		  	// Variable: copy file contents to the workbook sheets
		  	<#list entities as entity><#if !entity.abstract && entity.system==false>
		  	File ${entity.name?uncap_first}File = new File(directory+"/${entity.name?lower_case}.txt");
		  	if(${entity.name?uncap_first}File.exists())
			  	{
			  	CsvReader ${entity.name?uncap_first}Reader = new CsvReader(${entity.name?uncap_first}File);
			  	try 
			  	{
					copyCsvToWorkbook("${entity.name}", ${entity.name?uncap_first}Reader, entitiesWriter);
				}
				finally
				{
					${entity.name?uncap_first}Reader.close();
				}
			}		
			</#if></#list>
		}
		finally
		{
			IOUtils.closeQuietly(entitiesWriter);
			
			// Remove temporary directory
			FileUtils.deleteDirectory(directory);
		}
	}

	/**
	 * Convert a CSV to an Excel sheet inside a workbook
	 * @throws IOException 
	 */
	private void copyCsvToWorkbook(String tableName, TupleReader tupleReader, TableWriter tableWriter) throws IOException
	{
		// create table
		TupleWriter tupleWriter = tableWriter.createTupleWriter(tableName);
		try
		{
			// write table header
			List<String> colNames = Lists.newArrayList(tupleReader.colNamesIterator());
			tupleWriter.writeColNames(colNames);
			
			// write table data rows
			for (Tuple tuple : tupleReader)
				tupleWriter.write(tuple);
		}
		finally 
		{
			tupleWriter.close();
		}
	}
}