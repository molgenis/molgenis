package org.molgenis.data.meta.system;

import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCollection;
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
import static org.molgenis.AttributeType.STRING;
import static org.molgenis.AttributeType.XREF;
import static org.molgenis.data.meta.MetaUtils.getEntityMetaDataFetch;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.REF_ENTITY;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.data.meta.model.PackageMetaData.PACKAGE;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

/**
 * Persists {@link org.molgenis.data.meta.SystemEntityMetaData} in the meta data {@link org.molgenis.data.RepositoryCollection}.
 */
@Component
public class SystemEntityMetaDataPersister
{
	private final DataService dataService;
	private final SystemEntityMetaDataRegistry systemEntityMetaRegistry;
	private TagMetaData tagMeta;
	private AttributeMetaDataMetaData attrMetaMeta;
	private PackageMetaData packageMeta;
	private EntityMetaDataMetaData entityMetaMeta;

	@Autowired
	public SystemEntityMetaDataPersister(DataService dataService, SystemEntityMetaDataRegistry systemEntityMetaRegistry)
	{
		this.dataService = requireNonNull(dataService);
		this.systemEntityMetaRegistry = requireNonNull(systemEntityMetaRegistry);
	}

	public void persist(ContextRefreshedEvent event)
	{
		RepositoryCollection repositoryCollection = dataService.getMeta().getDefaultBackend();

		// workaround for a cyclic dependency entity meta <--> attribute meta:
		// first create attribute meta and entity meta table, then change data type.
		// see the note in AttributeMetaDataMetaData and the exception in DependencyResolver
		attrMetaMeta.getAttribute(REF_ENTITY).setDataType(STRING).setRefEntity(null);

		// create meta entity tables
		// TODO make generic with dependency resolving, use MetaDataService.isMetaEntityMetaData
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
		if (!repositoryCollection.hasRepository(entityMetaMeta))
		{
			repositoryCollection.createRepository(entityMetaMeta);
		}

		// workaround for a cyclic dependency entity meta <--> attribute meta:
		// first create attribute meta and entity meta table, then change data type.
		// see the note in AttributeMetaDataMetaData and the exception in DependencyResolver
		attrMetaMeta.getAttribute(REF_ENTITY).setDataType(XREF).setRefEntity(entityMetaMeta);

		// add default meta entities
		ApplicationContext ctx = event.getApplicationContext();
		Map<String, Package> packageMap = ctx.getBeansOfType(Package.class);
		List<Package> packagesToAdd = packageMap.values().stream().filter(this::isNotPersisted).collect(toList());
		if (!packagesToAdd.isEmpty())
		{
			persist(packagesToAdd);
		}

		// persist entity meta data
		Set<EntityMetaData> metaEntityMetaSet = systemEntityMetaRegistry.getSystemEntityMetaDatas().collect(toSet());
		DependencyResolver.resolve(metaEntityMetaSet).forEach(this::persist);

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
	public void setEntityMetaDataMetaData(EntityMetaDataMetaData entityMetaMeta)
	{
		this.entityMetaMeta = requireNonNull(entityMetaMeta);
	}

	private static void populateAutoAttributeValues(EntityMetaData existingEntityMeta, EntityMetaData entityMeta)
	{
		// inject existing auto-generated identifiers in system entity meta data
		Map<String, String> attrMap = stream(existingEntityMeta.getAllAttributes().spliterator(), false)
				.collect(toMap(AttributeMetaData::getName, AttributeMetaData::getIdentifier));
		entityMeta.getAllAttributes().forEach(attr ->
		{
			String attrIdentifier = attrMap.get(attr.getName());
			if (attrIdentifier != null)
			{
				attr.setIdentifier(attrIdentifier);
			}
		});
	}

	private void persist(EntityMetaData entityMeta)
	{
		EntityMetaData existingEntityMeta = dataService
				.findOneById(ENTITY_META_DATA, entityMeta.getName(), getEntityMetaDataFetch(), EntityMetaData.class);
		if (existingEntityMeta == null)
		{
			dataService.getMeta().addEntityMeta(entityMeta);
		}
		else
		{
			populateAutoAttributeValues(existingEntityMeta, entityMeta);

			if (!EntityUtils.equals(entityMeta, existingEntityMeta))
			{
				dataService.getMeta().updateEntityMeta(entityMeta);
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
		Set<EntityMetaData> systemEntityMetaSet = dataService.findAll(ENTITY_META_DATA, EntityMetaData.class)
				.filter(SystemEntityMetaDataPersister::isSystemEntity).collect(toSet());

		// determine removed system entities
		Map<String, EntityMetaData> removedSystemEntityMetaMap = systemEntityMetaSet.stream().filter(this::isNotExists)
				.collect(toMap(EntityMetaData::getName, Function.identity()));

		if (!removedSystemEntityMetaMap.isEmpty())
		{
			// sort removed entities by dependency
			List<EntityMetaData> sortedSystemEntityMetaList = DependencyResolver.resolve(systemEntityMetaSet);
			List<EntityMetaData> sortedRemovedSystemEntityMetaList = sortedSystemEntityMetaList.stream()
					.filter(entityMeta -> removedSystemEntityMetaMap.containsKey(entityMeta.getName()))
					.collect(toList());

			// remove entities in reverse order of dependencies
			dataService.delete(ENTITY_META_DATA, Lists.reverse(sortedRemovedSystemEntityMetaList).stream());
		}
	}

	private static boolean isSystemEntity(EntityMetaData entityMeta)
	{
		Package package_ = entityMeta.getPackage();
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

	private boolean isNotExists(EntityMetaData entityMeta)
	{
		return !systemEntityMetaRegistry.hasSystemEntityMetaData(entityMeta.getName());
	}
}
