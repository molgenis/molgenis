package org.molgenis.data.importer;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.MetaDataService;
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
		this.parser = requireNonNull(parser);
		this.writer = requireNonNull(writer);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public boolean canImport(File file, RepositoryCollection source)
	{
		String fileNameExtension = StringUtils.getFilenameExtension(file.getName());
		if (GenericImporterExtensions.getEMX().contains(fileNameExtension.toLowerCase()))
		{
			for (String entityName : source.getEntityNames())
			{
				if (entityName.equalsIgnoreCase(EmxMetaDataParser.EMX_ATTRIBUTES)) return true;
				if (entityName.equalsIgnoreCase(EmxMetaDataParser.EMX_LANGUAGES)) return true;
				if (entityName.equalsIgnoreCase(EmxMetaDataParser.EMX_I18NSTRINGS)) return true;
				if (dataService.getMeta().getEntityMetaData(entityName) != null) return true;
			}
		}

		return false;
	}

	@Override
	public EntityImportReport doImport(final RepositoryCollection source, DatabaseAction databaseAction,
			String defaultPackage)
	{
		ParsedMetaData parsedMetaData = parser.parse(source, defaultPackage);

		// TODO altered entities (merge, see getEntityMetaData)
		return doImport(new EmxImportJob(databaseAction, source, parsedMetaData, defaultPackage));
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
			throw e;
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

	@Override
	public LinkedHashMap<String, Boolean> integrationTestMetaData(MetaDataService metaDataService,
			RepositoryCollection repositoryCollection, String defaultPackage)
	{
		List<String> skipEntities = Arrays.asList(EmxMetaDataParser.EMX_ATTRIBUTES, EmxMetaDataParser.EMX_PACKAGES,
				EmxMetaDataParser.EMX_ENTITIES, EmxMetaDataParser.EMX_TAGS);
		ParsedMetaData parsedMetaData = parser.parse(repositoryCollection, defaultPackage);
		return metaDataService.integrationTestMetaData(parsedMetaData.getEntityMap(), skipEntities, defaultPackage);
	}
}
