package org.molgenis.data.meta;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.persist.PackagePersister;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.MetaUtils.getEntityTypeFetch;
import static org.molgenis.data.meta.model.AttributeMetadata.*;
import static org.molgenis.data.meta.model.EntityTypeMetadata.*;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.meta.model.PackageMetadata.PARENT;
import static org.molgenis.data.meta.model.TagMetadata.TAG;

/**
 * Meta data service for retrieving and editing meta data.
 */
@Component
public class MetaDataServiceImpl implements MetaDataService
{
	private static final Logger LOG = LoggerFactory.getLogger(MetaDataServiceImpl.class);

	private final DataService dataService;
	private final RepositoryCollectionRegistry repoCollectionRegistry;
	private final SystemEntityTypeRegistry systemEntityTypeRegistry;
	private final EntityTypeDependencyResolver entityTypeDependencyResolver;
	private final PackagePersister packagePersister;

	public MetaDataServiceImpl(DataService dataService, RepositoryCollectionRegistry repoCollectionRegistry,
			SystemEntityTypeRegistry systemEntityTypeRegistry,
			EntityTypeDependencyResolver entityTypeDependencyResolver, PackagePersister packagePersister)
	{
		this.dataService = requireNonNull(dataService);
		this.repoCollectionRegistry = requireNonNull(repoCollectionRegistry);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
		this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
		this.packagePersister = requireNonNull(packagePersister);
	}

