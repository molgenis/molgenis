package org.molgenis.data.meta;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeTraverser;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;
import org.molgenis.security.core.Permission;
import org.molgenis.util.DependencyResolver;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.MolgenisFieldTypes.AttributeType.COMPOUND;
import static org.molgenis.data.meta.MetaUtils.getEntityMetaDataFetch;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.*;
import static org.molgenis.data.meta.model.PackageMetaData.PACKAGE;
import static org.molgenis.data.meta.model.PackageMetaData.PARENT;
import static org.molgenis.data.meta.model.TagMetaData.TAG;
import static org.molgenis.util.SecurityDecoratorUtils.validatePermission;

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
		return getRepository(entityMeta);
	}

	@Override
	public <E extends Entity> Repository<E> getRepository(String entityName, Class<E> entityClass)
	{
		return (Repository<E>) getRepository(entityName);
	}

	@Override
	public Repository<Entity> getRepository(EntityMetaData entityMeta)
	{
		String backendName = entityMeta.getBackend();
		RepositoryCollection backend = getBackend(backendName);
		return backend.getRepository(entityMeta);
	}

	@Override
	public <E extends Entity> Repository<E> getRepository(EntityMetaData entityMeta, Class<E> entityClass)
	{
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
			return getEntityRepository().query().eq(FULL_NAME, entityName).and().eq(ABSTRACT, false).findOne() != null;
		}
	}

	@Override
	public RepositoryCollection getDefaultBackend()
	{
		return repoCollectionRegistry.getDefaultRepoCollection();
	}

	@Override
	public RepositoryCollection getBackend(String name)
	{
		return repoCollectionRegistry.getRepositoryCollection(name);
	}

	/**
	 * Removes entity meta data if it exists.
	 */
	@Transactional
	@Override
	public void deleteEntityMeta(String entityName)
	{
		getEntityRepository().deleteById(entityName);
	}

	@Transactional
	@Override
	public void delete(List<EntityMetaData> entities)
	{
		entities.forEach(emd -> validatePermission(emd.getName(), Permission.WRITEMETA));

		reverse(DependencyResolver.resolve(Sets.newHashSet(entities))).stream().map(EntityMetaData::getName)
				.forEach(this::deleteEntityMeta);
	}

	/**
	 * Removes an attribute from an entity.
	 */
	@Transactional
	@Override
	public void deleteAttributeById(Object id)
	{
		getAttributeRepository().deleteById(id);
	}

	@Override
	public RepositoryCollection getBackend(EntityMetaData emd)
	{
		String backendName = emd.getBackend() == null ? getDefaultBackend().getName() : emd.getBackend();
		RepositoryCollection backend = repoCollectionRegistry.getRepositoryCollection(backendName);
		if (backend == null) throw new RuntimeException(format("Unknown backend [%s]", backendName));

		return backend;
	}

	@Transactional
	@Override
	public Repository<Entity> addEntityMeta(EntityMetaData entityMeta)
	{
		// create attributes
		Stream<AttributeMetaData> attrEntities = stream(entityMeta.getOwnAttributes().spliterator(), false)
				.flatMap(MetaDataServiceImpl::getAttributesPostOrder);
		getAttributeRepository().add(attrEntities);

		// create tags
		Iterable<Tag> tags = entityMeta.getTags();
		if (!Iterables.isEmpty(tags))
		{
			getTagRepository().add(stream(tags.spliterator(), false));
		}

		// create entity
		getEntityRepository().add(entityMeta);

		return !entityMeta.isAbstract() ? getRepository(entityMeta) : null;
	}

	@Transactional
	@Override
	public void addAttribute(AttributeMetaData attr)
	{
		getAttributeRepository().add(attr);
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
			return getEntityRepository().findOneById(fullyQualifiedEntityName, getEntityMetaDataFetch());
		}
	}

	@Override
	public boolean hasEntityMetaData(String entityName)
	{
		// TODO replace findOneId with exists once available in Repository
		return systemEntityMetaRegistry.hasSystemEntityMetaData(entityName)
				|| getEntityRepository().findOneById(entityName) != null;
	}

	@Transactional
	@Override
	public void addPackage(Package package_)
	{
		Package existingPackage = getPackageRepository().findOneById(package_.getName());
		if (existingPackage == null) getPackageRepository().add(package_);
		else
		{
			// Only perform an update on this package if the fullName is equal to the existing package
			// i.e. You are only allowed to update the package description
			if (Objects.equals(package_.getName(), existingPackage.getName())) getPackageRepository().update(package_);
			else throw new MolgenisDataException(
					format("Changing the name or the parents of an existing package [%s] is not allowed",
							package_.getSimpleName()));
		}
	}

	@Override
	public Package getPackage(String string)
	{
		return getPackageRepository().findOneById(string);
	}

	@Override
	public List<Package> getPackages()
	{
		return newArrayList(getPackageRepository());
	}

	@Override
	public List<Package> getRootPackages()
	{
		return dataService.query(PACKAGE, Package.class).eq(PARENT, null).findAll().collect(toList());
	}

	@Override
	public Stream<EntityMetaData> getEntityMetaDatas()
	{
		List<EntityMetaData> result = newArrayList();
		getEntityRepository().forEachBatched(getEntityMetaDataFetch(), result::addAll, 1000);
		return result.stream();
	}

	@Override
	public Stream<Repository<Entity>> getRepositories()
	{
		return getEntityRepository().query().eq(ABSTRACT, false).fetch(getEntityMetaDataFetch()).findAll()
				.map(this::getRepository);
	}

	@Transactional
	@Override
	public void updateEntityMeta(EntityMetaData entityMeta)
	{
		EntityMetaData existingEntityMeta = dataService.query(ENTITY_META_DATA, EntityMetaData.class)
				.eq(FULL_NAME, entityMeta.getName()).findOne();
		if (existingEntityMeta == null)
		{
			throw new UnknownEntityException(format("Unknown entity [%s]", entityMeta.getName()));
		}

		// add, update attributes and collect attributes to delete
		List<AttributeMetaData> deletedAttrs = upsertAttributes(entityMeta, existingEntityMeta);

		// update entity
		if (!EntityUtils.equals(entityMeta, existingEntityMeta))
		{
			dataService.update(ENTITY_META_DATA, entityMeta);
		}

		// delete attributes
		if (!deletedAttrs.isEmpty())
		{
			// assumption: the attribute is owned by this entity or a compound attribute owned by this entity
			dataService.deleteAll(ATTRIBUTE_META_DATA, deletedAttrs.stream().map(AttributeMetaData::getIdentifier));
		}
	}

	/**
	 * Add and update entity attributes
	 *
	 * @param entityMeta         entity meta data
	 * @param existingEntityMeta existing entity meta data
	 * @return entity attributes deleted from the given entity meta data
	 */
	private List<AttributeMetaData> upsertAttributes(EntityMetaData entityMeta, EntityMetaData existingEntityMeta)
	{
		// analyze both compound and atomic attributes owned by the entity
		Map<String, AttributeMetaData> attrsMap = stream(entityMeta.getOwnAllAttributes().spliterator(), false)
				.collect(toMap(AttributeMetaData::getName, Function.identity()));
		Map<String, AttributeMetaData> existingAttrsMap = stream(existingEntityMeta.getOwnAllAttributes().spliterator(),
				false).collect(toMap(AttributeMetaData::getName, Function.identity()));

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
				.filter(attrName -> !EntityUtils.equals(attrsMap.get(attrName), existingAttrsMap.get(attrName)))
				.collect(toList());
		if (!updatedAttrNames.isEmpty())
		{
			dataService.update(ATTRIBUTE_META_DATA, updatedAttrNames.stream().map(attrsMap::get));
		}

		// return attributes to delete so that they can be deleted after the entity was updated
		return deletedAttrNames.stream().map(existingAttrsMap::get).collect(toList());
	}

	@Override
	public Iterator<RepositoryCollection> iterator()
	{
		return repoCollectionRegistry.getRepositoryCollections().iterator();
	}

	@Override
	public LinkedHashMap<String, Boolean> determineImportableEntities(RepositoryCollection repositoryCollection)
	{
		LinkedHashMap<String, Boolean> entitiesImportable = new LinkedHashMap<>();
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

	private Repository<Package> getPackageRepository()
	{
		return getRepository(PACKAGE, Package.class);
	}

	private Repository<EntityMetaData> getEntityRepository()
	{
		return getRepository(ENTITY_META_DATA, EntityMetaData.class);
	}

	private Repository<AttributeMetaData> getAttributeRepository()
	{
		return getRepository(ATTRIBUTE_META_DATA, AttributeMetaData.class);
	}

	private Repository<Tag> getTagRepository()
	{
		return getRepository(TAG, Tag.class);
	}

	/**
	 * Returns child attributes of the given attribute in post-order
	 *
	 * @param attr attribute
	 * @return descendant attributes of the given attribute
	 */
	private static Stream<AttributeMetaData> getAttributesPostOrder(AttributeMetaData attr)
	{
		return stream(new TreeTraverser<AttributeMetaData>()
		{
			@Override
			public Iterable<AttributeMetaData> children(@Nonnull AttributeMetaData attr)
			{
				return attr.getDataType() == COMPOUND ? attr.getAttributeParts() : emptyList();
			}
		}.postOrderTraversal(attr).spliterator(), false);
	}
}