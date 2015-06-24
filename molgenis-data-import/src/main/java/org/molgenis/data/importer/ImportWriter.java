package org.molgenis.data.importer;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.AttributeMetaData;
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
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Tag;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.util.DependencyResolver;
import org.molgenis.util.HugeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Writes the imported metadata and data to target {@link RepositoryCollection}.
 */
public class ImportWriter
{
	private static final Logger LOG = LoggerFactory.getLogger(ImportWriter.class);

	private final DataService dataService;
	private final PermissionSystemService permissionSystemService;
	private final TagService<LabeledResource, LabeledResource> tagService;

	/**
	 * Creates the ImportWriter
	 * 
	 * @param dataService
	 *            {@link DataService} to query existing repositories and transform entities
	 * @param permissionSystemService
	 *            {@link PermissionSystemService} to give permissions on uploaded entities
	 */
	public ImportWriter(DataService dataService, PermissionSystemService permissionSystemService,
			TagService<LabeledResource, LabeledResource> tagService)
	{
		this.dataService = dataService;
		this.permissionSystemService = permissionSystemService;
		this.tagService = tagService;
	}

	@Transactional
	public EntityImportReport doImport(EmxImportJob job)
	{
		RunAsSystemProxy.runAsSystem(() -> {
			importTags(job.source);
			return null;
		});
		importPackages(job.parsedMetaData);
		addEntityMetaData(job.parsedMetaData, job.report, job.metaDataChanges);
		addEntityPermissions(job.metaDataChanges);
		importEntityAndAttributeTags(job.parsedMetaData);
		importData(job.report, job.parsedMetaData.getEntities(), job.source, job.dbAction, job.defaultPackage);
		return job.report;
	}

	private void importEntityAndAttributeTags(ParsedMetaData parsedMetaData)
	{
		for (Tag<EntityMetaData, LabeledResource, LabeledResource> tag : parsedMetaData.getEntityTags())
		{
			tagService.addEntityTag(tag);
		}

		for (EntityMetaData emd : parsedMetaData.getAttributeTags().keySet())
		{
			for (Tag<AttributeMetaData, LabeledResource, LabeledResource> tag : parsedMetaData.getAttributeTags().get(
					emd))
			{
				tagService.addAttributeTag(emd, tag);
			}
		}
	}

	/**
	 * Imports entity data for all entities in {@link #resolved} from {@link #source}
	 */
	private void importData(EntityImportReport report, Iterable<EntityMetaData> resolved, RepositoryCollection source,
			DatabaseAction dbAction, String defaultPackage)
	{
		for (final EntityMetaData entityMetaData : resolved)
		{
			String name = entityMetaData.getName();

			if (dataService.hasRepository(name))
			{
				Repository repository = dataService.getRepository(name);
				Repository fileEntityRepository = source.getRepository(entityMetaData.getName());

				// Try without default package
				if ((fileEntityRepository == null) && (defaultPackage != null)
						&& entityMetaData.getName().toLowerCase().startsWith(defaultPackage.toLowerCase() + "_"))
				{
					fileEntityRepository = source.getRepository(entityMetaData.getName().substring(
							defaultPackage.length() + 1));
				}

				// check to prevent nullpointer when importing metadata only
				if (fileEntityRepository != null)
				{
					// transforms entities so that they match the entity meta data of the output repository
					Iterable<Entity> entities = Iterables.transform(fileEntityRepository,
							new Function<Entity, Entity>()
							{
								@Override
								public DefaultEntity apply(Entity entity)
								{
									return new DefaultEntityImporter(entityMetaData, dataService, entity);
								}
							});

					entities = new DependencyResolver().resolveSelfReferences(entities, entityMetaData);
					int count = update(repository, entities, dbAction);

					// Fix self referenced entities were not imported
					update(repository, this.keepSelfReferencedEntities(entities), DatabaseAction.UPDATE);

					report.addEntityCount(name, count);
				}
			}
		}
	}

