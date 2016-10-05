package org.molgenis.data.meta;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.util.DependencyResolver;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.MetaUtils.getEntityTypeFetch;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.*;
import static org.molgenis.data.meta.model.PackageMetaData.PACKAGE;
import static org.molgenis.data.meta.model.PackageMetaData.PARENT;
import static org.molgenis.data.meta.model.TagMetaData.TAG;

/**
 * Meta data service for retrieving and editing meta data.
 */
@Component
public class MetaDataServiceImpl implements MetaDataService
{
	private final DataService dataService;
	private final RepositoryCollectionRegistry repoCollectionRegistry;
	private final SystemEntityTypeRegistry systemEntityTypeRegistry;

	@Autowired
	public MetaDataServiceImpl(DataService dataService, RepositoryCollectionRegistry repoCollectionRegistry,
			SystemEntityTypeRegistry systemEntityTypeRegistry)
	{
		this.dataService = requireNonNull(dataService);
		this.repoCollectionRegistry = requireNonNull(repoCollectionRegistry);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
	}

	@Override
	public Stream<String> getLanguageCodes()
	{
		return getDefaultBackend().getLanguageCodes();
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
			return dataService.query(ENTITY_META_DATA, EntityType.class).eq(FULL_NAME, entityName).and()
					.eq(ABSTRACT, false).findOne() != null;
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
		dataService.deleteById(ENTITY_META_DATA, entityName);
	}

	@Transactional
	@Override
	public void delete(List<EntityType> entities)
	{
		List<EntityType> orderedEntities = DependencyResolver.resolve(Sets.newHashSet(entities));
		reverse(orderedEntities).stream().map(EntityType::getName).forEach(this::deleteEntityType);
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
		Stream<AttributeMetaData> attrs = stream(entityType.getOwnAllAttributes().spliterator(), false);
		dataService.add(ATTRIBUTE_META_DATA, attrs);

		// create entity
		dataService.add(ENTITY_META_DATA, entityType);
	}

	@Transactional
	@Override
	public void addAttribute(AttributeMetaData attr)
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
			return dataService.findOneById(ENTITY_META_DATA, fullyQualifiedEntityName, getEntityTypeFetch(),
					EntityType.class);
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
		dataService.getRepository(ENTITY_META_DATA, EntityType.class)
				.forEachBatched(getEntityTypeFetch(), entityTypeList::addAll, 1000);
		return entityTypeList.stream();
	}

	@Override
	public Stream<Repository<Entity>> getRepositories()
	{
		return dataService.query(ENTITY_META_DATA, EntityType.class).eq(ABSTRACT, false)
				.fetch(getEntityTypeFetch()).findAll().map(this::getRepository);
	}

	@Transactional
	@Override
	public void updateEntityType(EntityType entityType)
	{
		EntityType existingEntityType = dataService.query(ENTITY_META_DATA, EntityType.class)
				.eq(FULL_NAME, entityType.getName()).fetch(getEntityTypeFetch()).findOne();
		if (existingEntityType == null)
		{
			throw new UnknownEntityException(format("Unknown entity [%s]", entityType.getName()));
		}

		// add new attributes, update modified attributes
		upsertAttributes(entityType, existingEntityType);

		// update entity
		if (!EntityUtils.equals(entityType, existingEntityType))
		{
			// note: leave it up to the data service to decided what to do with attributes removed from entity meta data
			dataService.update(ENTITY_META_DATA, entityType);
		}
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
		Map<String, AttributeMetaData> attrsMap = stream(entityType.getOwnAllAttributes().spliterator(), false)
				.collect(toMap(AttributeMetaData::getName, Function.identity()));
		Map<String, AttributeMetaData> existingAttrsMap = stream(existingEntityType.getOwnAllAttributes().spliterator(),
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
				.put(entityName, this.isEntityTypeCompatible(
						repositoryCollection.getRepository(entityName).getEntityType())));

		return entitiesImportable;
	}

	@Override
	public boolean isEntityTypeCompatible(EntityType newEntityType)
	{
		String entityName = newEntityType.getName();
		if (dataService.hasRepository(entityName))
		{
			EntityType oldEntityType = dataService.getEntityType(entityName);

			List<AttributeMetaData> oldAtomicAttributes = stream(oldEntityType.getAtomicAttributes().spliterator(),
					false).collect(toList());

			LinkedHashMap<String, AttributeMetaData> newAtomicAttributesMap = newLinkedHashMap();
			stream(newEntityType.getAtomicAttributes().spliterator(), false)
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
	public boolean isMetaEntityType(EntityType entityType)
	{
		switch (entityType.getName())
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
}