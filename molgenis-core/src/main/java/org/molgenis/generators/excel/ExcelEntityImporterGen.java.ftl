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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.Database.DatabaseAction;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.framework.db.EntityImporter;
import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelSheetReader;

<#list entities as entity><#if !entity.abstract>
import ${entity.namespace}.db.${JavaName(entity)}EntityImporter;
</#if></#list>

public class ExcelEntityImporter
{
    /** importable entity names (lowercase) */
	private static final Map<String, EntityImporter> ENTITIES_IMPORTABLE;
	
	static {
		// entities added in import order
		ENTITIES_IMPORTABLE = new LinkedHashMap<String, EntityImporter>();
	<#list entities as entity><#if !entity.abstract>
		ENTITIES_IMPORTABLE.put("${entity.name?lower_case}", new ${JavaName(entity)}EntityImporter());
	</#if></#list>
	}
	
	private final Database db;
	
	public ExcelEntityImporter(Database db) {
		if(db == null) throw new IllegalArgumentException("db is null");
		this.db = db;
	}
	
	public EntityImportReport importData(File file, DatabaseAction dbAction) throws IOException, DatabaseException
	{
		return importData(new FileInputStream(file), dbAction);
	}
	
	public EntityImportReport importData(InputStream is, DatabaseAction dbAction) throws IOException, DatabaseException
	{
		if(dbAction == DatabaseAction.REMOVE || dbAction == DatabaseAction.REMOVE_IGNORE_MISSING)
			throw new IllegalArgumentException("remove action not allowed: " + dbAction);
		
		ExcelReader reader = new ExcelReader(is);

		EntityImportReport importReport = new EntityImportReport();
		boolean doTx = !db.inTx();
		try
		{
			if (doTx) db.beginTx();

			// map sheet names to sheets
			Map<String, ExcelSheetReader> sheetMap = new HashMap<String, ExcelSheetReader>();
			for (ExcelSheetReader sheet : reader)
			{
				String sheetName = sheet.getName();
				ExcelSheetReader previousValue = sheetMap.put(sheetName.toLowerCase(), sheet);
				if (previousValue != null) throw new IOException("duplicate sheet names not allowed: " + sheetName);
			}

			// import sheets in order defined by entities map
			for (Map.Entry<String, EntityImporter> entry : ENTITIES_IMPORTABLE.entrySet())
			{
				ExcelSheetReader sheet = sheetMap.get(entry.getKey());
				if (sheet != null)
				{
					EntityImporter entityImporter = entry.getValue();
					int nr = entityImporter.importEntity(sheet, db, dbAction);
					if(nr > 0)
						importReport.getMessages().put(entry.getKey(), "imported " + nr + " " + entry.getKey() + " entities");
				}
			}
			if (doTx) db.commitTx();
		}
		catch (IOException e)
		{
			if (doTx) db.rollbackTx();
			throw e;
		}
		catch (DatabaseException e)
		{
			if (doTx) db.rollbackTx();
			throw e;
		}
		finally
		{
			reader.close();
		}
		return importReport;
	}
}