package org.molgenis.data.importer;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.*;
import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.SemanticTag;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.data.support.EntityMetaDataUtils;
import org.molgenis.data.support.LazyEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.util.DependencyResolver;
import org.molgenis.util.HugeSet;
import org.molgenis.util.MolgenisDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.MolgenisFieldTypes.AttributeType.INT;
import static org.molgenis.MolgenisFieldTypes.AttributeType.XREF;
import static org.molgenis.data.i18n.model.I18nStringMetaData.I18N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetaData.LANGUAGE;
import static org.molgenis.data.importer.EmxMetaDataParser.*;
import static org.molgenis.data.meta.model.TagMetaData.TAG;
import static org.molgenis.data.support.EntityMetaDataUtils.isSingleReferenceType;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.molgenis.util.ApplicationContextProvider.getApplicationContext;

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
	private final TagMetaData tagMetaData;
	private final I18nStringMetaData i18nStringMetaData;
	private final TagFactory tagFactory;

	/**
	 * Creates the ImportWriter
	 *
	 * @param dataService             {@link DataService} to query existing repositories and transform entities
	 * @param permissionSystemService {@link PermissionSystemService} to give permissions on uploaded entities
	 * @param tagMetaData
	 * @param i18nStringMetaData
	 * @param tagFactory              {@link TagFactory} to create new tags
	 */
	public ImportWriter(DataService dataService, PermissionSystemService permissionSystemService,
			TagService<LabeledResource, LabeledResource> tagService,
			MolgenisPermissionService molgenisPermissionService, TagMetaData tagMetaData,
			I18nStringMetaData i18nStringMetaData, TagFactory tagFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.permissionSystemService = requireNonNull(permissionSystemService);
		this.tagService = requireNonNull(tagService);
		this.molgenisPermissionService = requireNonNull(molgenisPermissionService);
		this.tagMetaData = requireNonNull(tagMetaData);
		this.i18nStringMetaData = requireNonNull(i18nStringMetaData);
		this.tagFactory = requireNonNull(tagFactory);
	}

	@Transactional
	public EntityImportReport doImport(EmxImportJob job)
	{
		// languages first
		importLanguages(job.report, job.parsedMetaData.getLanguages(), job.dbAction, job.metaDataChanges);
		runAsSystem(() -> importTags(job.source));
		importPackages(job.parsedMetaData);
		addEntityMetaData(job.parsedMetaData, job.report, job.metaDataChanges);
		addEntityPermissions(job.metaDataChanges);
		runAsSystem(() -> importEntityAndAttributeTags(job.parsedMetaData));
		Iterable<EntityMetaData> existingMetaData = dataService.getMeta().getEntityMetaDatas()::iterator;
		Map<String, EntityMetaData> allEntityMetaDataMap = new HashMap<>();
		for (EntityMetaData emd : job.parsedMetaData.getEntities())
		{
			allEntityMetaDataMap.put(emd.getName(), emd);
		}
		scanMetaDataForSystemEntityMetaData(allEntityMetaDataMap, existingMetaData);
		importData(job.report, DependencyResolver.resolve(Sets.newLinkedHashSet(allEntityMetaDataMap.values())),
				job.source, job.dbAction, job.defaultPackage);
		importI18nStrings(job.report, job.parsedMetaData.getI18nStrings(), job.dbAction);

		return job.report;
	}

	private void importLanguages(EntityImportReport report, Map<String, Entity> languages, DatabaseAction dbAction,
			MetaDataChanges metaDataChanges)
	{
		if (!languages.isEmpty())
		{
			Repository<Entity> repo = dataService.getRepository(LANGUAGE);

			List<Entity> transformed = languages.values().stream()
					.map(e -> new DefaultEntityImporter(repo.getEntityMetaData(), dataService, e, false))
					.collect(toList());

			// Find new ones
			transformed.stream().map(Entity::getIdValue).forEach(id -> {
				if (repo.findOneById(id) == null)
				{
					metaDataChanges.addLanguage(languages.get(id));
				}
			});

			int count = update(repo, transformed, dbAction);
			report.addEntityCount(LANGUAGE, count);
		}
	}

	private void importI18nStrings(EntityImportReport report, Map<String, Entity> i18nStrings, DatabaseAction dbAction)
	{
		if (!i18nStrings.isEmpty())
		{
			Repository<Entity> repo = dataService.getRepository(I18N_STRING);

			List<Entity> transformed = i18nStrings.values().stream()
					.map(e -> new DefaultEntityImporter(i18nStringMetaData, dataService, e, false)).collect(toList());

			int count = update(repo, transformed, dbAction);
			report.addEntityCount(I18N_STRING, count);
		}
	}

	private void importEntityAndAttributeTags(ParsedMetaData parsedMetaData)
	{
		for (SemanticTag<EntityMetaData, LabeledResource, LabeledResource> tag : parsedMetaData.getEntityTags())
		{
			tagService.addEntityTag(tag);
		}

		for (EntityMetaData emd : parsedMetaData.getAttributeTags().keySet())
		{
			for (SemanticTag<AttributeMetaData, LabeledResource, LabeledResource> tag : parsedMetaData
					.getAttributeTags().get(emd))
			{
				tagService.addAttributeTag(emd, tag);
			}
		}
	}

	/**
	 * Imports entity data for all entities in resolved from source
	 */
	private void importData(EntityImportReport report, Iterable<EntityMetaData> resolved, RepositoryCollection source,
			DatabaseAction dbAction, String defaultPackage)
	{
		for (final EntityMetaData entityMetaData : resolved)
		{
			String name = entityMetaData.getName();

			// Languages and i18nstrings are already done
			if (!name.equalsIgnoreCase(LANGUAGE) && !name.equalsIgnoreCase(I18N_STRING) && dataService
					.hasRepository(name))
			{
				Repository<Entity> repository = dataService.getRepository(name);
				Repository<Entity> fileEntityRepository = source.getRepository(entityMetaData.getName());

				// Try without default package
				if ((fileEntityRepository == null) && (defaultPackage != null) && entityMetaData.getName().toLowerCase()
						.startsWith(defaultPackage.toLowerCase() + "_"))
				{
					fileEntityRepository = source
							.getRepository(entityMetaData.getName().substring(defaultPackage.length() + 1));
				}

				// check to prevent nullpointer when importing metadata only
				if (fileEntityRepository != null)
				{
					boolean selfReferencing = DependencyResolver.hasSelfReferences(entityMetaData);

					// transforms entities so that they match the entity meta data of the output repository
					Iterable<Entity> entities = Iterables.transform(fileEntityRepository,
							entity -> new DefaultEntityImporter(entityMetaData, dataService, entity, selfReferencing));

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
		return Iterables.filter(entities, entity -> {
			Iterator<AttributeMetaData> attributes = entity.getEntityMetaData().getAttributes().iterator();
			while (attributes.hasNext())
			{
				AttributeMetaData attribute = attributes.next();
				if (attribute.getRefEntity() != null && attribute.getRefEntity().getName()
						.equals(entity.getEntityMetaData().getName()))
				{
					List<String> ids = DataConverter.toList(entity.get(attribute.getName()));
					Iterable<Entity> refEntities = entity.getEntities(attribute.getName());
					if (ids != null && ids.size() != Iterators.size(refEntities.iterator()))
					{
						throw new UnknownEntityException(
								"One or more values [" + ids + "] from " + attribute.getDataType().toString()
										+ " field " + attribute.getName() + " could not be resolved");
					}
					return true;
				}
			}

			return false;
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
			permissionSystemService
					.giveUserEntityPermissions(SecurityContextHolder.getContext(), metaDataChanges.getAddedEntities());
		}
	}

	/**
	 * Adds the parsed {@link ParsedMetaData}, creating new repositories where necessary.
	 */
	private void addEntityMetaData(ParsedMetaData parsedMetaData, EntityImportReport report,
			MetaDataChanges metaDataChanges)
	{
		Iterable<EntityMetaData> existingMetaData = dataService.getMeta().getEntityMetaDatas()::iterator;
		Map<String, EntityMetaData> allEntityMetaDataMap = new HashMap<>();
		for (EntityMetaData emd : parsedMetaData.getEntities())
		{
			allEntityMetaDataMap.put(emd.getName(), emd);
		}
		scanMetaDataForSystemEntityMetaData(allEntityMetaDataMap, existingMetaData);
		List<EntityMetaData> resolve = DependencyResolver
				.resolve(new HashSet<EntityMetaData>(Sets.newLinkedHashSet(allEntityMetaDataMap.values())));
		for (EntityMetaData entityMetaData : resolve)
		{
			String name = entityMetaData.getName();
			if (!EMX_ENTITIES.equals(name) && !EMX_ATTRIBUTES.equals(name) && !EMX_PACKAGES.equals(name) && !EMX_TAGS
					.equals(name) && !EMX_LANGUAGES.equals(name) && !EMX_I18NSTRINGS.equals(name))
			{
				if (dataService.getMeta().getEntityMetaData(entityMetaData.getName()) == null)
				{
					LOG.debug("trying to create: " + name);
					metaDataChanges.addEntity(name);
					Repository<Entity> repo = dataService.getMeta().addEntityMeta(entityMetaData);
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
	 * Adds the packages from the packages sheet to the {@link org.molgenis.data.meta.MetaDataService}.
	 */
	private void importPackages(ParsedMetaData parsedMetaData)
	{
		parsedMetaData.getPackages().values().forEach(package_ -> {
			if (package_ != null) dataService.getMeta().addPackage(package_);
		});
	}

	/**
	 * Imports the tags from the tag sheet.
	 */
	// FIXME: can everybody always update a tag?
	private void importTags(RepositoryCollection source)
	{
		Repository<Entity> tagRepository = source.getRepository(EMX_TAGS);
		if (tagRepository != null)
		{
			for (Entity tagEntity : tagRepository)
			{
				Entity existingTag = dataService.findOneById(TAG, tagEntity.getString(EMX_TAG_IDENTIFIER));
				if (existingTag == null)
				{
					Tag tag = entityToTag(tagEntity.getString(EMX_TAG_IDENTIFIER), tagEntity);
					dataService.add(TAG, tag);
				}
				else
				{
					dataService.update(TAG, existingTag);
				}
			}
		}
	}

	/**
	 * Transforms an {@link Entity} to a {@link Tag}
	 *
	 * @param id
	 * @param tagEntity
	 * @return
	 */
	// FIXME: Duplicated with EmxMetaDataParser
	public Tag entityToTag(String id, Entity tagEntity)
	{
		Tag tag = tagFactory.create(id);
		tag.setObjectIri(tagEntity.getString(EMX_TAG_OBJECT_IRI));
		tag.setLabel(tagEntity.getString(EMX_TAG_LABEL));
		tag.setRelationLabel(tagEntity.getString(EMX_TAG_RELATION_LABEL));
		tag.setCodeSystem(tagEntity.getString(EMX_TAG_CODE_SYSTEM));
		tag.setRelationIri(tagEntity.getString(EMX_TAG_RELATION_IRI));

		return tag;
	}

	/**
	 * Updates a repository with entities.
	 *
	 * @param repo     the {@link Repository} to update
	 * @param entities the entities to
	 * @param dbAction {@link DatabaseAction} describing how to merge the existing entities
	 * @return number of updated entities
	 */
	private int update(Repository<Entity> repo, Iterable<Entity> entities, DatabaseAction dbAction)
	{
		if (entities == null) return 0;

		if (!molgenisPermissionService.hasPermissionOnEntity(repo.getName(), Permission.WRITE))
		{
			throw new MolgenisDataAccessException("No WRITE permission on entity '" + repo.getName()
					+ "'. Is this entity already imported by another user who did not grant you WRITE permission?");
		}
		String idAttributeName = repo.getEntityMetaData().getIdAttribute().getName();
		AttributeType dataType = repo.getEntityMetaData().getIdAttribute().getDataType();
		FieldType idFieldType = MolgenisFieldTypes.getType(AttributeType.getValueString(dataType));
		HugeSet<Object> existingIds = new HugeSet<>();
		HugeSet<Object> ids = new HugeSet<>();
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
					Query<Entity> q = new QueryImpl<>();
					Iterator<Object> it = ids.iterator();
					int batchCount = 0;
					while (it.hasNext())
					{
						Object id = it.next();
						q.eq(idAttributeName, id);
						batchCount++;
						if (batchCount == batchSize || !it.hasNext())
						{
							repo.findAll(q).forEach(existing -> existingIds.add(existing.getIdValue()));
							q = new QueryImpl<>();
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

					count = repo.add(stream(entities.spliterator(), false));
					break;
				case ADD_IGNORE_EXISTING:
					int batchSize = 1000;
					List<Entity> existingEntities;
					List<Entity> newEntities = newArrayList();

					Iterator<? extends Entity> it = entities.iterator();
					while (it.hasNext())
					{
						Entity entity = it.next();
						count++;
						Object id = idFieldType.convert(entity.get(idAttributeName));
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
					existingEntities = new ArrayList<>(batchSize);
					List<Integer> existingEntitiesRowIndex = new ArrayList<>(batchSize);
					newEntities = new ArrayList<>(batchSize);
					List<Integer> newEntitiesRowIndex = new ArrayList<>(batchSize);

					it = entities.iterator();
					while (it.hasNext())
					{
						Entity entity = it.next();
						count++;
						Object id = idFieldType.convert(entity.get(idAttributeName));
						if (existingIds.contains(id))
						{
							existingEntitiesRowIndex.add(count);
							existingEntities.add(entity);
							if (existingEntities.size() == batchSize)
							{
								updateInRepo(repo, existingEntities, existingEntitiesRowIndex);
							}
						}
						else
						{
							newEntitiesRowIndex.add(count);
							newEntities.add(entity);
							if (newEntities.size() == batchSize)
							{
								insertIntoRepo(repo, newEntities, newEntitiesRowIndex);
							}
						}
					}

					if (!existingEntities.isEmpty())
					{
						updateInRepo(repo, existingEntities, existingEntitiesRowIndex);
					}
					if (!newEntities.isEmpty())
					{
						insertIntoRepo(repo, newEntities, newEntitiesRowIndex);
					}
					break;
				case UPDATE:
					int errorCount = 0;
					StringBuilder msg = new StringBuilder();
					msg.append("Trying to update non-existing ").append(repo.getName()).append(" entities:");

					for (Entity entity : entities)
					{
						count++;
						Object id = idFieldType.convert(entity.get(idAttributeName));
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
					repo.update(stream(entities.spliterator(), false));
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

	private void updateInRepo(Repository<Entity> repo, List<Entity> existingEntities,
			List<Integer> existingEntitiesRowIndex)
	{
		try
		{
			repo.update(existingEntities.stream());
		}
		catch (MolgenisValidationException mve)
		{
			mve.renumberViolationRowIndices(existingEntitiesRowIndex);
			throw mve;
		}
		existingEntities.clear();
		existingEntitiesRowIndex.clear();
	}

	private void insertIntoRepo(Repository<Entity> repo, List<Entity> newEntities, List<Integer> newEntitiesRowIndex)
	{
		try
		{
			repo.add(newEntities.stream());
		}
		catch (MolgenisValidationException mve)
		{
			mve.renumberViolationRowIndices(newEntitiesRowIndex);
			throw mve;
		}
		newEntities.clear();
		newEntitiesRowIndex.clear();
	}

	/**
	 * A wrapper for a to import entity
	 * <p>
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
		public void setIdValue(Object id)
		{
			AttributeMetaData idAttr = entityMetaData.getIdAttribute();
			if (idAttr == null)
			{
				throw new IllegalArgumentException(
						format("Entity [%s] doesn't have an id attribute", entityMetaData.getName()));
			}
			set(idAttr.getName(), id);
		}

		@Override
		public Object getLabelValue()
		{
			return getString(entityMetaData.getLabelAttribute().getName());
		}

		@Override
		public Object get(String attributeName)
		{
			AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
			if (attribute == null) throw new UnknownAttributeException(attributeName);

			AttributeType dataType = attribute.getDataType();
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
				if (attribute.getDataType() == XREF)
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

		public List<String> getList(String attributeName)
		{
			return DataConverter.toList(entity.get(attributeName));
		}

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

				AttributeType dataType = attribute.getDataType();
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
		 * Returns lazy references if the decorated entity doesn't have self-references improving import performance.
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

			if (!isSingleReferenceType(attribute))
			{
				throw new MolgenisDataException(
						"can't use getEntity() on something that's not an xref, categorical or file");
			}

			FieldType dataType = MolgenisFieldTypes.getType(AttributeType.getValueString(attribute.getDataType()));
			value = dataType.convert(value);

			// referenced entity id value must match referenced entity id attribute data type
			AttributeMetaData refIdAttr = attribute.getRefEntity().getIdAttribute();
			if (isSingleReferenceType(refIdAttr) && !(value instanceof String))
			{
				value = String.valueOf(value);
			}
			else if (refIdAttr.getDataType() == INT && !(value instanceof Integer))
			{
				value = Integer.valueOf(value.toString());
			}

			Entity refEntity;
			if (selfReferencing)
			{
				refEntity = dataService.findOneById(attribute.getRefEntity().getName(), value);
				if (refEntity == null) throw new UnknownEntityException(
						attribute.getRefEntity().getName() + " with " + attribute.getRefEntity().getIdAttribute()
								.getName() + " [" + value + "] does not exist");
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
			throw new UnsupportedOperationException("FIXME"); // FIXME
			//			Entity entity = getEntity(attributeName);
			//			return entity != null
			//					? new ConvertingIterable<E>(clazz, Arrays.asList(entity)).iterator().next() : null;
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
		 * Returns lazy references if the decorated entity doesn't have self-references improving import performance.
		 *
		 * @param attributeName
		 * @return
		 */
		private Iterable<Entity> getEntitiesLikeDefaultEntity(String attributeName)
		{
			AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
			if (attribute == null) throw new UnknownAttributeException(attributeName);

			// FIXME this should fail on anything other than instanceof MrefField. requires an extensive code base review to find illegal use of getEntities()
			if (!EntityMetaDataUtils.isReferenceType(attribute))
			{
				throw new MolgenisDataException(
						"can't use getEntities() on something that's not an xref, mref, categorical, categorical_mref or file");
			}

			Iterable<?> ids;

			Object value = entity.get(attributeName);
			if (value instanceof String)
			{
				List<String> values = DataConverter.toList(value);
				// FIXME remove dependency on application context
				return getApplicationContext().getBean(EntityManager.class)
						.getReferences(attribute.getRefEntity(), values);
			}
			else if (value instanceof Entity) return Collections.singletonList((Entity) value);
			else ids = (Iterable<?>) value;

			if ((ids == null) || !ids.iterator().hasNext()) return Collections.emptyList();

			Object firstItem = ids.iterator().next();
			if (firstItem instanceof Entity) return (Iterable<Entity>) ids;

			if (selfReferencing)
			{
				FieldType dataType = MolgenisFieldTypes.getType(AttributeType.getValueString(attribute.getDataType()));
				return from(ids).transform(dataType::convert).transform(
						convertedId -> (dataService.findOneById(attribute.getRefEntity().getName(), convertedId)));
			}
			else
			{
				EntityMetaData refEntityMeta = attribute.getRefEntity();
				return () -> stream(ids.spliterator(), false).map(id -> {
					// referenced entity id value must match referenced entity id attribute data type
					if (EntityMetaDataUtils.isStringType(refEntityMeta.getIdAttribute()) && !(id instanceof String))
					{
						return String.valueOf(id);
					}
					else if (refEntityMeta.getIdAttribute().getDataType() == INT && !(id instanceof Integer))
					{
						return Integer.valueOf(id.toString());
					}
					else
					{
						return id;
					}
				}).<Entity>map(id -> new LazyEntity(refEntityMeta, dataService, id)).iterator();
			}
		}

		@Override
		public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
		{
			throw new UnsupportedOperationException("FIXME"); // FIXME
			//			Iterable<Entity> entities = getEntities(attributeName);
			//			return entities != null ? new ConvertingIterable<E>(clazz, entities) : null;
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
			Object labelValue = getLabelValue();
			return labelValue != null ? labelValue.toString() : null;
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