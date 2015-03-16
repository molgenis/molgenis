package org.molgenis.data.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.support.GenericImporterExtensions;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;

@Component
public class EmxImportService implements ImportService
{
	private static final Logger LOG = LoggerFactory.getLogger(EmxImportService.class);

	private final MetaDataParser parser;
	private final ImportWriter writer;
	private final DataService dataService;

	@Autowired
	public EmxImportService(MetaDataParser parser, ImportWriter writer, DataService dataService)
	{
		if (parser == null) throw new IllegalArgumentException("parser is null");
		if (writer == null) throw new IllegalArgumentException("writer is null");
		LOG.debug("EmxImportService created");
		this.parser = parser;
		this.writer = writer;
		this.dataService = dataService;
	}

	@Override
	public boolean canImport(File file, RepositoryCollection source)
	{
		String fileNameExtension = StringUtils.getFilenameExtension(file.getName());
		if (GenericImporterExtensions.getEMX().contains(fileNameExtension.toLowerCase()))
		{
			for (String entityName : source.getEntityNames())
			{
				if (entityName.equalsIgnoreCase(EmxMetaDataParser.ATTRIBUTES)) return true;
				if (dataService.getMeta().getEntityMetaData(entityName) != null) return true;
				if (canImportByHeuristic(entityName)) return true;
			}
		}

		return false;
	}

	private boolean canImportByHeuristic(String entityName)
	{
		// entity is importable if entity name is the simple name of an existing entity,
		// and only one entity with this simple name exists
		List<EntityMetaData> entityMetaDatas = new ArrayList<>();
		for (EntityMetaData entityMetaData : dataService.getMeta().getEntityMetaDatas())
		{
			if (entityName.equals(entityMetaData.getSimpleName()))
			{
				entityMetaDatas.add(entityMetaData);
			}
		}
		return entityMetaDatas.size() == 1;
	}

	@Override
	public EntityImportReport doImport(final RepositoryCollection source, DatabaseAction databaseAction)
	{
		ParsedMetaData parsedMetaData = parser.parse(source);

		// TODO altered entities (merge, see getEntityMetaData)
		return doImport(new EmxImportJob(databaseAction, source, parsedMetaData));
	}

	/**
	 * Does the import in a transaction. Manually rolls back schema changes if something goes wrong. Refreshes the
	 * metadata.
	 * 
	 * @return {@link EntityImportReport} describing what happened
	 */
	public EntityImportReport doImport(EmxImportJob job)
	{
		try
		{
			return writer.doImport(job);
		}
		catch (Exception e)
		{
			LOG.error("Error handling EmxImportJob", e);
			try
			{
				writer.rollbackSchemaChanges(job);
			}
			catch (Exception ignore)
			{
			}
			throw e;
		}
		finally
		{
			dataService.getMeta().refreshCaches();
		}
	}

	@Override
	public EntitiesValidationReport validateImport(File file, RepositoryCollection source)
	{
		return parser.validate(source);
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}

	@Override
	public List<DatabaseAction> getSupportedDatabaseActions()
	{
		return Lists.newArrayList(DatabaseAction.values());
	}

	@Override
	public boolean getMustChangeEntityName()
	{
		return false;
	}

	@Override
	public Set<String> getSupportedFileExtensions()
	{
		return GenericImporterExtensions.getEMX();
	}
}
