package org.molgenis.data.meta.system;

import com.google.common.collect.Maps;
import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.populate.UuidGenerator;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
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
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

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
	private final UuidGenerator uuidGenerator;

	@Autowired
	public SystemEntityTypePersister(DataService dataService, SystemEntityTypeRegistry systemEntityTypeRegistry,
			EntityTypeDependencyResolver entityTypeDependencyResolver, UuidGenerator uuidGenerator,
			SystemPackageRegistry systemPackageRegistry)
	{
		this.dataService = requireNonNull(dataService);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
		this.systemPackageRegistry = requireNonNull(systemPackageRegistry);
		this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
		this.uuidGenerator = requireNonNull(uuidGenerator);
	}

	public void persist(ContextRefreshedEvent event)
	{
		// persist entity meta data meta data
		persistMetadataMetadata(event.getApplicationContext());

		// persist Packages
		List<SystemPackage> systemPackages = systemPackageRegistry.getSystemPackages().collect(toList());
		List<Package> newSystemPackages = injectExistingPackageIdentifiers(systemPackages);
		dataService.add(PACKAGE, newSystemPackages.stream());

		// persist EntityTypes
		List<EntityType> metaEntityMetaSet = systemEntityTypeRegistry.getSystemEntityTypes().collect(toList());
		injectExistingIdentifiers(metaEntityMetaSet);
		dataService.getMeta().upsertEntityTypes(metaEntityMetaSet);

		// remove non-existing metadata
		removeNonExistingSystemEntityTypes();
		removeNonExistingSystemPackages();
	}

	private void persistMetadataMetadata(ApplicationContext ctx)
	{
		MetaDataService metadataService = dataService.getMeta();

		RepositoryCollection metadataRepoCollection = dataService.getMeta().getDefaultBackend();

		// collect meta entity meta
		List<EntityType> metaEntityTypeList = systemEntityTypeRegistry.getSystemEntityTypes()
				.filter(metadataService::isMetaEntityType).collect(toList());
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
				.filter(SystemEntityTypePersister::isSystemEntity).filter(this::isNotExists).collect(toList());

		dataService.getMeta().deleteEntityType(removedSystemEntityMetas);
	}

	private void removeNonExistingSystemPackages()
	{
		Stream<Package> systemPackages = dataService.findAll(PACKAGE, Package.class)
				.filter(SystemEntityTypePersister::isSystemPackage).filter(this::isNotExists);

		dataService.delete(PACKAGE, systemPackages);
	}

	private static boolean isSystemEntity(EntityType entityType)
	{
		return isSystemPackage(entityType.getPackage());
	}

	private static boolean isSystemPackage(Package package_)
	{
		if (package_ == null)
		{
			return false;
		}
		if (package_.getName().equals(PACKAGE_SYSTEM))
		{
			return true;
		}
		Package rootPackage = package_.getRootPackage();
		return rootPackage != null && rootPackage.getName().equals(PACKAGE_SYSTEM);
	}

	private boolean isNotExists(EntityType entityType)
	{
		return !systemEntityTypeRegistry.hasSystemEntityType(entityType.getName());
	}

	private boolean isNotExists(Package package_)
	{
		return !systemPackageRegistry.containsPackage(package_);
	}

	private List<Package> injectExistingPackageIdentifiers(List<SystemPackage> systemPackages)
	{
		Map<String, Package> existingPackageMap = dataService.findAll(PACKAGE, Package.class)
				.collect(toMap(Package::getName, pack -> pack));

		return systemPackages.stream().filter(pack ->
		{
			Package existingPackage = existingPackageMap.get(pack.getName());

			if (existingPackage == null)
			{
				// FIXME use populator
				pack.setId(uuidGenerator.generateId());
				return true;
			}
			else
			{
				pack.setId(existingPackage.getId());
				return false;
			}
		}).collect(toList());
	}

	/**
	 * Inject existing attribute identifiers in system entity types
	 *
	 * @param entityTypes system entity types
	 */
	private void injectExistingIdentifiers(List<EntityType> entityTypes)
	{
		Map<String, EntityType> existingEntityTypeMap = Maps.newHashMap();
		entityTypes.forEach(entityType ->
		{
			EntityType existingEntityType = dataService.findOne(ENTITY_TYPE_META_DATA,
					new QueryImpl<EntityType>().eq(EntityTypeMetadata.PACKAGE, entityType.getPackage()).and()
							.eq(EntityTypeMetadata.SIMPLE_NAME, entityType.getSimpleName()), EntityType.class);
			if (existingEntityType != null)
			{
				existingEntityTypeMap.put(entityType.getName(), existingEntityType);
			}
		});

		entityTypes.forEach(entityType ->
		{
			EntityType existingEntityType = existingEntityTypeMap.get(entityType.getName());
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
			else
			{
				// FIXME should be done by populator
				entityType.setId(uuidGenerator.generateId());
			}
		});
	}
}
