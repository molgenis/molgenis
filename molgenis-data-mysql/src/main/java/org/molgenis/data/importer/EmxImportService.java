package org.molgenis.data.importer;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.WritableMetaDataService;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
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

	private static final List<String> SUPPORTED_FILE_EXTENSIONS = Arrays.asList("xls", "xlsx", "csv", "zip");

	private final MetaDataParser parser;
	private final ImportWriter writer;
	private MysqlRepositoryCollection targetCollection;
	private WritableMetaDataService metaDataService;

	@Autowired
	public EmxImportService(MetaDataParser parser, ImportWriter writer)
	{
		if (parser == null) throw new IllegalArgumentException("parser is null");
		if (writer == null) throw new IllegalArgumentException("writer is null");
		LOG.debug("EmxImportService created");
		this.parser = parser;
		this.writer = writer;
	}

	@Autowired
	public void setRepositoryCollection(MysqlRepositoryCollection targetCollection,
			WritableMetaDataService metaDataService)
	{
		this.targetCollection = targetCollection;
		this.metaDataService = metaDataService;
		LOG.debug("EmxImportService created with targetCollection=" + targetCollection + " and metaDataService="
				+ metaDataService);
	}

	@Override
	public boolean canImport(File file, RepositoryCollection source)
	{
		String fileNameExtension = StringUtils.getFilenameExtension(file.getName());
		if (SUPPORTED_FILE_EXTENSIONS.contains(fileNameExtension.toLowerCase()))
		{
			for (String entityName : source.getEntityNames())
			{
				if (entityName.equalsIgnoreCase(EmxMetaDataParser.ATTRIBUTES)) return true;
				if (targetCollection.getRepositoryByEntityName(entityName) != null) return true;
			}
		}

		return false;
	}

	@Override
	public EntityImportReport doImport(final RepositoryCollection source, DatabaseAction databaseAction)
	{
		if (targetCollection == null) throw new RuntimeException("targetCollection was not set");
		if (metaDataService == null) throw new RuntimeException("metadataService was not set");

		ParsedMetaData parsedMetaData = parser.parse(source);

		// TODO altered entities (merge, see getEntityMetaData)
		return doImport(new EmxImportJob(databaseAction, source, parsedMetaData, targetCollection));

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
			writer.rollbackSchemaChanges(job);
			throw e;
		}
		finally
		{
			metaDataService.refreshCaches();
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

}
