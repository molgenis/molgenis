package org.molgenis.data.importer;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.io.IOUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Package;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.i18n.I18nStringMetaData;
import org.molgenis.data.i18n.LanguageMetaData;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Tag;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.data.support.ConvertingIterable;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.EntityMetaDataUtils;
import org.molgenis.data.support.LazyEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.IntField;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.StringField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.util.DependencyResolver;
import org.molgenis.util.HugeSet;
import org.molgenis.util.MolgenisDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Isolation;
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
	private final MolgenisPermissionService molgenisPermissionService;

	/**
	 * Creates the ImportWriter
	 * 
	 * @param dataService
	 *            {@link DataService} to query existing repositories and transform entities
	 * @param permissionSystemService
	 *            {@link PermissionSystemService} to give permissions on uploaded entities
	 */
	public ImportWriter(DataService dataService, PermissionSystemService permissionSystemService,
			TagService<LabeledResource, LabeledResource> tagService,
			MolgenisPermissionService molgenisPermissionService)
	{
		this.dataService = dataService;
		this.permissionSystemService = permissionSystemService;
		this.tagService = tagService;
		this.molgenisPermissionService = molgenisPermissionService;
	}

	// Use transaction isolation level SERIALIZABLE to prevent problems with the async template, to do ddl statements
	// without influencing on the current transaction
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public EntityImportReport doImport(EmxImportJob job)
	{
		// languages first
		importLanguages(job.report, job.parsedMetaData.getLanguages(), job.dbAction, job.metaDataChanges);

		runAsSystem(() -> importTags(job.source));
		importPackages(job.parsedMetaData);
		addEntityMetaData(job.parsedMetaData, job.report, job.metaDataChanges);
		addEntityPermissions(job.metaDataChanges);
		runAsSystem(() -> importEntityAndAttributeTags(job.parsedMetaData));
		importData(job.report, job.parsedMetaData.getEntities(), job.source, job.dbAction, job.defaultPackage);
		importI18nStrings(job.report, job.parsedMetaData.getI18nStrings(), job.dbAction);

		return job.report;
	}

	private void importLanguages(EntityImportReport report, Map<String, Entity> languages, DatabaseAction dbAction,
			MetaDataChanges metaDataChanges)
	{
		if (!languages.isEmpty())
		{
			Repository repo = dataService.getRepository(LanguageMetaData.ENTITY_NAME);

			List<Entity> transformed = languages.values().stream()
					.map(e -> new DefaultEntityImporter(repo.getEntityMetaData(), dataService, e, false))
					.collect(toList());

			// Find new ones
			transformed.stream().map(Entity::getIdValue).forEach(id -> {
				if (repo.findOne(id) == null)
				{
					metaDataChanges.addLanguage(languages.get(id));
				}
			});

			int count = update(repo, transformed, dbAction);
			report.addEntityCount(LanguageMetaData.ENTITY_NAME, count);
		}
	}

	private void importI18nStrings(EntityImportReport report, Map<String, Entity> i18nStrings, DatabaseAction dbAction)
	{
		if (!i18nStrings.isEmpty())
		{
			Repository repo = dataService.getRepository(I18nStringMetaData.ENTITY_NAME);

			List<Entity> transformed = i18nStrings.values().stream()
					.map(e -> new DefaultEntityImporter(I18nStringMetaData.INSTANCE, dataService, e, false))
					.collect(toList());

			int count = update(repo, transformed, dbAction);
			report.addEntityCount(I18nStringMetaData.ENTITY_NAME, count);
		}
	}

	private void importEntityAndAttributeTags(ParsedMetaData parsedMetaData)
	{
		for (Tag<EntityMetaData, LabeledResource, LabeledResource> tag : parsedMetaData.getEntityTags())
		{
			tagService.addEntityTag(tag);
		}

		for (EntityMetaData emd : parsedMetaData.getAttributeTags().keySet())
		{
			for (Tag<AttributeMetaData, LabeledResource, LabeledResource> tag : parsedMetaData.getAttributeTags()
					.get(emd))
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

			// Languages and i18nstrings are already done
			if (!name.equalsIgnoreCase(LanguageMetaData.ENTITY_NAME)
					&& !name.equalsIgnoreCase(I18nStringMetaData.ENTITY_NAME) && dataService.hasRepository(name))
			{
				Repository repository = dataService.getRepository(name);
				Repository fileEntityRepository = source.getRepository(entityMetaData.getName());

				// Try without default package
				if ((fileEntityRepository == null) && (defaultPackage != null)
						&& entityMetaData.getName().toLowerCase().startsWith(defaultPackage.toLowerCase() + "_"))
				{
					fileEntityRepository = source
							.getRepository(entityMetaData.getName().substring(defaultPackage.length() + 1));
				}

				// check to prevent nullpointer when importing metadata only
				if (fileEntityRepository != null)
				{
					boolean selfReferencing = DependencyResolver.hasSelfReferences(entityMetaData);

					// transforms entities so that they match the entity meta data of the output repository
					Iterable<Entity> entities = Iterables.transform(fileEntityRepository, new Function<Entity, Entity>()
					{
						@Override
						public Entity apply(Entity entity)
						{
							return new DefaultEntityImporter(entityMetaData, dataService, entity, selfReferencing);
						}
					});

					if (selfReferencing)
					{
						entities = new DependencyResolver().resolveSelfReferences(entities, entityMetaData);
					}
					int count = update(repository, entities, dbAction);
					if (selfReferencing && dbAction != DatabaseAction.UPDATE)
					{
						// Fix self referenced entities were not imported
						update(repository, this.keepSelfReferencedEntities(entities), DatabaseAction.UPDATE);
					}
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
							throw new UnknownEntityException(
									"One or more values [" + ids + "] from " + attribute.getDataType() + " field "
											+ attribute.getName() + " could not be resolved");
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
					&& !EmxMetaDataParser.PACKAGES.equals(name) && !EmxMetaDataParser.TAGS.equals(name)
					&& !EmxMetaDataParser.LANGUAGES.equals(name) && !EmxMetaDataParser.I18NSTRINGS.equals(name))
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

	/**
	 * Drops entities and added attributes and reindexes the entities whose attributes were modified.
	 */
	@RunAsSystem
	public void rollbackSchemaChanges(EmxImportJob job)
	{
		LOG.info("Rolling back changes.");
		dataService.delete(LanguageMetaData.ENTITY_NAME, job.metaDataChanges.getAddedLanguages().stream());
		dropAddedEntities(job.metaDataChanges.getAddedEntities());
		List<String> entities = dropAddedAttributes(job.metaDataChanges.getAddedAttributes());

		// FIXME import is not transactional, but uses corrective measures to rollback
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
				if (repo.getCapabilities().contains(RepositoryCapability.INDEXABLE))
				{
					repo.rebuildIndex();
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

		if (!molgenisPermissionService.hasPermissionOnEntity(repo.getName(), Permission.WRITE))
		{
			throw new MolgenisDataAccessException("No WRITE permission on entity '" + repo.getName()
					+ "'. Is this entity already imported by another user who did not grant you WRITE permission?");
		}

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
						Object id = it.next();
						q.eq(idAttributeName, id);
						batchCount++;
						if (batchCount == batchSize || !it.hasNext())
						{
							repo.findAll(q).forEach(existing -> {
								existingIds.add(existing.getIdValue());
							});
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

					count = repo.add(StreamSupport.stream(entities.spliterator(), false));
					break;
				case ADD_IGNORE_EXISTING:
					int batchSize = 1000;
					List<Entity> existingEntities = Lists.newArrayList();
					List<Entity> newEntities = Lists.newArrayList();

					Iterator<? extends Entity> it = entities.iterator();
					while (it.hasNext())
					{
						Entity entity = it.next();
						count++;
						Object id = idDataType.convert(entity.get(idAttributeName));
						if (!existingIds.contains(id))
						{
							newEntities.add(entity);
							if (newEntities.size() == batchSize)
							{
								repo.add(newEntities.stream());
								newEntities.clear();
							}
						}
					}

					if (!newEntities.isEmpty())
					{
						repo.add(newEntities.stream());
					}

					break;
				case ADD_UPDATE_EXISTING:
					batchSize = 1000;
					existingEntities = Lists.newArrayList();
					newEntities = Lists.newArrayList();

					it = entities.iterator();
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
								repo.update(existingEntities.stream());
								existingEntities.clear();
							}
						}
						else
						{
							newEntities.add(entity);
							if (newEntities.size() == batchSize)
							{
								repo.add(newEntities.stream());
								newEntities.clear();
							}
						}
					}

					if (!existingEntities.isEmpty())
					{
						repo.update(existingEntities.stream());
					}

					if (!newEntities.isEmpty())
					{
						repo.add(newEntities.stream());
					}
					break;

				case UPDATE:
					int errorCount = 0;
					StringBuilder msg = new StringBuilder();
					msg.append("Trying to update non-existing ").append(repo.getName()).append(" entities:");

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
					repo.update(StreamSupport.stream(entities.spliterator(), false));
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
	private class DefaultEntityImporter implements Entity
	{
		private static final long serialVersionUID = 1L;

		private final EntityMetaData entityMetaData;
		private final DataService dataService;
		private final Entity entity;
		private final boolean selfReferencing;

		public DefaultEntityImporter(EntityMetaData entityMetaData, DataService dataService, Entity entity,
				boolean selfReferencing)
		{
			this.entityMetaData = requireNonNull(entityMetaData);
			this.dataService = requireNonNull(dataService);
			this.entity = requireNonNull(entity);
			this.selfReferencing = selfReferencing;
		}

		@Override
		public EntityMetaData getEntityMetaData()
		{
			return entityMetaData;
		}

		@Override
		public Iterable<String> getAttributeNames()
		{
			return EntityMetaDataUtils.getAttributeNames(entityMetaData.getAtomicAttributes());
		}

		@Override
		public Object getIdValue()
		{
			return get(entityMetaData.getIdAttribute().getName());
		}

		@Override
		public String getLabelValue()
		{
			return getString(entityMetaData.getLabelAttribute().getName());
		}

		@Override
		public Object get(String attributeName)
		{
			AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
			if (attribute == null) throw new UnknownAttributeException(attributeName);

			FieldTypeEnum dataType = attribute.getDataType().getEnumType();
			switch (dataType)
			{
				case BOOL:
					return getBoolean(attributeName);
				case CATEGORICAL:
				case XREF:
				case FILE:
					return getEntity(attributeName);
				case COMPOUND:
					throw new UnsupportedOperationException();
				case DATE:
					return getDate(attributeName);
				case DATE_TIME:
					return getUtilDate(attributeName);
				case DECIMAL:
					return getDouble(attributeName);
				case EMAIL:
				case ENUM:
				case HTML:
				case HYPERLINK:
				case SCRIPT:
				case STRING:
				case TEXT:
					return getString(attributeName);
				case IMAGE:
					throw new MolgenisDataException("Unsupported data type [" + dataType + "]");
				case INT:
					return getInt(attributeName);
				case LONG:
					return getLong(attributeName);
				case CATEGORICAL_MREF:
				case MREF:
					return getEntities(attributeName);
				default:
					throw new RuntimeException("Unknown data type [" + dataType + "]");
			}
		}

		@Override
		public String getString(String attributeName)
		{
			AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
			if (attribute != null)
			{
				FieldType dataType = attribute.getDataType();
				if (dataType instanceof XrefField)
				{
					return DataConverter.toString(getEntity(attributeName));
				}
			}
			return DataConverter.toString(entity.get(attributeName));
		}

		@Override
		public Integer getInt(String attributeName)
		{
			return DataConverter.toInt(entity.get(attributeName));
		}

		@Override
		public Long getLong(String attributeName)
		{
			return DataConverter.toLong(entity.get(attributeName));
		}

		@Override
		public Boolean getBoolean(String attributeName)
		{
			return DataConverter.toBoolean(entity.get(attributeName));
		}

		@Override
		public Double getDouble(String attributeName)
		{
			return DataConverter.toDouble(entity.get(attributeName));
		}

		@Override
		public List<String> getList(String attributeName)
		{
			return DataConverter.toList(entity.get(attributeName));
		}

		@Override
		public List<Integer> getIntList(String attributeName)
		{
			return DataConverter.toIntList(entity.get(attributeName));
		}

		@Override
		public java.sql.Date getDate(String attributeName)
		{
			java.util.Date utilDate = getUtilDate(attributeName);
			return utilDate != null ? new java.sql.Date(utilDate.getTime()) : null;
		}

		@Override
		public java.util.Date getUtilDate(String attributeName)
		{
			Object value = entity.get(attributeName);
			if (value == null) return null;
			if (value instanceof java.util.Date) return (java.util.Date) value;

			try
			{
				AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
				if (attribute == null) throw new UnknownAttributeException(attributeName);

				FieldTypeEnum dataType = attribute.getDataType().getEnumType();
				switch (dataType)
				{
					case DATE:
						return MolgenisDateFormat.getDateFormat().parse(value.toString());
					case DATE_TIME:
						return MolgenisDateFormat.getDateTimeFormat().parse(value.toString());
					// $CASES-OMITTED$
					default:
						throw new MolgenisDataException("Type [" + dataType + "] is not a date type");

				}
			}
			catch (ParseException e)
			{
				throw new MolgenisDataException(e);
			}
		}

		@Override
		public Timestamp getTimestamp(String attributeName)
		{
			java.util.Date utilDate = getUtilDate(attributeName);
			return utilDate != null ? new Timestamp(utilDate.getTime()) : null;
		}

		@Override
		public Entity getEntity(String attributeName)
		{
			try
			{
				return getEntityLikeDefaultEntity(attributeName);
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
		 * Similar to {@link org.molgenis.data.support.DefaultEntity#getEntity(String)} but returns lazy references if
		 * the decorated entity doesn't have self-references improving import performance.
		 * 
		 * @param attributeName
		 * @return
		 */
		private Entity getEntityLikeDefaultEntity(String attributeName)
		{
			Object value = entity.get(attributeName);
			if (value == null) return null;
			if (value instanceof Entity) return (Entity) value;

			// value represents the id of the referenced entity
			AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
			if (attribute == null) throw new UnknownAttributeException(attributeName);

			if (value instanceof Map)
				return new DefaultEntity(attribute.getRefEntity(), dataService, (Map<String, Object>) value);

			FieldType dataType = attribute.getDataType();
			if (!(dataType instanceof XrefField))
			{
				throw new MolgenisDataException(
						"can't use getEntity() on something that's not an xref, categorical or file");
			}

			value = dataType.convert(value);

			// referenced entity id value must match referenced entity id attribute data type
			FieldType refIdAttr = attribute.getRefEntity().getIdAttribute().getDataType();
			if (refIdAttr instanceof StringField && !(value instanceof String))
			{
				value = String.valueOf(value);
			}
			else if (refIdAttr instanceof IntField && !(value instanceof Integer))
			{
				value = Integer.valueOf(value.toString());
			}

			Entity refEntity;
			if (selfReferencing)
			{
				refEntity = dataService.findOne(attribute.getRefEntity().getName(), value);
				if (refEntity == null) throw new UnknownEntityException(attribute.getRefEntity().getName() + " with "
						+ attribute.getRefEntity().getIdAttribute().getName() + " [" + value + "] does not exist");
			}
			else
			{
				refEntity = new LazyEntity(attribute.getRefEntity(), dataService, value);
			}

			return refEntity;
		}

		@Override
		public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
		{
			Entity entity = getEntity(attributeName);
			return entity != null
					? new ConvertingIterable<E>(clazz, Arrays.asList(entity), dataService).iterator().next() : null;
		}

		@Override
		public Iterable<Entity> getEntities(String attributeName)
		{
			if (entityMetaData.getName().equals(entityMetaData.getAttribute(attributeName).getRefEntity().getName()))
			{
				return from(getEntitiesLikeDefaultEntity(attributeName)).filter(notNull());
			}
			else
			{
				return getEntitiesLikeDefaultEntity(attributeName);
			}
		}

		/**
		 * Similar to {@link org.molgenis.data.support.DefaultEntity#getEntities(String)} but returns lazy references if
		 * the decorated entity doesn't have self-references improving import performance.
		 * 
		 * @param attributeName
		 * @return
		 */
		private Iterable<Entity> getEntitiesLikeDefaultEntity(String attributeName)
		{
			AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
			if (attribute == null) throw new UnknownAttributeException(attributeName);

			FieldType dataType = attribute.getDataType();

			// FIXME this should fail on anything other than instanceof MrefField. requires an extensive code base
			// review to
			// find illegal use of getEntities()
			if (!(dataType instanceof MrefField) && !(dataType instanceof XrefField))
			{
				throw new MolgenisDataException(
						"can't use getEntities() on something that's not an xref, mref, categorical, categorical_mref or file");
			}

			Iterable<?> ids;

			Object value = entity.get(attributeName);
			if (value instanceof String) ids = getList(attributeName);
			else if (value instanceof Entity) return Collections.singletonList((Entity) value);
			else ids = (Iterable<?>) value;

			if ((ids == null) || !ids.iterator().hasNext()) return Collections.emptyList();

			Object firstItem = ids.iterator().next();
			if (firstItem instanceof Entity) return (Iterable<Entity>) ids;

			if (firstItem instanceof Map)
			{
				return stream(ids.spliterator(), false)
						.map(id -> new DefaultEntity(attribute.getRefEntity(), dataService, (Map<String, Object>) id))
						.collect(Collectors.toList());
			}
			if (selfReferencing)
			{
				return from(ids).transform(dataType::convert).transform(
						convertedId -> (dataService.findOne(attribute.getRefEntity().getName(), convertedId)));
			}
			else
			{
				EntityMetaData refEntityMeta = attribute.getRefEntity();
				return new Iterable<Entity>()
				{
					@Override
					public Iterator<Entity> iterator()
					{
						return stream(ids.spliterator(), false).map(id -> {
							// referenced entity id value must match referenced entity id attribute data type
							if (refEntityMeta.getIdAttribute().getDataType() instanceof StringField
									&& !(id instanceof String))
							{
								return String.valueOf(id);
							}
							else if (refEntityMeta.getIdAttribute().getDataType() instanceof IntField
									&& !(id instanceof Integer))
							{
								return Integer.valueOf(id.toString());
							}
							else
							{
								return id;
							}
						}).<Entity> map(id -> new LazyEntity(refEntityMeta, dataService, id)).iterator();
					}
				};
			}
		}

		@Override
		public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
		{
			Iterable<Entity> entities = getEntities(attributeName);
			return entities != null ? new ConvertingIterable<E>(clazz, entities, dataService) : null;
		}

		@Override
		public void set(String attributeName, Object value)
		{
			entity.set(attributeName, value);
		}

		@Override
		public void set(Entity entity)
		{
			entityMetaData.getAtomicAttributes().forEach(attr -> set(attr.getName(), entity.get(attr.getName())));
		}

		@Override
		public String toString()
		{
			return getLabelValue();
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((entityMetaData == null) ? 0 : entityMetaData.hashCode());
			result = prime * result + ((getIdValue() == null) ? 0 : getIdValue().hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (!(obj instanceof Entity)) return false;
			Entity other = (Entity) obj;

			if (entityMetaData == null)
			{
				if (other.getEntityMetaData() != null) return false;
			}
			else if (!entityMetaData.equals(other.getEntityMetaData())) return false;
			if (getIdValue() == null)
			{
				if (other.getIdValue() != null) return false;
			}
			else if (!getIdValue().equals(other.getIdValue())) return false;
			return true;
		}
	}
}