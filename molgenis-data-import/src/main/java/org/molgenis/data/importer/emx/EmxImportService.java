package org.molgenis.data.importer.emx;

import com.google.common.collect.ImmutableMap;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.importer.*;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;

@Component
public class EmxImportService implements ImportService
{
	private static final Logger LOG = LoggerFactory.getLogger(EmxImportService.class);

	private final MetaDataParser parser;
	private final ImportWriter writer;
	private final DataService dataService;

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
		if (getSupportedFileExtensions().contains(fileNameExtension.toLowerCase()))
		{
			for (String entityTypeId : source.getEntityTypeIds())
			{
				if (entityTypeId.equalsIgnoreCase(EmxMetaDataParser.EMX_ATTRIBUTES)) return true;
				if (entityTypeId.equalsIgnoreCase(EmxMetaDataParser.EMX_LANGUAGES)) return true;
				if (entityTypeId.equalsIgnoreCase(EmxMetaDataParser.EMX_I18NSTRINGS)) return true;
				if (dataService.getMeta().getEntityType(entityTypeId) != null) return true;
			}
		}

		return false;
	}

	@Override
	public EntityImportReport doImport(final RepositoryCollection source, DatabaseAction databaseAction,
			String packageId)
	{
		ParsedMetaData parsedMetaData = parser.parse(source, packageId);

		// TODO altered entities (merge, see getEntityType)
		return doImport(new EmxImportJob(databaseAction, source, parsedMetaData, packageId));
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
		return newArrayList(DatabaseAction.values());
	}

	@Override
	public boolean getMustChangeEntityName()
	{
		return false;
	}

	@Override
	public Set<String> getSupportedFileExtensions()
	{
		return EmxFileExtensions.getEmx();
	}

	@Override
	public LinkedHashMap<String, Boolean> determineImportableEntities(MetaDataService metaDataService,
			RepositoryCollection repositoryCollection, String selectedPackage)
	{
		List<String> skipEntities = newArrayList(EmxMetaDataParser.EMX_ATTRIBUTES, EmxMetaDataParser.EMX_PACKAGES,
				EmxMetaDataParser.EMX_ENTITIES, EmxMetaDataParser.EMX_TAGS);
		ImmutableMap<String, EntityType> entityTypeMap = parser.parse(repositoryCollection, selectedPackage)
															   .getEntityMap();

		LinkedHashMap<String, Boolean> importableEntitiesMap = newLinkedHashMap();
		stream(entityTypeMap.keySet().spliterator(), false).forEach(entityTypeId ->
		{
			boolean importable = skipEntities.contains(entityTypeId) || metaDataService.isEntityTypeCompatible(
					entityTypeMap.get(entityTypeId));

			importableEntitiesMap.put(entityTypeId, importable);
		});

		return importableEntitiesMap;
	}
}
