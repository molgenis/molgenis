package org.molgenis.data.importer;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IndexedRepository;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.WritableMetaDataService;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.security.permission.PermissionSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Component
public class EmxImportService implements ImportService
{
	static final Logger logger = Logger.getLogger(EmxImportService.class);

	private static final List<String> SUPPORTED_FILE_EXTENSIONS = Arrays.asList("xls", "xlsx", "csv", "zip");

	MysqlRepositoryCollection targetCollection;
	private TransactionTemplate transactionTemplate;
	final DataService dataService;
	private PermissionSystemService permissionSystemService;
	WritableMetaDataService metaDataService;

	final EmxMetaDataParser parser = new EmxMetaDataParser();

	@Autowired
	public EmxImportService(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		logger.debug("EmxImportService created");
		this.dataService = dataService;
	}

	@Autowired
	public void setRepositoryCollection(MysqlRepositoryCollection targetCollection,
			WritableMetaDataService metaDataService)
	{
		this.targetCollection = targetCollection;
		this.metaDataService = metaDataService;
		logger.debug("EmxImportService created with targetCollection=" + targetCollection + " and metaDataService="
				+ metaDataService);
	}

	@Autowired
	public void setPlatformTransactionManager(PlatformTransactionManager transactionManager)
	{
		transactionTemplate = new TransactionTemplate(transactionManager);
	}

	@Autowired
	public void setPermissionSystemService(PermissionSystemService permissionSystemService)
	{
		this.permissionSystemService = permissionSystemService;
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

		List<EntityMetaData> metadataList = parser.combineMetaDataToList(dataService, source);

		List<String> addedEntities = Lists.newArrayList();
		Map<String, List<String>> addedAttributes = Maps.newLinkedHashMap();
		// TODO altered entities (merge, see getEntityMetaData)
		EmxImportWriter writer = new EmxImportWriter(this, databaseAction, source, metadataList, addedEntities,
				addedAttributes, permissionSystemService);
		try
		{
			return transactionTemplate.execute(writer);
		}
		catch (Exception e)
		{
			rollbackSchemaChanges(source, addedEntities, addedAttributes);
			throw e;
		}
		finally
		{
			metaDataService.refreshCaches();
		}

	}

	private void rollbackSchemaChanges(final RepositoryCollection source, List<String> addedEntities,
			Map<String, List<String>> addedAttributes)
	{
		logger.info("Rolling back changes.");

		dropAddedEntities(addedEntities);

		List<String> entities = dropAddedAttributes(addedAttributes);

		// Reindex
		Set<String> entitiesToIndex = Sets.newLinkedHashSet(source.getEntityNames());
		entitiesToIndex.addAll(entities);

		reindex(entitiesToIndex);
	}

	private void reindex(Set<String> entitiesToIndex)
	{
		for (String entity : entitiesToIndex)
		{
			if (dataService.hasRepository(entity))
			{
				Repository repo = dataService.getRepositoryByEntityName(entity);
				if ((repo != null) && (repo instanceof IndexedRepository))
				{
					((IndexedRepository) repo).rebuildIndex();
				}
			}
		}
	}

	private List<String> dropAddedAttributes(Map<String, List<String>> addedAttributes)
	{
		List<String> entities = Lists.newArrayList(addedAttributes.keySet());
		Collections.reverse(entities);

		for (String entityName : entities)
		{
			List<String> attributes = addedAttributes.get(entityName);
			for (String attributeName : attributes)
			{
				targetCollection.dropAttributeMetaData(entityName, attributeName);
			}
		}
		return entities;
	}

	private void dropAddedEntities(List<String> addedEntities)
	{
		// Rollback metadata, create table statements cannot be rolled back, we have to do it ourselfs
		Collections.reverse(addedEntities);

		for (String entityName : addedEntities)
		{
			targetCollection.dropEntityMetaData(entityName);
		}
	}

	@Override
	public EntitiesValidationReport validateImport(File file, RepositoryCollection source)
	{
		return parser.validateInput(dataService, source);
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
