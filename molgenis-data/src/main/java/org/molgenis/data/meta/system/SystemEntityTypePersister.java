package org.molgenis.data.meta.system;

import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.util.DependencyResolver;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.MolgenisFieldTypes.AttributeType.XREF;
import static org.molgenis.data.meta.MetaUtils.getEntityTypeFetch;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.REF_ENTITY;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_META_DATA;
import static org.molgenis.data.meta.model.PackageMetaData.PACKAGE;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

/**
 * Persists {@link SystemEntityType} in the meta data {@link org.molgenis.data.RepositoryCollection}.
 */
@Component
public class SystemEntityTypePersister
{
	private final DataService dataService;
	private final SystemEntityTypeRegistry systemEntityTypeRegistry;
	private TagMetaData tagMeta;
	private AttributeMetaDataMetaData attrMetaMeta;
	private PackageMetaData packageMeta;
	private EntityTypeMetadata entityTypeMeta;

	@Autowired
	public SystemEntityTypePersister(DataService dataService, SystemEntityTypeRegistry systemEntityTypeRegistry)
	{
		this.dataService = requireNonNull(dataService);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
	}

	public void persist(ContextRefreshedEvent event)
	{
		RepositoryCollection repositoryCollection = dataService.getMeta().getDefaultBackend();

		// workaround for a cyclic dependency entity meta <--> attribute meta:
		// first create attribute meta and entity meta table, then change data type.
		// see the note in AttributeMetaDataMetaData and the exception in DependencyResolver
		attrMetaMeta.getAttribute(REF_ENTITY).setDataType(STRING).setRefEntity(null);

		// create meta entity tables
		// TODO make generic with dependency resolving, use MetaDataService.isMetaEntityType
		if (!repositoryCollection.hasRepository(tagMeta))
		{
			repositoryCollection.createRepository(tagMeta);
		}
		if (!repositoryCollection.hasRepository(attrMetaMeta))
		{
			repositoryCollection.createRepository(attrMetaMeta);
		}
		if (!repositoryCollection.hasRepository(packageMeta))
		{
			repositoryCollection.createRepository(packageMeta);
		}
		if (!repositoryCollection.hasRepository(entityTypeMeta))
		{
			repositoryCollection.createRepository(entityTypeMeta);
		}

		// workaround for a cyclic dependency entity meta <--> attribute meta:
		// first create attribute meta and entity meta table, then change data type.
		// see the note in AttributeMetaDataMetaData and the exception in DependencyResolver
		attrMetaMeta.getAttribute(REF_ENTITY).setDataType(XREF).setRefEntity(entityTypeMeta);

		// add default meta entities
		ApplicationContext ctx = event.getApplicationContext();
		Map<String, Package> packageMap = ctx.getBeansOfType(Package.class);
		List<Package> packagesToAdd = packageMap.values().stream().filter(this::isNotPersisted).collect(toList());
		if (!packagesToAdd.isEmpty())
		{
			persist(packagesToAdd);
		}

		// persist entity meta data
		Set<EntityType> metaEntityTypeSet = systemEntityTypeRegistry.getSystemEntityTypes().collect(toSet());
		DependencyResolver.resolve(metaEntityTypeSet).forEach(this::persist);

		// remove entity meta data
		removeNonExistingSystemEntities();
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void TagMetaData(TagMetaData tagMeta)
	{
		this.tagMeta = requireNonNull(tagMeta);
	}

	@Autowired
	public void setAttributeMetaDataMetaData(AttributeMetaDataMetaData attrMetaMeta)
	{
		this.attrMetaMeta = requireNonNull(attrMetaMeta);
	}

	@Autowired
	public void PackageMetaData(PackageMetaData packageMeta)
	{
		this.packageMeta = requireNonNull(packageMeta);
	}

	@Autowired
	public void setEntityTypeMetaData(EntityTypeMetadata entityTypeMeta)
	{
		this.entityTypeMeta = requireNonNull(entityTypeMeta);
	}

	private static void populateAutoAttributeValues(EntityType existingEntityType, EntityType entityType)
	{
		// inject existing auto-generated identifiers in system entity meta data
		Map<String, String> attrMap = stream(existingEntityType.getAllAttributes().spliterator(), false)
				.collect(toMap(AttributeMetaData::getName, AttributeMetaData::getIdentifier));
		entityType.getAllAttributes().forEach(attr ->
		{
			String attrIdentifier = attrMap.get(attr.getName());
			if (attrIdentifier != null)
			{
				attr.setIdentifier(attrIdentifier);
			}
		});
	}

	private void persist(EntityType entityType)
	{
		EntityType existingEntityType = dataService
				.findOneById(ENTITY_META_DATA, entityType.getName(), getEntityTypeFetch(), EntityType.class);
		if (existingEntityType == null)
		{
			dataService.getMeta().addEntityType(entityType);
		}
		else
		{
			populateAutoAttributeValues(existingEntityType, entityType);

			if (!EntityUtils.equals(entityType, existingEntityType))
			{
				dataService.getMeta().updateEntityType(entityType);
			}
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
		Set<EntityType> systemEntityTypeSet = dataService.findAll(ENTITY_META_DATA, EntityType.class)
				.filter(SystemEntityTypePersister::isSystemEntity).collect(toSet());

		// determine removed system entities
		Map<String, EntityType> removedSystemEntityTypeMap = systemEntityTypeSet.stream().filter(this::isNotExists)
				.collect(toMap(EntityType::getName, Function.identity()));

		if (!removedSystemEntityTypeMap.isEmpty())
		{
			// sort removed entities by dependency
			List<EntityType> sortedSystemEntityTypeList = DependencyResolver.resolve(systemEntityTypeSet);
			List<EntityType> sortedRemovedSystemEntityTypeList = sortedSystemEntityTypeList.stream()
					.filter(entityType -> removedSystemEntityTypeMap.containsKey(entityType.getName()))
					.collect(toList());

			// remove entities in reverse order of dependencies
			dataService.delete(ENTITY_META_DATA, Lists.reverse(sortedRemovedSystemEntityTypeList).stream());
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
}
