package org.molgenis.data.meta;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeTraverser;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;
import org.molgenis.fieldtypes.CompoundField;
import org.molgenis.security.core.Permission;
import org.molgenis.util.DependencyResolver;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
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
	public void deleteAttribute(String entityName, String attributeName)
	{
		getAttributeRepository().deleteById(attributeName);
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
		getTagRepository().add(stream(entityMeta.getTags().spliterator(), false));

		// create entity
		getEntityRepository().add(entityMeta);

		return !entityMeta.isAbstract() ? getRepository(entityMeta) : null;
	}

	@Transactional
	@Override
	public void addAttribute(String fullyQualifiedEntityName, AttributeMetaData attr)
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

	@Override
	public void addPackage(Package p)
	{
		getPackageRepository().add(p);
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
	public List<AttributeMetaData> updateEntityMeta(EntityMetaData entityMeta)
	{
		EntityMetaData otherEntityMeta = dataService.query(ENTITY_META_DATA, EntityMetaData.class)
				.eq(EntityMetaDataMetaData.FULL_NAME, entityMeta.getName()).findOne();
		if (otherEntityMeta == null)
		{
			throw new UnknownEntityException(format("Unknown entity [%s]", entityMeta.getName()));
		}

		// add/update attributes, attributes are deleted when deleting entity meta data if no more references exist
		Iterable<AttributeMetaData> ownAttrs = entityMeta.getOwnAttributes();
		ownAttrs.forEach(attr -> {
			if (attr.getIdentifier() == null)
			{
				dataService.add(ATTRIBUTE_META_DATA, attr);
			}
			else
			{
				AttributeMetaData existingAttr = dataService
						.findOneById(ATTRIBUTE_META_DATA, attr.getIdentifier(), AttributeMetaData.class);
				if (!EntityUtils.equals(attr, existingAttr))
				{
					dataService.update(ATTRIBUTE_META_DATA, attr);
				}
			}
		});

		// package, tag and referenced entity changes are updated separately

		// update entity
		if (!EntityUtils.equals(entityMeta, otherEntityMeta))
		{
			dataService.update(ENTITY_META_DATA, entityMeta);
		}

		return MetaUtils.updateEntityMeta(this, entityMeta);
	}

	@Override
	public Iterator<RepositoryCollection> iterator()
	{
		return repoCollectionRegistry.getRepositoryCollections().iterator();
	}

	@Override
	public LinkedHashMap<String, Boolean> integrationTestMetaData(RepositoryCollection repositoryCollection)
	{
		LinkedHashMap<String, Boolean> entitiesImportable = new LinkedHashMap<>();
		stream(repositoryCollection.getEntityNames().spliterator(), false)
				.forEach(entityName -> entitiesImportable.put(entityName, this.canIntegrateEntityMetadataCheck(
						repositoryCollection.getRepository(entityName).getEntityMetaData())));

		return entitiesImportable;
	}

	@Override
	public LinkedHashMap<String, Boolean> integrationTestMetaData(
			ImmutableMap<String, EntityMetaData> newEntitiesMetaDataMap, List<String> skipEntities,
			String defaultPackage)
	{
		LinkedHashMap<String, Boolean> entitiesImportable = new LinkedHashMap<>();

		stream(newEntitiesMetaDataMap.keySet().spliterator(), false)
				.forEach(entityName -> entitiesImportable.put(entityName, skipEntities.contains(entityName) || this
						.canIntegrateEntityMetadataCheck(newEntitiesMetaDataMap.get(entityName))));

		return entitiesImportable;
	}

	private boolean canIntegrateEntityMetadataCheck(EntityMetaData newEntityMetaData)
	{
		String entityName = newEntityMetaData.getName();
		if (dataService.hasRepository(entityName))
		{
			EntityMetaData oldEntity = dataService.getEntityMetaData(entityName);

			List<AttributeMetaData> oldAtomicAttributes = stream(oldEntity.getAtomicAttributes().spliterator(), false)
					.collect(toList());

			LinkedHashMap<String, AttributeMetaData> newAtomicAttributesMap = new LinkedHashMap<>();
			stream(newEntityMetaData.getAtomicAttributes().spliterator(), false)
					.forEach(attribute -> newAtomicAttributesMap.put(attribute.getName(), attribute));

			for (AttributeMetaData oldAttribute : oldAtomicAttributes)
			{
				if (!newAtomicAttributesMap.keySet().contains(oldAttribute.getName()))
				{
					return false;
				}

				if (!EntityUtils.equals(oldAttribute, newAtomicAttributesMap.get(oldAttribute.getName())))
				{
					return false;
				}
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
				return attr.getDataType() instanceof CompoundField ? attr.getAttributeParts() : emptyList();
			}
		}.postOrderTraversal(attr).spliterator(), false);
	}
}