	@Override
	public Repository<Entity> getRepository(String entityTypeId)
	{
		EntityType entityType = getEntityType(entityTypeId);
		if (entityType == null)
		{
			throw new UnknownEntityTypeException(entityTypeId);
		}
		return !entityType.isAbstract() ? getRepository(entityType) : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> Repository<E> getRepository(String entityTypeId, Class<E> entityClass)
	{
		return (Repository<E>) getRepository(entityTypeId);
	}

	@Override
	public Repository<Entity> getRepository(EntityType entityType)
	{
		if (!entityType.isAbstract())
		{
			String backendName = entityType.getBackend();
			RepositoryCollection backend = getBackend(backendName);
			return backend.getRepository(entityType);
		}
		else
		{
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> Repository<E> getRepository(EntityType entityType, Class<E> entityClass)
	{
		return (Repository<E>) getRepository(entityType);
	}

	@Override
	public boolean hasRepository(String entityTypeId)
	{
		SystemEntityType systemEntityType = systemEntityTypeRegistry.getSystemEntityType(entityTypeId);
		if (systemEntityType != null)
		{
			return !systemEntityType.isAbstract();
		}
		else
		{
			return dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)
							  .eq(EntityTypeMetadata.ID, entityTypeId)
							  .and()
							  .eq(IS_ABSTRACT, false)
							  .findOne() != null;
		}
	}

	@Override
	public Repository<Entity> createRepository(EntityType entityType)
	{
		if (entityType.isAbstract())
		{
			throw new MolgenisDataException(
					format("Can't create repository for abstract entity [%s]", entityType.getId()));
		}
		addEntityType(entityType);
		return getRepository(entityType);
	}

	@Override
	public <E extends Entity> Repository<E> createRepository(EntityType entityType, Class<E> entityClass)
	{
		if (entityType.isAbstract())
		{
			throw new MolgenisDataException(
					format("Can't create repository for abstract entity [%s]", entityType.getId()));
		}
		addEntityType(entityType);
		return getRepository(entityType, entityClass);
	}

	@Override
	public RepositoryCollection getDefaultBackend()
	{
		return repoCollectionRegistry.getDefaultRepoCollection();
	}

	@Override
	public RepositoryCollection getBackend(String backendName)
	{
		return repoCollectionRegistry.getRepositoryCollection(backendName);
	}

	@Transactional
	@Override
	public void deleteEntityType(String entityTypeId)
	{
		dataService.deleteById(ENTITY_TYPE_META_DATA, entityTypeId);

		LOG.info("Removed entity [{}]", entityTypeId);
	}

	@Transactional
	@Override
	public void deleteEntityType(Collection<EntityType> entityTypes)
	{
		if (entityTypes.isEmpty())
		{
			return;
		}

		dataService.delete(ENTITY_TYPE_META_DATA, entityTypes.stream());

		LOG.info("Removed entities [{}]", entityTypes.stream().map(EntityType::getId).collect(joining(",")));
	}

	@Transactional
	@Override
	public void deleteAttributeById(Object id)
	{
		Attribute attribute = dataService.findOneById(ATTRIBUTE_META_DATA, id, Attribute.class);
		EntityType entityType = attribute.getEntity();

		// Update repository state
		entityType.removeAttribute(attribute);

		// Update repository state
		dataService.update(ENTITY_TYPE_META_DATA, entityType);

		// Update administration
		dataService.delete(ATTRIBUTE_META_DATA, attribute);
	}

	@Override
	public RepositoryCollection getBackend(EntityType entityType)
	{
		String backendName = entityType.getBackend() == null ? getDefaultBackend().getName() : entityType.getBackend();
		RepositoryCollection backend = repoCollectionRegistry.getRepositoryCollection(backendName);
		if (backend == null) throw new RuntimeException(format("Unknown backend [%s]", backendName));

		return backend;
	}

	@Transactional
	@Override
	public void addEntityType(EntityType entityType)
	{
		// create entity
		dataService.add(ENTITY_TYPE_META_DATA, entityType);

		// create attributes
		Stream<Attribute> attrs = stream(entityType.getOwnAllAttributes().spliterator(), false);
		dataService.add(ATTRIBUTE_META_DATA, attrs);
	}

	@Transactional
	@Override
	public void updateEntityType(EntityType entityType)
	{
		EntityType existingEntityType = dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)
												   .eq(EntityTypeMetadata.ID, entityType.getId())
												   .fetch(getEntityTypeFetch())
												   .findOne();
		if (existingEntityType == null)
		{
			throw new UnknownEntityTypeException(entityType.getId());
		}

		updateEntityType(entityType, existingEntityType);
	}

	/**
	 * Returns true if entity meta contains mapped by attributes that do not exist in the existing entity meta.
	 *
	 * @param entityType         entity meta data
	 * @param existingEntityType existing entity meta data
	 * @return true if entity meta contains mapped by attributes that do not exist in the existing entity meta.
	 */
	private static boolean hasNewMappedByAttrs(EntityType entityType, EntityType existingEntityType)
	{
		Set<String> mappedByAttrs = entityType.getOwnMappedByAttributes().map(Attribute::getName).collect(toSet());

		Set<String> existingMappedByAttrs = existingEntityType.getOwnMappedByAttributes()
															  .map(Attribute::getName)
															  .collect(toSet());
		return !mappedByAttrs.equals(existingMappedByAttrs);
	}

	@Transactional
	@Override
	public void upsertEntityTypes(Collection<EntityType> entityTypes)
	{
		if (entityTypes.isEmpty())
		{
			return;
		}

		List<EntityType> resolvedEntityTypes = entityTypeDependencyResolver.resolve(entityTypes);

		Map<String, EntityType> existingEntityTypeMap = getExistingEntityTypeMap(entityTypes);
		upsertEntityTypesSkipMappedByAttributes(resolvedEntityTypes, existingEntityTypeMap);
		addMappedByAttributes(resolvedEntityTypes, existingEntityTypeMap);
	}

	private Map<String, EntityType> getExistingEntityTypeMap(Collection<EntityType> entityTypes)
	{
		Map<String, EntityType> existingEntityTypeMap = new HashMap<>();
		entityTypes.forEach(entityType ->
		{
			String entityId = entityType.getId();
			if (entityId != null)
			{
				EntityType existingEntityType = dataService.findOneById(ENTITY_TYPE_META_DATA, entityId,
						EntityType.class);

				if (existingEntityType != null)
				{
					existingEntityTypeMap.put(entityType.getId(), existingEntityType);
				}
			}
		});
		return existingEntityTypeMap;
	}

	private void addMappedByAttributes(List<EntityType> resolvedEntityTypes,
			Map<String, EntityType> existingEntityTypeMap)
	{
		// 2nd pass: create mappedBy attributes and update entity
		resolvedEntityTypes.forEach(entityType ->
		{
			EntityType existingEntityType = existingEntityTypeMap.get(entityType.getId());
			if (existingEntityType == null)
			{
				if (entityType.hasMappedByAttributes())
				{
					updateEntityType(entityType, new EntityTypeWithoutMappedByAttributes(entityType));
				}
			}
			else
			{
				if (hasNewMappedByAttrs(entityType, existingEntityType))
				{
					updateEntityType(entityType, existingEntityType);
				}
			}
		});
	}

	private void upsertEntityTypesSkipMappedByAttributes(List<EntityType> resolvedEntityType,
			Map<String, EntityType> existingEntityTypeMap)
	{
		// 1st pass: create entities and attributes except for mappedBy attributes
		resolvedEntityType.forEach(entityType ->
		{
			EntityType existingEntityType = existingEntityTypeMap.get(entityType.getId());
			if (existingEntityType == null)
			{
				if (entityType.hasMappedByAttributes())
				{
					entityType = new EntityTypeWithoutMappedByAttributes(entityType);
				}

				addEntityType(entityType);
			}
			else
			{
				if (hasNewMappedByAttrs(entityType, existingEntityType))
				{
					entityType = new EntityTypeWithoutMappedByAttributes(entityType, existingEntityType);
				}

				updateEntityType(entityType, existingEntityType);
			}
		});
	}

	private void updateEntityType(EntityType entityType, EntityType existingEntityType)
	{
		// update entity
		if (!EntityUtils.equals(entityType, existingEntityType))
		{
			// note: leave it up to the data service to decided what to do with attributes removed from entity meta data
			dataService.update(ENTITY_TYPE_META_DATA, entityType);
		}
		// add new attributes, update modified attributes
		upsertAttributes(entityType, existingEntityType);
	}

	@Transactional
	@Override
	public void addAttribute(Attribute attr)
	{
		EntityType entityType = dataService.getEntityType(attr.getEntity().getId());
		entityType.addAttribute(attr);

		// Update repository state
		dataService.update(ENTITY_TYPE_META_DATA, entityType);

		// Update administration
		dataService.add(ATTRIBUTE_META_DATA, attr);
	}

	@Transactional
	@Override
	public void addAttributes(String entityTypeId, Stream<Attribute> attrs)
	{
		EntityType entityType = dataService.getEntityType(entityTypeId);
		List<Attribute> attributes = attrs.collect(toList());
		entityType.addAttributes(attributes);

		// Update repository state
		dataService.update(ENTITY_TYPE_META_DATA, entityType);

		// Update administration
		dataService.add(ATTRIBUTE_META_DATA, attributes.stream());
	}

	@Override
	public boolean hasEntityType(String entityTypeId)
	{
		return systemEntityTypeRegistry.hasSystemEntityType(entityTypeId)
				|| getEntityTypeBypassingRegistry(entityTypeId) != null;
	}

	@Override
	public EntityType getEntityType(String entityTypeId)
	{
		EntityType systemEntity = systemEntityTypeRegistry.getSystemEntityType(entityTypeId);
		if (systemEntity != null)
		{
			return systemEntity;
		}
		else
		{
			return getEntityTypeBypassingRegistry(entityTypeId);
		}
	}

	@Override
	public EntityType getEntityTypeById(String entityTypeId)
	{
		EntityType systemEntity = systemEntityTypeRegistry.getSystemEntityType(entityTypeId);
		if (systemEntity != null)
		{
			return systemEntity;
		}
		else
		{
			return getEntityTypeBypassingRegistry(entityTypeId);
		}
	}

	@Transactional
	@Override
	public void addPackage(Package package_)
	{
		dataService.add(PACKAGE, package_);
	}

	@Transactional
	@Override
	public void upsertPackages(Stream<Package> packages)
	{
		packagePersister.upsertPackages(packages);
	}

	@Override
	public Package getPackage(String fullyQualifiedPackageName)
	{
		return dataService.findOneById(PACKAGE, fullyQualifiedPackageName, Package.class);
	}

	@Override
	public List<Package> getPackages()
	{
		return dataService.findAll(PACKAGE, Package.class).collect(toList());
	}

	@Override
	public List<Package> getRootPackages()
	{
		return dataService.query(PACKAGE, Package.class).eq(PARENT, null).findAll().collect(toList());
	}

	@Transactional
	@Override
	public void upsertTags(Collection<Tag> tags)
	{
		// TODO replace with dataService.upsert once available in Repository
		tags.forEach(tag ->
		{
			Tag existingTag = dataService.findOneById(TAG, tag.getId(), Tag.class);
			if (existingTag == null)
			{
				dataService.add(TAG, tag);
			}
			else
			{
				dataService.update(TAG, tag);
			}
		});
	}

	@Override
	public Stream<EntityType> getEntityTypes()
	{
		List<EntityType> entityTypeList = newArrayList();
		Fetch entityTypeFetch = getEntityTypeFetch();

		// Fetch the entitytypes page by page so that the results can be cached
		final int pageSize = 1000;
		for (int page = 0; entityTypeList.size() == page * pageSize; page++)
		{
			QueryImpl<EntityType> query = new QueryImpl<>();
			query.setFetch(entityTypeFetch);
			query.setPageSize(pageSize);
			query.setOffset(page * pageSize);
			dataService.findAll(ENTITY_TYPE_META_DATA, query, EntityType.class).forEach(entityTypeList::add);
		}

		return entityTypeList.stream();
	}

	@Override
	public Stream<Repository<Entity>> getRepositories()
	{
		return dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)
						  .eq(IS_ABSTRACT, false)
						  .fetch(getEntityTypeFetch())
						  .findAll()
						  .map(this::getRepository);
	}