	/**
	 * Keeps the entities that have: 1. A reference to themselves. 2. Minimal one value.
	 * 
	 * @param entities
	 * @return Iterable<Entity> - filtered entities;
	 */
	private Iterable<Entity> keepSelfReferencedEntities(Iterable<Entity> entities)
	{
		return Iterables.filter(entities, new Predicate<Entity>()
		{
			@Override
			public boolean apply(Entity entity)
			{
				Iterator<AttributeMetaData> attributes = entity.getEntityMetaData().getAttributes().iterator();
				while (attributes.hasNext())
				{
					AttributeMetaData attribute = attributes.next();
					if (attribute.getRefEntity() != null
							&& attribute.getRefEntity().getName().equals(entity.getEntityMetaData().getName()))
					{
						List<String> ids = entity.getList(attribute.getName());
						Iterable<Entity> refEntities = entity.getEntities(attribute.getName());
						if (ids != null && ids.size() != Iterators.size(refEntities.iterator()))
						{
							throw new UnknownEntityException("One or more values [" + ids + "] from "
									+ attribute.getDataType() + " field " + attribute.getName()
									+ " could not be resolved");
						}
						return true;
					}
				}

				return false;
			}
		});
	}

	/**
	 * Gives the user permission to see and edit his imported entities, unless the user is admin since admins can do
	 * that anyways.
	 */
	private void addEntityPermissions(MetaDataChanges metaDataChanges)
	{
		if (!SecurityUtils.currentUserIsSu())
		{
			permissionSystemService.giveUserEntityPermissions(SecurityContextHolder.getContext(),
					metaDataChanges.getAddedEntities());
		}
	}

	/**
	 * Adds the parsed {@link ParsedMetaData}, creating new repositories where necessary.
	 */
	private void addEntityMetaData(ParsedMetaData parsedMetaData, EntityImportReport report,
			MetaDataChanges metaDataChanges)
	{
		for (EntityMetaData entityMetaData : parsedMetaData.getEntities())
		{
			String name = entityMetaData.getName();
			if (!EmxMetaDataParser.ENTITIES.equals(name) && !EmxMetaDataParser.ATTRIBUTES.equals(name)
					&& !EmxMetaDataParser.PACKAGES.equals(name) && !EmxMetaDataParser.TAGS.equals(name))
			{
				if (dataService.getMeta().getEntityMetaData(entityMetaData.getName()) == null)
				{
					LOG.debug("trying to create: " + name);
					metaDataChanges.addEntity(name);
					Repository repo = dataService.getMeta().addEntityMeta(entityMetaData);
					if (repo != null)
					{
						report.addNewEntity(name);
					}
				}
				else if (!entityMetaData.isAbstract())
				{
					List<AttributeMetaData> addedAttributes = dataService.getMeta().updateEntityMeta(entityMetaData);
					metaDataChanges.addAttributes(name, addedAttributes);
				}
			}
		}
	}

	/**
	 * Adds the packages from the packages sheet to the {@link #metaDataService}.
	 */
	private void importPackages(ParsedMetaData parsedMetaData)
	{
		for (Package p : parsedMetaData.getPackages().values())
		{
			if (p != null)
			{
				dataService.getMeta().addPackage(p);
			}
		}
	}

