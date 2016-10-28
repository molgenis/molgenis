package org.molgenis.data.meta.system;

import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
	private final EntityTypeDependencyResolver entityTypeDependencyResolver;

	@Autowired
	public SystemEntityTypePersister(DataService dataService, SystemEntityTypeRegistry systemEntityTypeRegistry,
			EntityTypeDependencyResolver entityTypeDependencyResolver)
	{
		this.dataService = requireNonNull(dataService);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
		this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
	}

	public void persist(ContextRefreshedEvent event)
	{
		// persist entity meta data meta data
		persistMetadataMetadata(event.getApplicationContext());

		// persist entity meta data
		List<EntityType> metaEntityMetaSet = systemEntityTypeRegistry.getSystemEntityTypes().collect(toList());

		// inject attribute identifiers
		injectExistingIdentifiers(metaEntityMetaSet);

		// upsert entity types
		dataService.getMeta().upsertEntityTypes(metaEntityMetaSet);

		// remove entity meta data
		removeNonExistingSystemEntities();
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

		// collect meta entities
		Map<String, Package> packageMap = ctx.getBeansOfType(Package.class);
		List<Package> packagesToAdd = packageMap.values().stream().filter(this::isNotPersisted).collect(toList());
		if (!packagesToAdd.isEmpty())
		{
			persist(packagesToAdd);
		}
	}

	private boolean isNotPersisted(Package package_)
	{
		return dataService.findOneById(PACKAGE, package_.getIdValue(), Package.class) == null;
	}

	private void persist(List<Package> packages)
	{
		dataService.add(PACKAGE, packages.stream());
	}

	/**
	 * Package-private for testability
	 */
	void removeNonExistingSystemEntities()
	{
		// get all system entities
		List<EntityType> systemEntityMetaSet = dataService.findAll(ENTITY_TYPE_META_DATA, EntityType.class)
				.filter(SystemEntityTypePersister::isSystemEntity).collect(toList());

		// determine removed system entities
		List<EntityType> removedSystemEntityMetas = systemEntityMetaSet.stream().filter(this::isNotExists)
				.collect(toList());

		if (!removedSystemEntityMetas.isEmpty())
		{
			dataService.getMeta().deleteEntityType(removedSystemEntityMetas);
		}
	}

	private static boolean isSystemEntity(EntityType entityType)
	{
		Package package_ = entityType.getPackage();
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

	/**
	 * Inject existing attribute identifiers in system entity types
	 *
	 * @param entityTypes system entity types
	 */
	private void injectExistingIdentifiers(List<EntityType> entityTypes)
	{

		Map<Object, EntityType> existingEntityTypeMap = dataService
				.findAll(ENTITY_TYPE_META_DATA, entityTypes.stream().map(EntityType::getIdValue), EntityType.class)
				.collect(toMap(EntityType::getIdValue, Function.identity()));

		entityTypes.forEach(entityType ->
		{
			EntityType existingEntityType = existingEntityTypeMap.get(entityType.getIdValue());
			if (existingEntityType != null)
			{
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