	/**
	 * Add and update entity attributes
	 *
	 * @param entityType         entity meta data
	 * @param existingEntityType existing entity meta data
	 */
	private void upsertAttributes(EntityType entityType, EntityType existingEntityType)
	{
		// analyze both compound and atomic attributes owned by the entity
		Map<String, Attribute> attrsMap = stream(entityType.getOwnAllAttributes().spliterator(), false).collect(
				toMap(Attribute::getName, Function.identity()));
		Map<String, Attribute> existingAttrsMap = stream(existingEntityType.getOwnAllAttributes().spliterator(),
				false).collect(toMap(Attribute::getName, Function.identity()));

		// determine attributes to add, update and delete
		Set<String> addedAttrNames = Sets.difference(attrsMap.keySet(), existingAttrsMap.keySet());
		Set<String> sharedAttrNames = Sets.intersection(attrsMap.keySet(), existingAttrsMap.keySet());
		Set<String> deletedAttrNames = Sets.difference(existingAttrsMap.keySet(), attrsMap.keySet());

		// add new attributes
		if (!addedAttrNames.isEmpty())
		{
			dataService.add(ATTRIBUTE_META_DATA, addedAttrNames.stream().map(attrsMap::get));
		}

		// update changed attributes
		List<String> updatedAttrNames = sharedAttrNames.stream()
													   .filter(attrName -> !EntityUtils.equals(attrsMap.get(attrName),
															   existingAttrsMap.get(attrName)))
													   .collect(toList());
		if (!updatedAttrNames.isEmpty())
		{
			dataService.update(ATTRIBUTE_META_DATA, updatedAttrNames.stream().map(attrsMap::get));
		}

		// delete removed attributes
		if (!deletedAttrNames.isEmpty())
		{
			dataService.delete(ATTRIBUTE_META_DATA, deletedAttrNames.stream().map(existingAttrsMap::get));
		}
	}