	/**
	 * Imports the tags from the tag sheet.
	 */
	// FIXME: can everybody always update a tag?
	private void importTags(RepositoryCollection source)
	{
		Repository tagRepo = source.getRepository(TagMetaData.ENTITY_NAME);
		if (tagRepo != null)
		{
			for (Entity tag : tagRepo)
			{
				Entity transformed = new DefaultEntity(TagMetaData.INSTANCE, dataService, tag);
				Entity existingTag = dataService
						.findOne(TagMetaData.ENTITY_NAME, tag.getString(TagMetaData.IDENTIFIER));

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

	/**
	 * Drops entities and added attributes and reindexes the entities whose attributes were modified.
	 */
	public void rollbackSchemaChanges(EmxImportJob job)
	{
		LOG.info("Rolling back changes.");
		dropAddedEntities(job.metaDataChanges.getAddedEntities());
		List<String> entities = dropAddedAttributes(job.metaDataChanges.getAddedAttributes());

		// Reindex
		Set<String> entitiesToIndex = Sets.newLinkedHashSet(job.source.getEntityNames());
		entitiesToIndex.addAll(entities);
		entitiesToIndex.add("tags");
		entitiesToIndex.add("packages");
		entitiesToIndex.add("entities");
		entitiesToIndex.add("attributes");

		reindex(entitiesToIndex);
	}

	/**
	 * Reindexes entities
	 * 
	 * @param entitiesToIndex
	 *            Set of entity names
	 */
	private void reindex(Set<String> entitiesToIndex)
	{
		for (String entity : entitiesToIndex)
		{
			if (dataService.hasRepository(entity))
			{
				Repository repo = dataService.getRepository(entity);
				if ((repo != null) && (repo instanceof IndexedRepository))
				{
					((IndexedRepository) repo).rebuildIndex();
				}
			}
		}
	}

	/**
	 * Drops attributes from entities
	 */
	private List<String> dropAddedAttributes(ImmutableMap<String, Collection<AttributeMetaData>> addedAttributes)
	{
		List<String> entities = Lists.newArrayList(addedAttributes.keySet());
		Collections.reverse(entities);

		for (String entityName : entities)
		{
			for (AttributeMetaData attribute : addedAttributes.get(entityName))
			{
				dataService.getMeta().deleteAttribute(entityName, attribute.getName());
			}
		}
		return entities;
	}

	/**
	 * Drops added entities in the reverse order in which they were created.
	 */
	private void dropAddedEntities(List<String> addedEntities)
	{
		// Rollback metadata, create table statements cannot be rolled back, we have to do it ourselves
		Lists.reverse(addedEntities).forEach(entity -> {
			try
			{
				dataService.getMeta().deleteEntityMeta(entity);
			}
			catch (Exception ex)
			{
				LOG.error("Failed to rollback creation of entity {}", entity);
			}
		});
	}

	/**
	 * Updates a repository with entities.
	 * 
	 * @param repo
	 *            the {@link Repository} to update
	 * @param entities
	 *            the entities to
	 * @param dbAction
	 *            {@link DatabaseAction} describing how to merge the existing entities
	 * @return number of updated entities
	 */
	public int update(Repository repo, Iterable<? extends Entity> entities, DatabaseAction dbAction)
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

	/**
	 * A wrapper for a to import entity
	 * 
	 * When importing, some references (Entity) are still not imported. This wrapper accepts this inconsistency in the
	 * dataService. For example xref and mref.
	 */
	private class DefaultEntityImporter extends DefaultEntity
	{
		/**
		 * Auto generated
		 */
		private static final long serialVersionUID = -5994977400560081655L;
		private final EntityMetaData entityMetaData;

		public DefaultEntityImporter(EntityMetaData entityMetaData, DataService dataService, Entity entity)
		{
			super(entityMetaData, dataService, entity);
			this.entityMetaData = entityMetaData;
		}

		/**
		 * getEntity returns null when attributeName is not resulting in an entity
		 */
		@Override
		public Entity getEntity(String attributeName)
		{
			try
			{
				return super.getEntity(attributeName);
			}
			catch (UnknownEntityException uee)
			{
				// self reference? ignore UnknownEntityExceptions those are solved in a later step
				if (entityMetaData.getName()
						.equals(entityMetaData.getAttribute(attributeName).getRefEntity().getName()))
				{
					return null;
				}
				throw uee;
			}
		}

		/**
		 * getEntities filters the entities that are still not imported
		 */
		@Override
		public Iterable<Entity> getEntities(String attributeName)
		{
			if (entityMetaData.getName().equals(entityMetaData.getAttribute(attributeName).getRefEntity().getName()))
			{
				return from(super.getEntities(attributeName)).filter(notNull());
			}
			else
			{
				return super.getEntities(attributeName);
			}
		}
	}
}