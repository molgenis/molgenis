package org.molgenis.data.importer;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IndexedRepository;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Package;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.meta.WritableMetaDataService;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.support.TransformedEntity;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.util.DependencyResolver;
import org.molgenis.util.HugeSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Component
public class EmxImportService implements ImportService
{
	private static final Logger logger = Logger.getLogger(EmxImportService.class);

	private static final List<String> SUPPORTED_FILE_EXTENSIONS = Arrays.asList("xls", "xlsx", "csv", "zip");

	private MysqlRepositoryCollection targetCollection;
	private TransactionTemplate transactionTemplate;
	private final DataService dataService;
	private PermissionSystemService permissionSystemService;
	private WritableMetaDataService metaDataService;

	private final EmxMetaDataParser parser = new EmxMetaDataParser();

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
		try
		{
			return transactionTemplate.execute(new EmxImportTransactionCallback(databaseAction, source, metadataList,
					addedEntities, addedAttributes, permissionSystemService));
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

	private final class EmxImportTransactionCallback implements TransactionCallback<EntityImportReport>
	{
		private final RepositoryCollection source;
		private final List<EntityMetaData> sourceMetadata;
		private final List<String> addedEntities;
		private final Map<String, List<String>> addedAttributes;// Attributes per entity
		private final DatabaseAction dbAction;
		private final PermissionSystemService permissionSystemService;
		private final EntityImportReport report = new EntityImportReport();

		private EmxImportTransactionCallback(DatabaseAction dbAction, RepositoryCollection source,
				List<EntityMetaData> metadata, List<String> addedEntities, Map<String, List<String>> addedAttributes,
				PermissionSystemService permissionSystemService)
		{
			this.permissionSystemService = permissionSystemService;
			this.dbAction = dbAction;
			this.source = source;
			this.sourceMetadata = metadata;
			this.addedEntities = addedEntities;
			this.addedAttributes = addedAttributes;
		}

		@Override
		public EntityImportReport doInTransaction(TransactionStatus status)
		{
			try
			{
				importTags();
				importPackages();
				List<EntityMetaData> resolved = resolveEntityDependencies();
				addEntityMetaData(resolved);
				addEntityPermissions();
				importData(resolved);
				return report;
			}
			catch (Exception e)
			{
				logger.info("Error in import transaction, setRollbackOnly", e);
				status.setRollbackOnly();
				throw e;
			}
		}

		private void importData(List<EntityMetaData> resolved)
		{
			for (final EntityMetaData entityMetaData : resolved)
			{
				String name = entityMetaData.getName();
				CrudRepository crudRepository = (CrudRepository) targetCollection.getRepositoryByEntityName(name);

				if (crudRepository != null)
				{
					Repository fileEntityRepository = source.getRepositoryByEntityName(entityMetaData.getSimpleName());

					// check to prevent nullpointer when importing metadata only
					if (fileEntityRepository != null)
					{
						// transforms entities so that they match the entity meta data of the output repository
						Iterable<Entity> entities = Iterables.transform(fileEntityRepository,
								new Function<Entity, Entity>()
								{
									@Override
									public Entity apply(Entity entity)
									{
										return new TransformedEntity(entity, entityMetaData, dataService);
									}
								});
						entities = DependencyResolver.resolveSelfReferences(entities, entityMetaData);

						int count = update(crudRepository, entities, dbAction);
						report.getNrImportedEntitiesMap().put(name, count);
					}
				}
			}
		}

		/**
		 * Gives the user permission to see and edit his imported entities, unless the user is admin since admins can do
		 * that anyways.
		 */
		private void addEntityPermissions()
		{
			if (!SecurityUtils.currentUserIsSu())
			{
				permissionSystemService.giveUserEntityAndMenuPermissions(SecurityContextHolder.getContext(),
						addedEntities);
			}
		}

		private void addEntityMetaData(List<EntityMetaData> resolved)
		{
			for (EntityMetaData entityMetaData : resolved)
			{
				String name = entityMetaData.getName();
				if (!EmxMetaDataParser.ENTITIES.equals(name) && !EmxMetaDataParser.ATTRIBUTES.equals(name)
						&& !EmxMetaDataParser.PACKAGES.equals(name) && !EmxMetaDataParser.TAGS.equals(name))
				{
					if (metaDataService.getEntityMetaData(entityMetaData.getName()) == null)
					{
						logger.debug("tyring to create: " + name);
						addedEntities.add(name);
						Repository repo = targetCollection.add(entityMetaData);
						if (repo != null)
						{
							report.addNewEntity(name);
						}
					}
					else if (!entityMetaData.isAbstract())
					{
						List<String> addedEntityAttributes = Lists.transform(targetCollection.update(entityMetaData),
								new Function<AttributeMetaData, String>()
								{
									@Override
									public String apply(AttributeMetaData input)
									{
										return input.getName();
									}
								});
						if ((addedEntityAttributes != null) && !addedEntityAttributes.isEmpty())
						{
							addedAttributes.put(name, addedEntityAttributes);
						}
					}
				}
			}
		}

		private List<EntityMetaData> resolveEntityDependencies()
		{
			Set<EntityMetaData> allMetaData = Sets.newLinkedHashSet(sourceMetadata);
			Iterable<EntityMetaData> existingMetaData = metaDataService.getEntityMetaDatas();
			Iterables.addAll(allMetaData, existingMetaData);

			// Use all metadata for dependency resolving
			List<EntityMetaData> resolved = DependencyResolver.resolve(allMetaData);

			// Only import source
			resolved.retainAll(sourceMetadata);
			return resolved;
		}

		private void importPackages()
		{
			Map<String, PackageImpl> packages = parser.parsePackagesSheet(source);
			for (Package p : packages.values())
			{
				if (p != null)
				{
					metaDataService.addPackage(p);
				}
			}
		}

		private void importTags()
		{
			Repository tagRepo = source.getRepositoryByEntityName(TagMetaData.ENTITY_NAME);
			if (tagRepo != null)
			{
				for (Entity tag : tagRepo)
				{
					Entity transformed = new TransformedEntity(tag, new TagMetaData(), dataService);
					Entity existingTag = dataService.findOne(TagMetaData.ENTITY_NAME,
							tag.getString(TagMetaData.IDENTIFIER));

					if (existingTag == null)
					{
						dataService.add(TagMetaData.ENTITY_NAME, transformed);
					}
					else
					{
						dataService.update(TagMetaData.ENTITY_NAME, transformed);
					}
				}
			}
		}

		public int update(CrudRepository repo, Iterable<? extends Entity> entities, DatabaseAction dbAction)
		{
			if (entities == null) return 0;

			String idAttributeName = repo.getEntityMetaData().getIdAttribute().getName();
			FieldType idDataType = repo.getEntityMetaData().getIdAttribute().getDataType();

			HugeSet<Object> existingIds = new HugeSet<Object>();
			HugeSet<Object> ids = new HugeSet<Object>();
			try
			{
				for (Entity entity : entities)
				{
					Object id = entity.get(idAttributeName);
					if (id != null)
					{
						ids.add(id);
					}
				}

				if (!ids.isEmpty())
				{
					// Check if the ids already exist
					if (repo.count() > 0)
					{
						int batchSize = 100;
						Query q = new QueryImpl();
						Iterator<Object> it = ids.iterator();
						int batchCount = 0;
						while (it.hasNext())
						{
							q.eq(idAttributeName, it.next());
							batchCount++;
							if (batchCount == batchSize || !it.hasNext())
							{
								for (Entity existing : repo.findAll(q))
								{
									existingIds.add(existing.getIdValue());
								}
								q = new QueryImpl();
								batchCount = 0;
							}
							else
							{
								q.or();
							}
						}
					}
				}

				int count = 0;
				switch (dbAction)
				{
					case ADD:
						if (!existingIds.isEmpty())
						{
							StringBuilder msg = new StringBuilder();
							msg.append("Trying to add existing ").append(repo.getName())
									.append(" entities as new insert: ");

							int i = 0;
							Iterator<?> it = existingIds.iterator();
							while (it.hasNext() && i < 5)
							{
								if (i > 0)
								{
									msg.append(",");
								}
								msg.append(it.next());
								i++;
							}

							if (it.hasNext())
							{
								msg.append(" and more.");
							}
							throw new MolgenisDataException(msg.toString());
						}
						count = repo.add(entities);
						break;

					case ADD_UPDATE_EXISTING:
						int batchSize = 1000;
						List<Entity> existingEntities = Lists.newArrayList();
						List<Entity> newEntities = Lists.newArrayList();

						Iterator<? extends Entity> it = entities.iterator();
						while (it.hasNext())
						{
							Entity entity = it.next();
							count++;
							Object id = idDataType.convert(entity.get(idAttributeName));
							if (existingIds.contains(id))
							{
								existingEntities.add(entity);
								if (existingEntities.size() == batchSize)
								{
									repo.update(existingEntities);
									existingEntities.clear();
								}
							}
							else
							{
								newEntities.add(entity);
								if (newEntities.size() == batchSize)
								{
									repo.add(newEntities);
									newEntities.clear();
								}
							}
						}

						if (!existingEntities.isEmpty())
						{
							repo.update(existingEntities);
						}

						if (!newEntities.isEmpty())
						{
							repo.add(newEntities);
						}
						break;

					case UPDATE:
						int errorCount = 0;
						StringBuilder msg = new StringBuilder();
						msg.append("Trying to update not exsisting ").append(repo.getName()).append(" entities:");

						for (Entity entity : entities)
						{
							count++;
							Object id = idDataType.convert(entity.get(idAttributeName));
							if (!existingIds.contains(id))
							{
								if (++errorCount == 6)
								{
									break;
								}

								if (errorCount > 0)
								{
									msg.append(", ");
								}
								msg.append(id);
							}
						}

						if (errorCount > 0)
						{
							if (errorCount == 6)
							{
								msg.append(" and more.");
							}
							throw new MolgenisDataException(msg.toString());
						}
						repo.update(entities);
						break;

					default:
						break;

				}

				return count;
			}
			finally
			{
				IOUtils.closeQuietly(existingIds);
				IOUtils.closeQuietly(ids);
			}
		}
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
