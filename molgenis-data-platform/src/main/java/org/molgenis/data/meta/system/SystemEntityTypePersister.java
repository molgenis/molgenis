package org.molgenis.data.meta.system;

import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.security.EntityIdentityUtils;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.security.acl.MutableAclClassService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;

/**
 * Persists {@link SystemEntityType} in the meta data {@link org.molgenis.data.RepositoryCollection}.
 */
@Component
public class SystemEntityTypePersister
{
	private final DataService dataService;
	private final SystemEntityTypeRegistry systemEntityTypeRegistry;
	private final SystemPackageRegistry systemPackageRegistry;
	private final EntityTypeDependencyResolver entityTypeDependencyResolver;
	private final MutableAclClassService mutableAclClassService;

	SystemEntityTypePersister(DataService dataService, SystemEntityTypeRegistry systemEntityTypeRegistry,
			EntityTypeDependencyResolver entityTypeDependencyResolver, SystemPackageRegistry systemPackageRegistry,
			MutableAclClassService mutableAclClassService)
	{
		this.dataService = requireNonNull(dataService);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
		this.systemPackageRegistry = requireNonNull(systemPackageRegistry);
		this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
		this.mutableAclClassService = requireNonNull(mutableAclClassService);
	}

	public void persist()
	{
		// persist entity metadata metadata
		persistMetadataMetadata();

		// persist Package entities
		List<Package> systemPackages = systemPackageRegistry.getSystemPackages().collect(toList());
		injectExistingPackageIdentifiers(systemPackages);
		dataService.getMeta().upsertPackages(systemPackages.stream());

		// persist EntityType entities
		List<EntityType> metaEntityMetaSet = systemEntityTypeRegistry.getSystemEntityTypes().collect(toList());
		injectExistingEntityTypeAttributeIdentifiers(metaEntityMetaSet);
		metaEntityMetaSet.forEach(systemEntityType ->
		{
			String aclClass = EntityIdentityUtils.toType(systemEntityType);
			// TODO remove casting
			if (((SystemEntityType) systemEntityType).isRowLevelSecured() && !mutableAclClassService.hasAclClass(
					aclClass))
			{
				mutableAclClassService.createAclClass(aclClass, EntityIdentityUtils.toIdType(systemEntityType));
			}
			else if (!((SystemEntityType) systemEntityType).isRowLevelSecured() && mutableAclClassService.hasAclClass(
					aclClass))
			{
				mutableAclClassService.deleteAclClass(aclClass);
			}
		});
		dataService.getMeta().upsertEntityTypes(metaEntityMetaSet);

		// remove non-existing metadata
		removeNonExistingSystemEntityTypes();
		removeNonExistingSystemPackages();
	}

	private void persistMetadataMetadata()
	{
		RepositoryCollection metadataRepoCollection = dataService.getMeta().getDefaultBackend();

		// collect meta entity meta
		List<EntityType> metaEntityTypeList = systemEntityTypeRegistry.getSystemEntityTypes()
																	  .filter(MetaDataService::isMetaEntityType)
																	  .collect(toList());
		List<EntityType> resolvedEntityTypeList = entityTypeDependencyResolver.resolve(metaEntityTypeList);

		resolvedEntityTypeList.forEach(metaEntityType ->
		{
			if (!metadataRepoCollection.hasRepository(metaEntityType))
			{
				metadataRepoCollection.createRepository(metaEntityType);
			}
		});
	}

	/**
	 * Package-private for testability
	 */
	void removeNonExistingSystemEntityTypes()
	{
		// get all system entities
		List<EntityType> removedSystemEntityMetas = dataService.findAll(ENTITY_TYPE_META_DATA, EntityType.class)
															   .filter(EntityUtils::isSystemEntity)
															   .filter(this::isNotExists)
															   .collect(toList());

		dataService.getMeta().deleteEntityType(removedSystemEntityMetas);
		removedSystemEntityMetas.forEach(
				entityType -> mutableAclClassService.deleteAclClass(EntityIdentityUtils.toType(entityType)));
	}

	private void removeNonExistingSystemPackages()
	{
		Stream<Package> systemPackages = dataService.findAll(PACKAGE, Package.class)
													.filter(EntityUtils::isSystemPackage)
													.filter(this::isNotExists);

		dataService.delete(PACKAGE, systemPackages);
	}

	private boolean isNotExists(EntityType entityType)
	{
		return !systemEntityTypeRegistry.hasSystemEntityType(entityType.getId());
	}

	private boolean isNotExists(Package package_)
	{
		return !systemPackageRegistry.containsPackage(package_);
	}

	private void injectExistingPackageIdentifiers(List<Package> systemPackages)
	{
		Map<String, Package> existingPackageMap = dataService.findAll(PACKAGE, Package.class)
															 .collect(toMap(Package::getId, pack -> pack));

		systemPackages.forEach(pack ->
		{
			Package existingPackage = existingPackageMap.get(pack.getId());

			if (existingPackage != null)
			{
				pack.setId(existingPackage.getId());
			}
		});
	}

	/**
	 * Inject existing attribute identifiers in system entity types
	 *
	 * @param entityTypes system entity types
	 */
	private void injectExistingEntityTypeAttributeIdentifiers(List<EntityType> entityTypes)
	{
		Map<String, EntityType> existingEntityTypeMap = dataService.findAll(ENTITY_TYPE_META_DATA, EntityType.class)
																   .collect(toMap(EntityType::getId,
																		   entityType -> entityType));

		entityTypes.forEach(entityType ->
		{
			EntityType existingEntityType = existingEntityTypeMap.get(entityType.getId());

			if (existingEntityType != null)
			{
				entityType.setId(existingEntityType.getId());

				Map<String, Attribute> existingAttrs = stream(existingEntityType.getOwnAllAttributes().spliterator(),
						false).collect(toMap(Attribute::getName, Function.identity()));
				entityType.getOwnAllAttributes().forEach(attr ->
				{
					Attribute existingAttr = existingAttrs.get(attr.getName());
					if (existingAttr != null)
					{
						// inject existing attribute identifier
						attr.setIdentifier(existingAttr.getIdentifier());
					}
				});
			}
		});
	}
}