	@Override
	public Iterator<RepositoryCollection> iterator()
	{
		return repoCollectionRegistry.getRepositoryCollections().iterator();
	}

	@Override
	public LinkedHashMap<String, Boolean> determineImportableEntities(RepositoryCollection repositoryCollection)
	{
		LinkedHashMap<String, Boolean> entitiesImportable = Maps.newLinkedHashMap();
		stream(repositoryCollection.getEntityTypeIds().spliterator(), false).forEach(id -> entitiesImportable.put(id,
				this.isEntityTypeCompatible(repositoryCollection.getRepository(id).getEntityType())));

		return entitiesImportable;
	}

	@Override
	public boolean isEntityTypeCompatible(EntityType newEntityType)
	{
		String newEntityTypeId = newEntityType.getId();
		if (dataService.hasRepository(newEntityTypeId))
		{
			EntityType oldEntityType = dataService.getEntityType(newEntityTypeId);
			List<Attribute> oldAtomicAttributes = stream(oldEntityType.getAtomicAttributes().spliterator(),
					false).collect(toList());

			LinkedHashMap<String, Attribute> newAtomicAttributesMap = newLinkedHashMap();
			stream(newEntityType.getAtomicAttributes().spliterator(), false).forEach(
					attribute -> newAtomicAttributesMap.put(attribute.getName(), attribute));

			for (Attribute oldAttribute : oldAtomicAttributes)
			{
				if (!newAtomicAttributesMap.keySet().contains(oldAttribute.getName())) return false;
				// FIXME This implies that an attribute can never be different when doing an update import?
				if (!EntityUtils.equals(oldAttribute, newAtomicAttributesMap.get(oldAttribute.getName()), false))
					return false;
			}
		}
		return true;
	}

