package org.molgenis.data.meta;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;
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
import static org.molgenis.data.meta.MetaUtils.getEntityMetaDataFetch;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.*;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.*;
import static org.molgenis.data.meta.model.PackageMetaData.PACKAGE;
import static org.molgenis.data.meta.model.PackageMetaData.PARENT;
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
	private final SystemEntityMetaDataRegistry systemEntityMetaRegistry;
	private final EntityMetaDataDependencyResolver entityMetaDependencyResolver;

	@Autowired
	public MetaDataServiceImpl(DataService dataService, RepositoryCollectionRegistry repoCollectionRegistry,
			SystemEntityMetaDataRegistry systemEntityMetaRegistry,
			EntityMetaDataDependencyResolver entityMetaDependencyResolver)
	{
		this.dataService = requireNonNull(dataService);
		this.repoCollectionRegistry = requireNonNull(repoCollectionRegistry);
		this.systemEntityMetaRegistry = requireNonNull(systemEntityMetaRegistry);
		this.entityMetaDependencyResolver = requireNonNull(entityMetaDependencyResolver);
	}

	@Override
	public Repository<Entity> getRepository(String entityName)
	{
		EntityMetaData entityMeta = getEntityMetaData(entityName);
		if (entityMeta == null)
		{
			throw new UnknownEntityException(format("Unknown entity [%s]", entityName));
		}
		return !entityMeta.isAbstract() ? getRepository(entityMeta) : null;
	}

	@Override
	public <E extends Entity> Repository<E> getRepository(String entityName, Class<E> entityClass)
	{
		//noinspection unchecked
		return (Repository<E>) getRepository(entityName);
	}

	@Override
	public Repository<Entity> getRepository(EntityMetaData entityMeta)
	{
		if (!entityMeta.isAbstract())
		{
			String backendName = entityMeta.getBackend();
			RepositoryCollection backend = getBackend(backendName);
			return backend.getRepository(entityMeta);
		}
		else
		{
			return null;
		}
	}

	@Override
	public <E extends Entity> Repository<E> getRepository(EntityMetaData entityMeta, Class<E> entityClass)
	{
		//noinspection unchecked
		return (Repository<E>) getRepository(entityMeta);
	}

	@Override
	public boolean hasRepository(String entityName)
	{
		SystemEntityMetaData systemEntityMeta = systemEntityMetaRegistry.getSystemEntityMetaData(entityName);
		if (systemEntityMeta != null)
		{
			return !systemEntityMeta.isAbstract();
		}
		else
		{
			return dataService.query(ENTITY_META_DATA, EntityMetaData.class).eq(FULL_NAME, entityName).and()
					.eq(ABSTRACT, false).findOne() != null;
		}
	}

	@Override
	public Repository<Entity> createRepository(EntityMetaData entityMeta)
	{
		if (entityMeta.isAbstract())
		{
			throw new MolgenisDataException(
					format("Can't create repository for abstract entity [%s]", entityMeta.getName()));
		}
		addEntityMeta(entityMeta);
		return getRepository(entityMeta);
	}

	@Override
	public <E extends Entity> Repository<E> createRepository(EntityMetaData entityMeta, Class<E> entityClass)
	{
		if (entityMeta.isAbstract())
		{
			throw new MolgenisDataException(
					format("Can't create repository for abstract entity [%s]", entityMeta.getName()));
		}
		addEntityMeta(entityMeta);
		return getRepository(entityMeta, entityClass);
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
	public void deleteEntityMeta(String entityName)
	{
		dataService.deleteById(ENTITY_META_DATA, entityName);

		LOG.info("Removed entity [{}]", entityName);
	}

	@Transactional
	@Override
	public void deleteEntityMeta(Collection<EntityMetaData> entityMetas)
	{
		if (entityMetas.isEmpty())
		{
			return;
		}

		List<EntityMetaData> resolvedEntityMetas = reverse(entityMetaDependencyResolver.resolve(entityMetas));

		// 1st pass: remove mappedBy attributes
		List<EntityMetaData> mappedByEntityMetas = resolvedEntityMetas.stream()
				.filter(EntityMetaData::hasMappedByAttributes).map(EntityMetaDataWithoutMappedByAttributes::new)
				.collect(toList());
		if (!mappedByEntityMetas.isEmpty())
		{
			dataService.update(ENTITY_META_DATA, mappedByEntityMetas.stream());
		}

		// 2nd pass: delete entities
		dataService.deleteAll(ENTITY_META_DATA, resolvedEntityMetas.stream().map(EntityMetaData::getName));

		LOG.info("Removed entities [{}]", entityMetas.stream().map(EntityMetaData::getName).collect(joining(",")));
	}

	@Transactional
	@Override
	public void deleteAttributeById(Object id)
	{
		dataService.deleteById(ATTRIBUTE_META_DATA, id);
	}

	@Override
	public RepositoryCollection getBackend(EntityMetaData entityMeta)
	{
		String backendName = entityMeta.getBackend() == null ? getDefaultBackend().getName() : entityMeta.getBackend();
		RepositoryCollection backend = repoCollectionRegistry.getRepositoryCollection(backendName);
		if (backend == null) throw new RuntimeException(format("Unknown backend [%s]", backendName));

		return backend;
	}

	@Transactional
	@Override
	public void addEntityMeta(EntityMetaData entityMeta)
	{
		// create attributes
		Stream<AttributeMetaData> attrs = stream(entityMeta.getOwnAllAttributes().spliterator(), false);
		dataService.add(ATTRIBUTE_META_DATA, attrs);

		// create entity
		dataService.add(ENTITY_META_DATA, entityMeta);
	}

	@Transactional
	@Override
	public void addEntityMeta(Collection<EntityMetaData> entityMetas)
	{
		if (entityMetas.isEmpty())
		{
			return;
		}

		List<EntityMetaData> resolvedEntityMetas = entityMetaDependencyResolver.resolve(entityMetas);

		// 1st pass: create entities and attributes except for mappedBy attributes
		resolvedEntityMetas.forEach(entityMeta ->
		{
			if (entityMeta.hasMappedByAttributes())
			{
				entityMeta = new EntityMetaDataWithoutMappedByAttributes(entityMeta);
			}

			addEntityMeta(entityMeta);
		});

		// 2nd pass: create mappedBy attributes and update entity
		resolvedEntityMetas.forEach(entityMeta ->
		{
			if (entityMeta.hasMappedByAttributes())
			{
				updateEntityMeta(entityMeta, new EntityMetaDataWithoutMappedByAttributes(entityMeta));
			}
		});
	}

	@Transactional
	@Override
	public void updateEntityMeta(EntityMetaData entityMeta)
	{
		EntityMetaData existingEntityMeta = dataService.query(ENTITY_META_DATA, EntityMetaData.class)
				.eq(FULL_NAME, entityMeta.getName()).fetch(getEntityMetaDataFetch()).findOne();
		if (existingEntityMeta == null)
		{
			throw new UnknownEntityException(format("Unknown entity [%s]", entityMeta.getName()));
		}

		updateEntityMeta(entityMeta, existingEntityMeta);
	}

	@Transactional
	@Override
	public void upsertEntityMeta(EntityMetaData entityMeta)
	{
		EntityMetaData existingEntityMeta = dataService.query(ENTITY_META_DATA, EntityMetaData.class)
				.eq(FULL_NAME, entityMeta.getName()).fetch(getEntityMetaDataFetch()).findOne();
		if (existingEntityMeta != null)
		{
			updateEntityMeta(entityMeta);
		}
		else
		{
			addEntityMeta(entityMeta);
		}
	}

	@Transactional
	@Override
	public void updateEntityMeta(Collection<EntityMetaData> entityMetas)
	{
		if (entityMetas.isEmpty())
		{
			return;
		}

		List<EntityMetaData> resolvedEntityMeta = entityMetaDependencyResolver.resolve(entityMetas);

		Map<String, EntityMetaData> existingEntityMetaMap = dataService
				.findAll(ENTITY_META_DATA, entityMetas.stream().map(EntityMetaData::getName), EntityMetaData.class)
				.collect(toMap(EntityMetaData::getName, Function.identity()));

		// 1st pass: create entities and attributes except for mappedBy attributes
		resolvedEntityMeta.forEach(entityMeta ->
		{
			EntityMetaData existingEntityMeta = existingEntityMetaMap.get(entityMeta.getName());
			if (existingEntityMeta == null)
			{
				throw new UnknownEntityException(format("Unknown entity [%s]", entityMeta.getName()));
			}
			if (hasNewMappedByAttrs(entityMeta, existingEntityMeta))
			{
				entityMeta = new EntityMetaDataWithoutMappedByAttributes(entityMeta, existingEntityMeta);
			}

			updateEntityMeta(entityMeta, existingEntityMeta);
		});

		// 2nd pass: create mappedBy attributes and update entity
		resolvedEntityMeta.forEach(entityMeta ->
		{
			EntityMetaData existingEntityMeta = existingEntityMetaMap.get(entityMeta.getName());
			if (hasNewMappedByAttrs(entityMeta, existingEntityMeta))
			{
				updateEntityMeta(entityMeta, existingEntityMeta);
			}
		});
	}

	/**
	 * Returns true if entity meta contains mapped by attributes that do not exist in the existing entity meta.
	 *
	 * @param entityMeta         entity meta data
	 * @param existingEntityMeta existing entity meta data
	 * @return true if entity meta contains mapped by attributes that do not exist in the existing entity meta.
	 */
	private static boolean hasNewMappedByAttrs(EntityMetaData entityMeta, EntityMetaData existingEntityMeta)
	{
		Set<String> mappedByAttrs = entityMeta.getOwnMappedByAttributes().map(AttributeMetaData::getName)
				.collect(toSet());
		Set<String> existingMappedByAttrs = existingEntityMeta.getOwnMappedByAttributes()
				.map(AttributeMetaData::getName).collect(toSet());
		return !mappedByAttrs.equals(existingMappedByAttrs);
	}

	@Transactional
	@Override
	public void upsertEntityMeta(Collection<EntityMetaData> entityMetas)
	{
		if (entityMetas.isEmpty())
		{
			return;
		}

		List<EntityMetaData> resolvedEntityMeta = entityMetaDependencyResolver.resolve(entityMetas);

		Map<String, EntityMetaData> existingEntityMetaMap = dataService
				.findAll(ENTITY_META_DATA, entityMetas.stream().map(EntityMetaData::getName), MetaUtils.getEntityMetaDataFetch(), EntityMetaData.class)
				.collect(toMap(EntityMetaData::getName, Function.identity()));

		// 1st pass: create entities and attributes except for mappedBy attributes
		resolvedEntityMeta.forEach(entityMeta ->
		{
			EntityMetaData existingEntityMeta = existingEntityMetaMap.get(entityMeta.getName());
			if (existingEntityMeta == null)
			{
				if (entityMeta.hasMappedByAttributes())
				{
					entityMeta = new EntityMetaDataWithoutMappedByAttributes(entityMeta);
				}

				addEntityMeta(entityMeta);
			}
			else
			{
				if (hasNewMappedByAttrs(entityMeta, existingEntityMeta))
				{
					entityMeta = new EntityMetaDataWithoutMappedByAttributes(entityMeta, existingEntityMeta);
				}

				updateEntityMeta(entityMeta, existingEntityMeta);
			}
		});

		// 2nd pass: create mappedBy attributes and update entity
		resolvedEntityMeta.forEach(entityMeta ->
		{
			EntityMetaData existingEntityMeta = existingEntityMetaMap.get(entityMeta.getName());
			if (existingEntityMeta == null)
			{
				if (entityMeta.hasMappedByAttributes())
				{
					updateEntityMeta(entityMeta, new EntityMetaDataWithoutMappedByAttributes(entityMeta));
				}
			}
			else
			{
				if (hasNewMappedByAttrs(entityMeta, existingEntityMeta))
				{
					updateEntityMeta(entityMeta, existingEntityMeta);
				}
			}
		});
	}

	private void updateEntityMeta(EntityMetaData entityMeta, EntityMetaData existingEntityMeta)
	{
		populateAutoAttributeValues(existingEntityMeta, entityMeta);

		// add new attributes, update modified attributes
		upsertAttributes(entityMeta, existingEntityMeta);

		// update entity
		if (!EntityUtils.equals(entityMeta, existingEntityMeta))
		{
			// note: leave it up to the data service to decided what to do with attributes removed from entity meta data
			dataService.update(ENTITY_META_DATA, entityMeta);
		}
	}

	private static void populateAutoAttributeValues(EntityMetaData existingEntityMeta, EntityMetaData entityMeta)
	{
		// inject existing auto-generated identifiers in system entity meta data
		Map<String, String> attrMap = stream(existingEntityMeta.getOwnAllAttributes().spliterator(), false)
				.collect(toMap(AttributeMetaData::getName, AttributeMetaData::getIdentifier));
		entityMeta.getOwnAllAttributes().forEach(attr ->
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
	public void addAttribute(AttributeMetaData attr)
	{
		dataService.add(ATTRIBUTE_META_DATA, attr);
	}

	@Override
	public EntityMetaData getEntityMetaData(String fullyQualifiedEntityName)
	{
		EntityMetaData systemEntity = systemEntityMetaRegistry.getSystemEntityMetaData(fullyQualifiedEntityName);
		if (systemEntity != null)
		{
			return systemEntity;
		}
		else
		{
			return dataService.findOneById(ENTITY_META_DATA, fullyQualifiedEntityName, getEntityMetaDataFetch(),
					EntityMetaData.class);
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
	public Stream<EntityMetaData> getEntityMetaDatas()
	{
		List<EntityMetaData> entityMetaDataList = newArrayList();
		dataService.getRepository(ENTITY_META_DATA, EntityMetaData.class)
				.forEachBatched(getEntityMetaDataFetch(), entityMetaDataList::addAll, 1000);
		return entityMetaDataList.stream();
	}

	@Override
	public Stream<Repository<Entity>> getRepositories()
	{
		return dataService.query(ENTITY_META_DATA, EntityMetaData.class).eq(ABSTRACT, false)
				.fetch(getEntityMetaDataFetch()).findAll().map(this::getRepository);
	}

	/**
	 * Add and update entity attributes
	 *
	 * @param entityMeta         entity meta data
	 * @param existingEntityMeta existing entity meta data
	 */
	private void upsertAttributes(EntityMetaData entityMeta, EntityMetaData existingEntityMeta)
	{
		// analyze both compound and atomic attributes owned by the entity
		Map<String, AttributeMetaData> attrsMap = stream(entityMeta.getOwnAllAttributes().spliterator(), false)
				.collect(toMap(AttributeMetaData::getName, Function.identity()));
		Map<String, AttributeMetaData> existingAttrsMap = stream(existingEntityMeta.getOwnAllAttributes().spliterator(),
				false).collect(toMap(AttributeMetaData::getName, Function.identity()));

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
				.put(entityName, this.isEntityMetaDataCompatible(
						repositoryCollection.getRepository(entityName).getEntityMetaData())));

		return entitiesImportable;
	}

	@Override
	public boolean isEntityMetaDataCompatible(EntityMetaData newEntityMetaData)
	{
		String entityName = newEntityMetaData.getName();
		if (dataService.hasRepository(entityName))
		{
			EntityMetaData oldEntityMetaData = dataService.getEntityMetaData(entityName);

			List<AttributeMetaData> oldAtomicAttributes = stream(oldEntityMetaData.getAtomicAttributes().spliterator(),
					false).collect(toList());

			LinkedHashMap<String, AttributeMetaData> newAtomicAttributesMap = newLinkedHashMap();
			stream(newEntityMetaData.getAtomicAttributes().spliterator(), false)
					.forEach(attribute -> newAtomicAttributesMap.put(attribute.getName(), attribute));

			for (AttributeMetaData oldAttribute : oldAtomicAttributes)
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
	public boolean isMetaEntityMetaData(EntityMetaData entityMetaData)
	{
		switch (entityMetaData.getName())
		{
			case ENTITY_META_DATA:
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
	private static class EntityMetaDataWithoutMappedByAttributes extends EntityMetaData
	{
		private final EntityMetaData entityMeta;
		private final EntityMetaData existingEntityMeta;

		EntityMetaDataWithoutMappedByAttributes(EntityMetaData entityMeta)
		{
			this(entityMeta, null);
		}

		EntityMetaDataWithoutMappedByAttributes(EntityMetaData entityMeta, EntityMetaData existingEntityMeta)
		{
			this.entityMeta = requireNonNull(entityMeta);
			this.existingEntityMeta = existingEntityMeta;
		}

		@Override
		public void init(Entity entity)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Object get(String attributeName)
		{
			return entityMeta.get(attributeName);
		}

		@Override
		public Boolean getBoolean(String attributeName)
		{
			return entityMeta.getBoolean(attributeName);
		}

		@Override
		public java.sql.Date getDate(String attributeName)
		{
			return entityMeta.getDate(attributeName);
		}

		@Override
		public Double getDouble(String attributeName)
		{
			return entityMeta.getDouble(attributeName);
		}

		@Override
		public Iterable<Entity> getEntities(String attributeName)
		{
			Iterable<Entity> entities = entityMeta.getEntities(attributeName);
			if (attributeName.equals(ATTRIBUTES))
			{
				return () -> stream(entities.spliterator(), false).filter(entity ->
				{
					if (existingEntityMeta != null)
					{
						return entity.getEntity(MAPPED_BY) == null
								|| existingEntityMeta.getAttribute(entity.getString(NAME)) != null;
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
			return entityMeta.getEntities(attributeName, clazz);
		}

		@Override
		public Entity getEntity(String attributeName)
		{
			return entityMeta.getEntity(attributeName);
		}

		@Override
		public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
		{
			return entityMeta.getEntity(attributeName, clazz);
		}

		@Override
		public EntityMetaData getEntityMetaData()
		{
			return entityMeta.getEntityMetaData();
		}

		@Override
		public Object getIdValue()
		{
			return entityMeta.getIdValue();
		}

		@Override
		public Integer getInt(String attributeName)
		{
			return entityMeta.getInt(attributeName);
		}

		@Override
		public Object getLabelValue()
		{
			return entityMeta.getLabelValue();
		}

		@Override
		public Long getLong(String attributeName)
		{
			return entityMeta.getLong(attributeName);
		}

		@Override
		public String getString(String attributeName)
		{
			return entityMeta.getString(attributeName);
		}

		@Override
		public Timestamp getTimestamp(String attributeName)
		{
			return entityMeta.getTimestamp(attributeName);
		}

		@Override
		public Date getUtilDate(String attributeName)
		{
			return entityMeta.getUtilDate(attributeName);
		}

		@Override
		public void set(Entity values)
		{
			entityMeta.set(values);
		}

		@Override
		public void setIdValue(Object id)
		{
			entityMeta.setIdValue(id);
		}

		public static EntityMetaData newInstance(EntityMetaData entityMeta, AttributeCopyMode attrCopyMode)
		{
			return EntityMetaData.newInstance(entityMeta, attrCopyMode);
		}

		@Override
		public Iterable<String> getAttributeNames()
		{
			return entityMeta.getAttributeNames();
		}

		@Override
		public String getName()
		{
			return entityMeta.getName();
		}

		@Override
		public EntityMetaData setName(String fullName)
		{
			return entityMeta.setName(fullName);
		}

		@Override
		public String getSimpleName()
		{
			return entityMeta.getSimpleName();
		}

		@Override
		public EntityMetaData setSimpleName(String simpleName)
		{
			return entityMeta.setSimpleName(simpleName);
		}

		@Override
		public String getLabel()
		{
			return entityMeta.getLabel();
		}

		@Override
		public String getLabel(String languageCode)
		{
			return entityMeta.getLabel(languageCode);
		}

		@Override
		public EntityMetaData setLabel(String label)
		{
			return entityMeta.setLabel(label);
		}

		@Override
		public EntityMetaData setLabel(String languageCode, String label)
		{
			return entityMeta.setLabel(languageCode, label);
		}

		@Override
		public String getDescription()
		{
			return entityMeta.getDescription();
		}

		@Override
		public String getDescription(String languageCode)
		{
			return entityMeta.getDescription(languageCode);
		}

		@Override
		public EntityMetaData setDescription(String description)
		{
			return entityMeta.setDescription(description);
		}

		@Override
		public EntityMetaData setDescription(String languageCode, String description)
		{
			return entityMeta.setDescription(languageCode, description);
		}

		@Override
		public String getBackend()
		{
			return entityMeta.getBackend();
		}

		@Override
		public EntityMetaData setBackend(String backend)
		{
			return entityMeta.setBackend(backend);
		}

		@Override
		public Package getPackage()
		{
			return entityMeta.getPackage();
		}

		@Override
		public EntityMetaData setPackage(Package package_)
		{
			return entityMeta.setPackage(package_);
		}

		@Override
		public AttributeMetaData getIdAttribute()
		{
			return entityMeta.getIdAttribute();
		}

		@Override
		public AttributeMetaData getOwnIdAttribute()
		{
			return entityMeta.getOwnIdAttribute();
		}

		@Override
		public EntityMetaData setIdAttribute(AttributeMetaData idAttr)
		{
			return entityMeta.setIdAttribute(idAttr);
		}

		@Override
		public AttributeMetaData getLabelAttribute()
		{
			return entityMeta.getLabelAttribute();
		}

		@Override
		public AttributeMetaData getLabelAttribute(String langCode)
		{
			return entityMeta.getLabelAttribute(langCode);
		}

		@Override
		public AttributeMetaData getOwnLabelAttribute()
		{
			return entityMeta.getOwnLabelAttribute();
		}

		@Override
		public AttributeMetaData getOwnLabelAttribute(String languageCode)
		{
			return entityMeta.getOwnLabelAttribute(languageCode);
		}

		@Override
		public EntityMetaData setLabelAttribute(AttributeMetaData labelAttr)
		{
			return entityMeta.setLabelAttribute(labelAttr);
		}

		@Override
		public AttributeMetaData getLookupAttribute(String lookupAttrName)
		{
			return entityMeta.getLookupAttribute(lookupAttrName);
		}

		@Override
		public Iterable<AttributeMetaData> getLookupAttributes()
		{
			return entityMeta.getLookupAttributes();
		}

		@Override
		public Iterable<AttributeMetaData> getOwnLookupAttributes()
		{
			return entityMeta.getOwnLookupAttributes();
		}

		@Override
		public EntityMetaData setLookupAttributes(Iterable<AttributeMetaData> lookupAttrs)
		{
			return entityMeta.setLookupAttributes(lookupAttrs);
		}

		@Override
		public boolean isAbstract()
		{
			return entityMeta.isAbstract();
		}

		@Override
		public EntityMetaData setAbstract(boolean abstract_)
		{
			return entityMeta.setAbstract(abstract_);
		}

		@Override
		public EntityMetaData getExtends()
		{
			return entityMeta.getExtends();
		}

		@Override
		public EntityMetaData setExtends(EntityMetaData extends_)
		{
			return entityMeta.setExtends(extends_);
		}

		@Override
		public Iterable<AttributeMetaData> getOwnAttributes()
		{
			// FIXME mappedBy attribute in compound not removed
			return () -> stream(entityMeta.getOwnAttributes().spliterator(), false).filter(attr ->
			{
				if (existingEntityMeta != null)
				{
					return !attr.isMappedBy() || existingEntityMeta.getAttribute(attr.getName()) != null;
				}
				else
				{
					return !attr.isMappedBy();
				}
			}).iterator();
		}

		@Override
		public LinkedHashSet<AttributeMetaData> getCompoundOrderedAttributes()
		{
			return entityMeta.getCompoundOrderedAttributes();
		}

		@Override
		public EntityMetaData setOwnAttributes(Iterable<AttributeMetaData> attrs)
		{
			return entityMeta.setOwnAttributes(attrs);
		}

		@Override
		public Iterable<AttributeMetaData> getAttributes()
		{
			return entityMeta.getAttributes();
		}

		@Override
		public Iterable<AttributeMetaData> getAtomicAttributes()
		{
			return entityMeta.getAtomicAttributes();
		}

		@Override
		public Iterable<AttributeMetaData> getAllAttributes()
		{
			return entityMeta.getAllAttributes();
		}

		@Override
		public Iterable<AttributeMetaData> getOwnAllAttributes()
		{
			return () -> stream(entityMeta.getOwnAllAttributes().spliterator(), false).filter(attr ->
			{
				if (existingEntityMeta != null)
				{
					return !attr.isMappedBy() || existingEntityMeta.getAttribute(attr.getName()) != null;
				}
				else
				{
					return !attr.isMappedBy();
				}
			}).iterator();
		}

		@Override
		public AttributeMetaData getAttribute(String attrName)
		{
			return entityMeta.getAttribute(attrName);
		}

		@Override
		public EntityMetaData addAttribute(AttributeMetaData attr, AttributeRole... attrTypes)
		{
			return entityMeta.addAttribute(attr, attrTypes);
		}

		@Override
		public void addAttributes(Iterable<AttributeMetaData> attrs)
		{
			entityMeta.addAttributes(attrs);
		}

		@Override
		public void setAttributeRoles(AttributeMetaData attr, AttributeRole... attrTypes)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasAttributeWithExpression()
		{
			return entityMeta.hasAttributeWithExpression();
		}

		@Override
		public void removeAttribute(AttributeMetaData attr)
		{
			entityMeta.removeAttribute(attr);
		}

		@Override
		public void addLookupAttribute(AttributeMetaData lookupAttr)
		{
			entityMeta.addLookupAttribute(lookupAttr);
		}

		@Override
		public Iterable<Tag> getTags()
		{
			return entityMeta.getTags();
		}

		@Override
		public EntityMetaData setTags(Iterable<Tag> tags)
		{
			return entityMeta.setTags(tags);
		}

		@Override
		public void addTag(Tag tag)
		{
			entityMeta.addTag(tag);
		}

		@Override
		public void removeTag(Tag tag)
		{
			entityMeta.removeTag(tag);
		}

		@Override
		public Iterable<AttributeMetaData> getOwnAtomicAttributes()
		{
			return entityMeta.getOwnAtomicAttributes();
		}

		@Override
		public boolean hasBidirectionalAttributes()
		{
			return entityMeta.hasBidirectionalAttributes();
		}

		@Override
		public boolean hasMappedByAttributes()
		{
			return entityMeta.hasMappedByAttributes();
		}

		@Override
		public Stream<AttributeMetaData> getOwnMappedByAttributes()
		{
			return entityMeta.getOwnMappedByAttributes();
		}

		@Override
		public Stream<AttributeMetaData> getMappedByAttributes()
		{
			return entityMeta.getMappedByAttributes();
		}

		@Override
		public boolean hasInversedByAttributes()
		{
			return entityMeta.hasInversedByAttributes();
		}

		@Override
		public Stream<AttributeMetaData> getInversedByAttributes()
		{
			return entityMeta.getInversedByAttributes();
		}

		@Override
		public void set(String attributeName, Object value)
		{
			entityMeta.set(attributeName, value);
		}

		@Override
		public void setDefaultValues()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString()
		{
			return entityMeta.toString();
		}
	}
}