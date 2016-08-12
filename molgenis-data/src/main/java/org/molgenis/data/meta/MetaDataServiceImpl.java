package org.molgenis.data.meta;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;
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
import static org.molgenis.data.meta.MetaUtils.getEntityMetaDataFetch;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
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
	private final DataService dataService;
	private final RepositoryCollectionRegistry repoCollectionRegistry;
	private final SystemEntityMetaDataRegistry systemEntityMetaRegistry;

	@Autowired
	public MetaDataServiceImpl(DataService dataService, RepositoryCollectionRegistry repoCollectionRegistry,
			SystemEntityMetaDataRegistry systemEntityMetaRegistry)
	{
		this.dataService = requireNonNull(dataService);
		this.repoCollectionRegistry = requireNonNull(repoCollectionRegistry);
		this.systemEntityMetaRegistry = requireNonNull(systemEntityMetaRegistry);
	}

	@Override
	public Stream<String> getLanguageCodes()
	{
		return getDefaultBackend().getLanguageCodes();
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
	}

	@Transactional
	@Override
	public void delete(List<EntityMetaData> entities)
	{
		List<EntityMetaData> orderedEntities = DependencyResolver.resolve(Sets.newHashSet(entities));
		reverse(orderedEntities).stream().map(EntityMetaData::getName).forEach(this::deleteEntityMeta);
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

		// add new attributes, update modified attributes
		upsertAttributes(entityMeta, existingEntityMeta);

		// update entity
		if (!EntityUtils.equals(entityMeta, existingEntityMeta))
		{
			// note: leave it up to the data service to decided what to do with attributes removed from entity meta data
			dataService.update(ENTITY_META_DATA, entityMeta);
		}
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
}