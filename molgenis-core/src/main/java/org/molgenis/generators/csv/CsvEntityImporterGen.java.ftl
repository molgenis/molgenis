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

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.Database.DatabaseAction;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.framework.db.EntityImporter;
import org.molgenis.framework.db.CsvEntityImporter;
import org.molgenis.io.csv.CsvReader;

<#list entities as entity><#if !entity.abstract && !entity.system>
import ${entity.namespace}.db.${JavaName(entity)}EntityImporter;
</#if></#list>

public class CsvEntityImporterImpl implements CsvEntityImporter
{
    /** importable entity names (lowercase) */
	private static final Map<String, EntityImporter> ENTITIES_IMPORTABLE;
	
	static {
		// entities added in import order
		ENTITIES_IMPORTABLE = new LinkedHashMap<String, EntityImporter>();
	<#list entities as entity><#if !entity.abstract && !entity.system>
		ENTITIES_IMPORTABLE.put("${entity.name?lower_case}", new ${JavaName(entity)}EntityImporter());
	</#if></#list>
	}
	
	@Override	
	public int importEntity(Reader reader, String entityName, Database db, DatabaseAction dbAction) throws IOException,
			DatabaseException
	{
		if(dbAction == DatabaseAction.REMOVE || dbAction == DatabaseAction.REMOVE_IGNORE_MISSING)
			throw new IllegalArgumentException("remove action not allowed: " + dbAction);
			
		EntityImporter entityImporter = ENTITIES_IMPORTABLE.get(entityName.toLowerCase());
		if (entityImporter == null) throw new IllegalArgumentException("unknown entity: " + entityName);

		CsvReader csvReader = new CsvReader(reader);
		int nrImportedEntities = 0;

		boolean doTx = !db.inTx();
		try
		{
			if (doTx) db.beginTx();
			nrImportedEntities = entityImporter.importEntity(csvReader, db, dbAction);
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
		return nrImportedEntities;
	}
	
	@Override
	public void close()
	{
		// noop
	}
}