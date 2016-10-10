package org.molgenis.data.meta;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.MetaUtils.getEntityTypeFetch;
import static org.molgenis.data.meta.model.AttributeMetadata.*;
import static org.molgenis.data.meta.model.AttributeMetadata.MAPPED_BY;
import static org.molgenis.data.meta.model.EntityTypeMetadata.*;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.meta.model.PackageMetadata.PARENT;
import static org.molgenis.data.meta.model.TagMetaData.TAG;

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

	@Autowired
	public MetaDataServiceImpl(DataService dataService, RepositoryCollectionRegistry repoCollectionRegistry,
			SystemEntityTypeRegistry systemEntityTypeRegistry,
			EntityTypeDependencyResolver entityTypeDependencyResolver)
	{
		this.dataService = requireNonNull(dataService);
		this.repoCollectionRegistry = requireNonNull(repoCollectionRegistry);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
		this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
	}

	@Override
	public Repository<Entity> getRepository(String entityName)
	{
		EntityType entityType = getEntityType(entityName);
		if (entityType == null)
		{
			throw new UnknownEntityException(format("Unknown entity [%s]", entityName));
		}
		return !entityType.isAbstract() ? getRepository(entityType) : null;
	}

	@Override
	public <E extends Entity> Repository<E> getRepository(String entityName, Class<E> entityClass)
	{
		//noinspection unchecked
		return (Repository<E>) getRepository(entityName);
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

	@Override
	public <E extends Entity> Repository<E> getRepository(EntityType entityType, Class<E> entityClass)
	{
		//noinspection unchecked
		return (Repository<E>) getRepository(entityType);
	}

	@Override
	public boolean hasRepository(String entityName)
	{
		SystemEntityType systemEntityType = systemEntityTypeRegistry.getSystemEntityType(entityName);
		if (systemEntityType != null)
		{
			return !systemEntityType.isAbstract();
		}
		else
		{
			return dataService.query(ENTITY_TYPE_META_DATA, EntityType.class).eq(FULL_NAME, entityName).and()
					.eq(IS_ABSTRACT, false).findOne() != null;
		}
	}

	@Override
	public Repository<Entity> createRepository(EntityType entityType)
	{
		if (entityType.isAbstract())
		{
			throw new MolgenisDataException(
					format("Can't create repository for abstract entity [%s]", entityType.getName()));
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
					format("Can't create repository for abstract entity [%s]", entityType.getName()));
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
	public void deleteEntityType(String entityName)
	{
		dataService.deleteById(ENTITY_TYPE_META_DATA, entityName);

		LOG.info("Removed entity [{}]", entityName);
	}

	@Transactional
	@Override
	public void deleteEntityType(Collection<EntityType> entityTypes)
	{
		if (entityTypes.isEmpty())
		{
			return;
		}

		List<EntityType> resolvedEntityTypes = reverse(entityTypeDependencyResolver.resolve(entityTypes));

		// 1st pass: remove mappedBy attributes
		List<EntityType> mappedByEntityTypes = resolvedEntityTypes.stream().filter(EntityType::hasMappedByAttributes)
				.map(EntityTypeWithoutMappedByAttributes::new).collect(toList());
		if (!mappedByEntityTypes.isEmpty())
		{
			dataService.update(ENTITY_TYPE_META_DATA, mappedByEntityTypes.stream());
		}

		// 2nd pass: delete entities
		dataService.deleteAll(ENTITY_TYPE_META_DATA, resolvedEntityTypes.stream().map(EntityType::getName));

		LOG.info("Removed entities [{}]", entityTypes.stream().map(EntityType::getName).collect(joining(",")));
	}

	@Transactional
	@Override
	public void deleteAttributeById(Object id)
	{
		dataService.deleteById(ATTRIBUTE_META_DATA, id);
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
		// create attributes
		Stream<Attribute> attrs = stream(entityType.getOwnAllAttributes().spliterator(), false);
		dataService.add(ATTRIBUTE_META_DATA, attrs);

		// create entity
		dataService.add(ENTITY_TYPE_META_DATA, entityType);
	}

	@Transactional
	@Override
	public void addEntityType(Collection<EntityType> entityTypes)
	{
		if (entityTypes.isEmpty())
		{
			return;
		}

		List<EntityType> resolvedEntityTypes = entityTypeDependencyResolver.resolve(entityTypes);

		// 1st pass: create entities and attributes except for mappedBy attributes
		resolvedEntityTypes.forEach(entityType ->
		{
			if (entityType.hasMappedByAttributes())
			{
				entityType = new EntityTypeWithoutMappedByAttributes(entityType);
			}

			addEntityType(entityType);
		});

		// 2nd pass: create mappedBy attributes and update entity
		resolvedEntityTypes.forEach(entityType ->
		{
			if (entityType.hasMappedByAttributes())
			{
				updateEntityType(entityType, new EntityTypeWithoutMappedByAttributes(entityType));
			}
		});
	}

	@Transactional
	@Override
	public void updateEntityType(EntityType entityType)
	{
		EntityType existingEntityType = dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)
				.eq(FULL_NAME, entityType.getName()).fetch(getEntityTypeFetch()).findOne();
		if (existingEntityType == null)
		{
			throw new UnknownEntityException(format("Unknown entity [%s]", entityType.getName()));
		}

		updateEntityType(entityType, existingEntityType);
	}

	@Transactional
	@Override
	public void upsertEntityType(EntityType entityType)
	{
		EntityType existingEntityType = dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)
				.eq(FULL_NAME, entityType.getName()).fetch(getEntityTypeFetch()).findOne();
		if (existingEntityType != null)
		{
			updateEntityType(entityType);
		}
		else
		{
			addEntityType(entityType);
		}
	}

	@Transactional
	@Override
	public void updateEntityType(Collection<EntityType> entityTypes)
	{
		if (entityTypes.isEmpty())
		{
			return;
		}

		List<EntityType> resolvedEntityType = entityTypeDependencyResolver.resolve(entityTypes);

		Map<String, EntityType> existingEntityTypeMap = dataService
				.findAll(ENTITY_TYPE_META_DATA, entityTypes.stream().map(EntityType::getName), EntityType.class)
				.collect(toMap(EntityType::getName, Function.identity()));

		// 1st pass: create entities and attributes except for mappedBy attributes
		resolvedEntityType.forEach(entityType ->
		{
			EntityType existingEntityType = existingEntityTypeMap.get(entityType.getName());
			if (existingEntityType == null)
			{
				throw new UnknownEntityException(format("Unknown entity [%s]", entityType.getName()));
			}
			if (hasNewMappedByAttrs(entityType, existingEntityType))
			{
				entityType = new EntityTypeWithoutMappedByAttributes(entityType, existingEntityType);
			}

			updateEntityType(entityType, existingEntityType);
		});

		// 2nd pass: create mappedBy attributes and update entity
		resolvedEntityType.forEach(entityType ->
		{
			EntityType existingEntityType = existingEntityTypeMap.get(entityType.getName());
			if (hasNewMappedByAttrs(entityType, existingEntityType))
			{
				updateEntityType(entityType, existingEntityType);
			}
		});
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
		Set<String> mappedByAttrs = entityType.getOwnMappedByAttributes().map(Attribute::getName)
				.collect(toSet());

		Set<String> existingMappedByAttrs = existingEntityType.getOwnMappedByAttributes()
				.map(Attribute::getName).collect(toSet());
		return !mappedByAttrs.equals(existingMappedByAttrs);
	}

	@Transactional
	@Override
	public void upsertEntityType(Collection<EntityType> entityTypes)
	{
		if (entityTypes.isEmpty())
		{
			return;
		}

		List<EntityType> resolvedEntityType = entityTypeDependencyResolver.resolve(entityTypes);

		Map<String, EntityType> existingEntityTypeMap = dataService
				.findAll(ENTITY_TYPE_META_DATA, entityTypes.stream().map(EntityType::getName), MetaUtils.getEntityTypeFetch(), EntityType.class)
				.collect(toMap(EntityType::getName, Function.identity()));

		// 1st pass: create entities and attributes except for mappedBy attributes
		resolvedEntityType.forEach(entityType ->
		{
			EntityType existingEntityType = existingEntityTypeMap.get(entityType.getName());
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

		// 2nd pass: create mappedBy attributes and update entity
		resolvedEntityType.forEach(entityType ->
		{
			EntityType existingEntityType = existingEntityTypeMap.get(entityType.getName());
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

	private void updateEntityType(EntityType entityType, EntityType existingEntityType)
	{
		populateAutoAttributeValues(existingEntityType, entityType);

		// add new attributes, update modified attributes
		upsertAttributes(entityType, existingEntityType);

		// update entity
		if (!EntityUtils.equals(entityType, existingEntityType))
		{
			// note: leave it up to the data service to decided what to do with attributes removed from entity meta data
			dataService.update(ENTITY_TYPE_META_DATA, entityType);
		}
	}

	private static void populateAutoAttributeValues(EntityType existingEntityType, EntityType entityType)
	{
		// inject existing auto-generated identifiers in system entity meta data
		Map<String, String> attrMap = stream(existingEntityType.getOwnAllAttributes().spliterator(), false)
				.collect(toMap(Attribute::getName, Attribute::getIdentifier));

		entityType.getOwnAllAttributes().forEach(attr ->
		{
			String attrIdentifier = attrMap.get(attr.getName());
			if (attrIdentifier != null)
			{
				attr.setIdentifier(attrIdentifier);
			}
		});
	}

	@Transactional
	@Override
	public void addAttribute(Attribute attr)
	{
		dataService.add(ATTRIBUTE_META_DATA, attr);
	}

	@Override
	public EntityType getEntityType(String fullyQualifiedEntityName)
	{
		EntityType systemEntity = systemEntityTypeRegistry.getSystemEntityType(fullyQualifiedEntityName);
		if (systemEntity != null)
		{
			return systemEntity;
		}
		else
		{
			return dataService
					.findOneById(ENTITY_TYPE_META_DATA, fullyQualifiedEntityName, getEntityTypeFetch(), EntityType.class);
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
		// TODO replace with dataService.upsert once available in Repository
		packages.forEach(package_ ->
		{
			Package existingPackage = dataService.findOneById(PACKAGE, package_.getName(), Package.class);
			if (existingPackage == null)
			{
				addPackage(package_);
			}
			else
			{
				dataService.update(PACKAGE, package_);
			}
		});
	}

	@Override
	public Package getPackage(String string)
	{
		return dataService.findOneById(PACKAGE, string, Package.class);
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

	@Override
	public Stream<EntityType> getEntityTypes()
	{
		List<EntityType> entityTypeList = newArrayList();
		dataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class)
				.forEachBatched(getEntityTypeFetch(), entityTypeList::addAll, 1000);
		return entityTypeList.stream();
	}

	@Override
	public Stream<Repository<Entity>> getRepositories()
	{
		return dataService.query(ENTITY_TYPE_META_DATA, EntityType.class).eq(IS_ABSTRACT, false).fetch(getEntityTypeFetch())
				.findAll().map(this::getRepository);
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
		Map<String, Attribute> attrsMap = stream(entityType.getOwnAllAttributes().spliterator(), false)
				.collect(toMap(Attribute::getName, Function.identity()));
		Map<String, Attribute> existingAttrsMap = stream(existingEntityType.getOwnAllAttributes().spliterator(),
				false).collect(toMap(Attribute::getName, Function.identity()));

		// determine attributes to add, update and delete
		Set<String> addedAttrNames = Sets.difference(attrsMap.keySet(), existingAttrsMap.keySet());
		Set<String> sharedAttrNames = Sets.intersection(attrsMap.keySet(), existingAttrsMap.keySet());

		// add new attributes
		if (!addedAttrNames.isEmpty())
		{
			dataService.add(ATTRIBUTE_META_DATA, addedAttrNames.stream().map(attrsMap::get));
		}

		// update changed attributes
		List<String> updatedAttrNames = sharedAttrNames.stream()
				.filter(attrName -> !EntityUtils.equals(attrsMap.get(attrName), existingAttrsMap.get(attrName)))
				.collect(toList());
		if (!updatedAttrNames.isEmpty())
		{
			dataService.update(ATTRIBUTE_META_DATA, updatedAttrNames.stream().map(attrsMap::get));
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
		stream(repositoryCollection.getEntityNames().spliterator(), false).forEach(entityName -> entitiesImportable
				.put(entityName,
						this.isEntityTypeCompatible(repositoryCollection.getRepository(entityName).getEntityType())));

		return entitiesImportable;
	}

	@Override
	public boolean isEntityTypeCompatible(EntityType newEntityType)
	{
		String entityName = newEntityType.getName();
		if (dataService.hasRepository(entityName))
		{
			EntityType oldEntityType = dataService.getEntityType(entityName);
			List<Attribute> oldAtomicAttributes = stream(oldEntityType.getAtomicAttributes().spliterator(),
					false).collect(toList());

			LinkedHashMap<String, Attribute> newAtomicAttributesMap = newLinkedHashMap();
			stream(newEntityType.getAtomicAttributes().spliterator(), false)
					.forEach(attribute -> newAtomicAttributesMap.put(attribute.getName(), attribute));

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
	public boolean isMetaEntityType(EntityType entityType)
	{
		switch (entityType.getName())
		{
			case ENTITY_TYPE_META_DATA:
			case ATTRIBUTE_META_DATA:
			case TAG:
			case PACKAGE:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Entity meta data that wraps a entity meta data and hides the mappedBy attributes. In code both a new and an existing
	 * entity meta data are provided only the new mappedBy attributes are hidden.
	 */
	private static class EntityTypeWithoutMappedByAttributes extends EntityType
	{
		private final EntityType entityType;
		private final EntityType existingEntityType;

		EntityTypeWithoutMappedByAttributes(EntityType entityType)
		{
			this(entityType, null);
		}

		EntityTypeWithoutMappedByAttributes(EntityType entityType, EntityType existingEntityType)
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
		public java.sql.Date getDate(String attributeName)
		{
			return entityType.getDate(attributeName);
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
								|| existingEntityType.getAttribute(entity.getString(NAME)) != null;
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
		public Timestamp getTimestamp(String attributeName)
		{
			return entityType.getTimestamp(attributeName);
		}

		@Override
		public Date getUtilDate(String attributeName)
		{
			return entityType.getUtilDate(attributeName);
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

		public static EntityType newInstance(EntityType entityType, AttributeCopyMode attrCopyMode)
		{
			return EntityType.newInstance(entityType, attrCopyMode);
		}

		@Override
		public Iterable<String> getAttributeNames()
		{
			return entityType.getAttributeNames();
		}

		@Override
		public String getName()
		{
			return entityType.getName();
		}

		@Override
		public EntityType setName(String fullName)
		{
			return entityType.setName(fullName);
		}

		@Override
		public String getSimpleName()
		{
			return entityType.getSimpleName();
		}

		@Override
		public EntityType setSimpleName(String simpleName)
		{
			return entityType.setSimpleName(simpleName);
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
		public EntityType setIdAttribute(Attribute idAttr)
		{
			return entityType.setIdAttribute(idAttr);
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
		public EntityType setLabelAttribute(Attribute labelAttr)
		{
			return entityType.setLabelAttribute(labelAttr);
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
		public EntityType setLookupAttributes(Iterable<Attribute> lookupAttrs)
		{
			return entityType.setLookupAttributes(lookupAttrs);
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
		public LinkedHashSet<Attribute> getCompoundOrderedAttributes()
		{
			return entityType.getCompoundOrderedAttributes();
		}

		@Override
		public EntityType setOwnAttributes(Iterable<Attribute> attrs)
		{
			return entityType.setOwnAttributes(attrs);
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
		public void addLookupAttribute(Attribute lookupAttr)
		{
			entityType.addLookupAttribute(lookupAttr);
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