	@Override
	public boolean hasBackend(String backendName)
	{
		return repoCollectionRegistry.hasRepositoryCollection(backendName);
	}

	@Override
	public Stream<EntityType> getConcreteChildren(EntityType entityType)
	{
		if (!entityType.isAbstract())
		{
			return Stream.of(entityType);
		}
		return dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)
						  .eq(EXTENDS, entityType)
						  .findAll()
						  .flatMap(this::getConcreteChildren);
	}

	@Override
	public EntityType getEntityTypeBypassingRegistry(String entityTypeId)
	{
		return entityTypeId != null ? dataService.findOneById(ENTITY_TYPE_META_DATA, entityTypeId, getEntityTypeFetch(),
				EntityType.class) : null;
	}

	@Override
	public Stream<Attribute> getReferringAttributes(String entityTypeId)
	{
		return dataService.findAll(ATTRIBUTE_META_DATA, QueryImpl.EQ(REF_ENTITY_TYPE, entityTypeId), Attribute.class);
	}

	/**
	 * Entity meta data that wraps a entity meta data and hides the mappedBy attributes. In code both a new and an existing
	 * entity meta data are provided only the new mappedBy attributes are hidden.
	 */
	public static class EntityTypeWithoutMappedByAttributes extends EntityType
	{
		private final EntityType entityType;
		private final EntityType existingEntityType;

		public EntityTypeWithoutMappedByAttributes(EntityType entityType)
		{
			this(entityType, null);
		}

		public EntityTypeWithoutMappedByAttributes(EntityType entityType, EntityType existingEntityType)
		{
			this.entityType = requireNonNull(entityType);
			this.existingEntityType = existingEntityType;
		}

		@Override
		public void init(Entity entity)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Object get(String attributeName)
		{
			return entityType.get(attributeName);
		}

		@Override
		public Boolean getBoolean(String attributeName)
		{
			return entityType.getBoolean(attributeName);
		}

		@Override
		public Double getDouble(String attributeName)
		{
			return entityType.getDouble(attributeName);
		}

		@Override
		public Iterable<Entity> getEntities(String attributeName)
		{
			Iterable<Entity> entities = entityType.getEntities(attributeName);
			if (attributeName.equals(ATTRIBUTES))
			{
				return () -> stream(entities.spliterator(), false).filter(entity ->
				{
					if (existingEntityType != null)
					{
						return entity.getEntity(MAPPED_BY) == null
								|| existingEntityType.getAttribute(entity.getString(AttributeMetadata.NAME)) != null;
					}
					else
					{
						return entity.getEntity(MAPPED_BY) == null;
					}
				}).iterator();
			}
			return entities;
		}

		@Override
		public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
		{
			return entityType.getEntities(attributeName, clazz);
		}

		@Override
		public Entity getEntity(String attributeName)
		{
			return entityType.getEntity(attributeName);
		}

		@Override
		public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
		{
			return entityType.getEntity(attributeName, clazz);
		}

		@Override
		public EntityType getEntityType()
		{
			return entityType.getEntityType();
		}

		@Override
		public Object getIdValue()
		{
			return entityType.getIdValue();
		}

		@Override
		public Integer getInt(String attributeName)
		{
			return entityType.getInt(attributeName);
		}

		@Override
		public Object getLabelValue()
		{
			return entityType.getLabelValue();
		}

		@Override
		public Long getLong(String attributeName)
		{
			return entityType.getLong(attributeName);
		}

		@Override
		public String getString(String attributeName)
		{
			return entityType.getString(attributeName);
		}

		@Override
		public LocalDate getLocalDate(String attributeName)
		{
			return entityType.getLocalDate(attributeName);
		}

		@Override
		public Instant getInstant(String attributeName)
		{
			return entityType.getInstant(attributeName);
		}

		@Override
		public void set(Entity values)
		{
			entityType.set(values);
		}

		@Override
		public void setIdValue(Object id)
		{
			entityType.setIdValue(id);
		}

		@Override
		public Iterable<String> getAttributeNames()
		{
			return entityType.getAttributeNames();
		}

		@Override
		public String getId()
		{
			return entityType.getId();
		}

		@Override
		public EntityType setId(String id)
		{
			return entityType.setId(id);
		}

		@Override
		public String getLabel()
		{
			return entityType.getLabel();
		}

		@Override
		public String getLabel(String languageCode)
		{
			return entityType.getLabel(languageCode);
		}

		@Override
		public EntityType setLabel(String label)
		{
			return entityType.setLabel(label);
		}

		@Override
		public EntityType setLabel(String languageCode, String label)
		{
			return entityType.setLabel(languageCode, label);
		}

		@Override
		public String getDescription()
		{
			return entityType.getDescription();
		}

		@Override
		public String getDescription(String languageCode)
		{
			return entityType.getDescription(languageCode);
		}

		@Override
		public EntityType setDescription(String description)
		{
			return entityType.setDescription(description);
		}

		@Override
		public EntityType setDescription(String languageCode, String description)
		{
			return entityType.setDescription(languageCode, description);
		}

		@Override
		public String getBackend()
		{
			return entityType.getBackend();
		}

		@Override
		public EntityType setBackend(String backend)
		{
			return entityType.setBackend(backend);
		}

		@Override
		public Package getPackage()
		{
			return entityType.getPackage();
		}

		@Override
		public EntityType setPackage(Package package_)
		{
			return entityType.setPackage(package_);
		}

		@Override
		public Attribute getIdAttribute()
		{
			return entityType.getIdAttribute();
		}

		@Override
		public Attribute getOwnIdAttribute()
		{
			return entityType.getOwnIdAttribute();
		}

		@Override
		public Attribute getLabelAttribute()
		{
			return entityType.getLabelAttribute();
		}

		@Override
		public Attribute getLabelAttribute(String langCode)
		{
			return entityType.getLabelAttribute(langCode);
		}

		@Override
		public Attribute getOwnLabelAttribute()
		{
			return entityType.getOwnLabelAttribute();
		}

		@Override
		public Attribute getOwnLabelAttribute(String languageCode)
		{
			return entityType.getOwnLabelAttribute(languageCode);
		}

		@Override
		public Attribute getLookupAttribute(String lookupAttrName)
		{
			return entityType.getLookupAttribute(lookupAttrName);
		}

		@Override
		public Iterable<Attribute> getLookupAttributes()
		{
			return entityType.getLookupAttributes();
		}

		@Override
		public Iterable<Attribute> getOwnLookupAttributes()
		{
			return entityType.getOwnLookupAttributes();
		}

		@Override
		public boolean isAbstract()
		{
			return entityType.isAbstract();
		}

		@Override
		public EntityType setAbstract(boolean abstract_)
		{
			return entityType.setAbstract(abstract_);
		}

		@Override
		public EntityType getExtends()
		{
			return entityType.getExtends();
		}

		@Override
		public EntityType setExtends(EntityType extends_)
		{
			return entityType.setExtends(extends_);
		}

		@Override
		public Iterable<Attribute> getOwnAttributes()
		{
			// FIXME mappedBy attribute in compound not removed
			return () -> stream(entityType.getOwnAttributes().spliterator(), false).filter(attr ->
			{
				if (existingEntityType != null)
				{
					return !attr.isMappedBy() || existingEntityType.getAttribute(attr.getName()) != null;
				}
				else
				{
					return !attr.isMappedBy();
				}
			}).iterator();
		}

		@Override
		public EntityType setOwnAllAttributes(Iterable<Attribute> attrs)
		{
			return entityType.setOwnAllAttributes(attrs);
		}

		@Override
		public Iterable<Attribute> getAttributes()
		{
			return entityType.getAttributes();
		}

		@Override
		public Iterable<Attribute> getAtomicAttributes()
		{
			return entityType.getAtomicAttributes();
		}

		@Override
		public Iterable<Attribute> getAllAttributes()
		{
			return entityType.getAllAttributes();
		}

		@Override
		public Iterable<Attribute> getOwnAllAttributes()
		{
			return () -> stream(entityType.getOwnAllAttributes().spliterator(), false).filter(attr ->
			{
				if (existingEntityType != null)
				{
					return !attr.isMappedBy() || existingEntityType.getAttribute(attr.getName()) != null;
				}
				else
				{
					return !attr.isMappedBy();
				}
			}).iterator();
		}

		@Override
		public Attribute getAttribute(String attrName)
		{
			return entityType.getAttribute(attrName);
		}

		@Override
		public EntityType addAttribute(Attribute attr, AttributeRole... attrTypes)
		{
			return entityType.addAttribute(attr, attrTypes);
		}

		@Override
		public void addAttributes(Iterable<Attribute> attrs)
		{
			entityType.addAttributes(attrs);
		}

		@Override
		public void setAttributeRoles(Attribute attr, AttributeRole... attrTypes)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasAttributeWithExpression()
		{
			return entityType.hasAttributeWithExpression();
		}

		@Override
		public void removeAttribute(Attribute attr)
		{
			entityType.removeAttribute(attr);
		}

		@Override
		public Iterable<Tag> getTags()
		{
			return entityType.getTags();
		}

		@Override
		public EntityType setTags(Iterable<Tag> tags)
		{
			return entityType.setTags(tags);
		}

		@Override
		public void addTag(Tag tag)
		{
			entityType.addTag(tag);
		}

		@Override
		public void removeTag(Tag tag)
		{
			entityType.removeTag(tag);
		}

		@Override
		public Iterable<Attribute> getOwnAtomicAttributes()
		{
			return entityType.getOwnAtomicAttributes();
		}

		@Override
		public boolean hasBidirectionalAttributes()
		{
			return entityType.hasBidirectionalAttributes();
		}

		@Override
		public boolean hasMappedByAttributes()
		{
			return entityType.hasMappedByAttributes();
		}

		@Override
		public Stream<Attribute> getOwnMappedByAttributes()
		{
			return entityType.getOwnMappedByAttributes();
		}

		@Override
		public Stream<Attribute> getMappedByAttributes()
		{
			return entityType.getMappedByAttributes();
		}

		@Override
		public boolean hasInversedByAttributes()
		{
			return entityType.hasInversedByAttributes();
		}

		@Override
		public Stream<Attribute> getInversedByAttributes()
		{
			return entityType.getInversedByAttributes();
		}

		@Override
		public void set(String attributeName, Object value)
		{
			entityType.set(attributeName, value);
		}

		@Override
		public void setDefaultValues()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString()
		{
			return entityType.toString();
		}
	}
}
