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
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.Database.DatabaseAction;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.EntitiesImporter;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.framework.db.EntityImporter;
import org.molgenis.io.TableReader;
import org.molgenis.io.TableReaderFactory;
import org.molgenis.io.TupleReader;

<#list entities as entity>
<#if !entity.abstract && !entity.system>
import ${entity.namespace}.db.${JavaName(entity)}EntityImporter;
</#if>
</#list>

public class EntitiesImporterImpl implements EntitiesImporter
{
	/** importable entity names (lowercase) */
	private static final Map<String, EntityImporter> ENTITIES_IMPORTABLE;
	
	static {
		// entities added in import order
		ENTITIES_IMPORTABLE = new LinkedHashMap<String, EntityImporter>();
	<#list entities as entity>
		<#if !entity.abstract && !entity.system>
		ENTITIES_IMPORTABLE.put("${entity.name?lower_case}", new ${JavaName(entity)}EntityImporter());
		</#if>
	</#list>
	}
	
	private Database db;
	
	@Deprecated
	public EntitiesImporterImpl() {
	}
	
	public EntitiesImporterImpl(Database db) {
		if(db == null) throw new IllegalArgumentException("db is null");
		this.db = db;
	}
	
	@Override
	public EntityImportReport importEntities(File file, DatabaseAction dbAction) throws IOException, DatabaseException
	{
		return importEntities(TableReaderFactory.create(file), dbAction);
	}

	@Override
	public EntityImportReport importEntities(List<File> files, DatabaseAction dbAction) throws IOException,
			DatabaseException
	{
		return importEntities(TableReaderFactory.create(files), dbAction);
	}
	
	@Override
	public EntityImportReport importEntities(TupleReader tupleReader, String entityName, DatabaseAction dbAction) throws IOException, DatabaseException
	{
		final TupleReader reader = tupleReader;
		final String name = entityName;
		return importEntities(new TableReader()
		{
			@Override
			public Iterator<TupleReader> iterator()
			{
				return Collections.singletonList(reader).iterator();
			}

			@Override
			public void close() throws IOException
			{
				reader.close();
			}

			@Override
			public TupleReader getTupleReader(String tableName) throws IOException
			{
				return name.equals(tableName) ? reader : null;
			}

			@Override
			public Iterable<String> getTableNames() throws IOException
			{
				return Collections.singletonList(name);
			}
		}, dbAction);
	}
		
	@Override	
	public EntityImportReport importEntities(TableReader tableReader, DatabaseAction dbAction) throws IOException,
			DatabaseException
	{
		EntityImportReport importReport = new EntityImportReport();

		boolean doTx = !db.inTx();
		try
		{
			// map entity names on tuple readers
			Map<String, TupleReader> tupleReaderMap = new HashMap<String, TupleReader>();
			for (String tableName : tableReader.getTableNames())
			{
				tupleReaderMap.put(tableName.toLowerCase(), tableReader.getTupleReader(tableName));
			}

			if (doTx) db.beginTx();

			// import entities in order defined by entities map
			for (Map.Entry<String, EntityImporter> entry : ENTITIES_IMPORTABLE.entrySet())
			{
				String entityName = entry.getKey();
				TupleReader tupleReader = tupleReaderMap.get(entityName);
				if (tupleReader != null)
				{
					EntityImporter entityImporter = entry.getValue();
					int nr = entityImporter.importEntity(tupleReader, db, dbAction);
					if (nr > 0) {
						importReport.getMessages().put(entry.getKey(), "imported " + nr + " " + entityName + " entities");
						importReport.addNrImported(nr);
					}
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
			tableReader.close();
		}
		return importReport;
	}
	
	@Override
	public void setDatabase(Database db) {
		this.db = db;
	}
}