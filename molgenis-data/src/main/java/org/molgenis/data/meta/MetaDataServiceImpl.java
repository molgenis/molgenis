package org.molgenis.data.meta;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Package;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.NonDecoratingRepositoryDecoratorFactory;
import org.molgenis.util.DependencyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * MetaData service. Administration of the {@link Package}, {@link EntityMetaData} and {@link AttributeMetaData} of the
 * metadata of the repositories.
 * 
 * <img src="http://yuml.me/8870d0e4.png" alt="Metadata entities" width="640"/>
 */
public class MetaDataServiceImpl implements MetaDataService
{
	private static final Logger LOG = LoggerFactory.getLogger(MetaDataServiceImpl.class);

	private PackageRepository packageRepository;
	private EntityMetaDataRepository entityMetaDataRepository;
	private AttributeMetaDataRepository attributeMetaDataRepository;
	private ManageableRepositoryCollection defaultBackend;
	private final Map<String, RepositoryCollection> backends = Maps.newHashMap();
	private final DataServiceImpl dataService;

	public MetaDataServiceImpl(DataServiceImpl dataService)
	{
		this.dataService = dataService;
		dataService.setMetaDataService(this);
	}

	/**
	 * Sets the Backend, in wich the meta data and the user data is saved
	 * 
	 * Setter for the ManageableCrudRepositoryCollection, to be called after it's created. This resolves the circular
	 * dependency {@link MysqlRepositoryCollection} => decorated {@link MetaDataService} => {@link RepositoryCreator}
	 * 
	 * @param ManageableRepositoryCollection
	 */
	@Override
	public void setDefaultBackend(ManageableRepositoryCollection backend)
	{
		this.defaultBackend = backend;
		backends.put(backend.getName(), backend);

		bootstrapMetaRepos();
	}

	private void bootstrapMetaRepos()
	{
		Repository tagRepo = defaultBackend.addEntityMeta(new TagMetaData());
		dataService.addRepository(tagRepo);

		Repository packages = defaultBackend.addEntityMeta(PackageRepository.META_DATA);
		dataService.addRepository(packages);
		packageRepository = new PackageRepository(packages);

		entityMetaDataRepository = new EntityMetaDataRepository(defaultBackend, packageRepository);
		dataService.addRepository(entityMetaDataRepository.getRepository());
		entityMetaDataRepository.fillEntityMetaDataCache();

		attributeMetaDataRepository = new AttributeMetaDataRepository(defaultBackend, entityMetaDataRepository);
		dataService.addRepository(attributeMetaDataRepository.getRepository());
	}

	@Override
	public ManageableRepositoryCollection getDefaultBackend()
	{
		return defaultBackend;
	}

	/**
	 * Removes entity meta data if it exists.
	 */
	@Transactional
	@Override
	public void deleteEntityMeta(String entityName)
	{
		if (dataService.hasRepository(entityName)) dataService.removeRepository(entityName);
		attributeMetaDataRepository.deleteAllAttributes(entityName);
		EntityMetaData emd = getEntityMetaData(entityName);
		if (emd != null)
		{
			entityMetaDataRepository.delete(entityName);
			getManageableRepositoryCollection(emd).deleteEntityMeta(entityName);
		}
	}

	@Transactional
	@Override
	public void delete(List<EntityMetaData> entities)
	{
		reverse(DependencyResolver.resolve(Sets.newHashSet(entities))).stream().map(emd -> emd.getName())
				.forEach(this::deleteEntityMeta);
	}

	/**
	 * Removes an attribute from an entity.
	 */
	@Transactional
	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		// Update AttributeMetaDataRepository
		attributeMetaDataRepository.remove(entityName, attributeName);
		EntityMetaData emd = getEntityMetaData(entityName);
		if (emd != null) getManageableRepositoryCollection(emd).deleteAttribute(entityName, attributeName);
	}

	private ManageableRepositoryCollection getManageableRepositoryCollection(EntityMetaData emd)
	{
		RepositoryCollection backend = getRepositoryCollection(emd);
		if (!(backend instanceof ManageableRepositoryCollection)) throw new RuntimeException(
				"Backend  is not a ManageableCrudRepositoryCollection");

		return (ManageableRepositoryCollection) backend;
	}

	private RepositoryCollection getRepositoryCollection(EntityMetaData emd)
	{
		String backendName = emd.getBackend() == null ? getDefaultBackend().getName() : emd.getBackend();
		RepositoryCollection backend = backends.get(backendName);
		if (backend == null) throw new RuntimeException("Unknown backend [" + backendName + "]");

		return backend;
	}

	@Transactional
	@Override
	public Repository add(EntityMetaData emd, RepositoryDecoratorFactory decoratorFactory)
	{
		RepositoryCollection backend = getRepositoryCollection(emd);

		if (getEntityMetaData(emd.getName()) != null)
		{
			if (!dataService.hasRepository(emd.getName()))
			{
				Repository repo = backend.getRepository(emd.getName());
				if (repo == null) throw new UnknownEntityException(String.format(
						"Unknown entity '%s' for backend '%s'", emd.getName(), backend.getName()));
				Repository decoratedRepo = decoratorFactory.createDecoratedRepository(repo);
				dataService.addRepository(decoratedRepo);
			}

			// Return decorated repo
			return dataService.getRepository(emd.getName());
		}

		if (dataService.hasRepository(emd.getName()))
		{
			throw new MolgenisDataException("Entity with name [" + emd.getName() + "] already exists.");
		}

		if (emd.getPackage() != null)
		{
			packageRepository.add(emd.getPackage());
		}

		Entity mdEntity = entityMetaDataRepository.add(emd);

		// add attribute metadata
		for (AttributeMetaData att : emd.getAttributes())
		{
			if (LOG.isTraceEnabled())
			{
				LOG.trace("Adding attribute metadata for entity " + emd.getName() + ", attribute " + att.getName());
			}

			if ((emd.getExtends() == null) || !Iterables.contains(emd.getExtends().getAtomicAttributes(), att))
			{
				attributeMetaDataRepository.add(mdEntity, att);
			}
		}

		if (emd.isAbstract()) return null;

		Repository repo = backend.addEntityMeta(getEntityMetaData(emd.getName()));
		Repository decoratedRepo = decoratorFactory.createDecoratedRepository(repo);
		dataService.addRepository(decoratedRepo);

		// Return decorated repo
		return dataService.getRepository(emd.getName());
	}

	@Transactional
	@Override
	public synchronized Repository addEntityMeta(EntityMetaData emd)
	{
		return add(emd, new NonDecoratingRepositoryDecoratorFactory());
	}

	@Transactional
	@Override
	public void addAttribute(String fullyQualifiedEntityName, AttributeMetaData attr)
	{
		Entity entity = entityMetaDataRepository.getEntity(fullyQualifiedEntityName);
		attributeMetaDataRepository.add(entity, attr);

		DefaultEntityMetaData emd = getEntityMetaData(fullyQualifiedEntityName);
		getManageableRepositoryCollection(emd).addAttribute(fullyQualifiedEntityName, attr);
	}

	@Override
	public DefaultEntityMetaData getEntityMetaData(String fullyQualifiedEntityName)
	{
		// at construction time, will be called when entityMetaDataRepository is still null
		if (attributeMetaDataRepository == null)
		{
			return null;
		}
		return entityMetaDataRepository.get(fullyQualifiedEntityName);
	}

	@Override
	public void addPackage(Package p)
	{
		packageRepository.add(p);
	}

	@Override
	public Package getPackage(String string)
	{
		return packageRepository.getPackage(string);
	}

	@Override
	public List<Package> getRootPackages()
	{
		return packageRepository.getRootPackages();
	}

	/**
	 * Empties all metadata tables for the sake of testability.
	 */
	@Transactional
	public void recreateMetaDataRepositories()
	{
		delete(newArrayList(getEntityMetaDatas()));

		attributeMetaDataRepository.deleteAll();
		entityMetaDataRepository.deleteAll();
		packageRepository.deleteAll();
		packageRepository.updatePackageCache();
	}

	@Override
	public Collection<EntityMetaData> getEntityMetaDatas()
	{
		return entityMetaDataRepository.getMetaDatas();
	}

	// TODO make private
	@Override
	public void refreshCaches()
	{
		packageRepository.updatePackageCache();
		entityMetaDataRepository.fillEntityMetaDataCache();
		attributeMetaDataRepository.fillAllEntityAttributes();
	}

	@Transactional
	@Override
	public List<AttributeMetaData> updateEntityMeta(EntityMetaData entityMeta)
	{
		return MetaUtils.updateEntityMeta(this, entityMeta, false);
	}

	@Override
	public void addAttributeSync(String entityName, AttributeMetaData attribute)
	{
		Entity entity = entityMetaDataRepository.getEntity(entityName);
		DefaultEntityMetaData emd = getEntityMetaData(entityName);
		emd.addAttributeMetaData(attribute);
		attributeMetaDataRepository.add(entity, attribute);
		getManageableRepositoryCollection(emd).addAttributeSync(entityName, attribute);
	}

	@Override
	@Transactional
	public List<AttributeMetaData> updateSync(EntityMetaData sourceEntityMetaData)
	{
		return MetaUtils.updateEntityMeta(this, sourceEntityMetaData, true);
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}

	public void addBackend(RepositoryCollection backend)
	{
		backends.put(backend.getName(), backend);
	}

	@Override
	public synchronized void onApplicationEvent(ContextRefreshedEvent event)
	{
		// Discover all backends
		Map<String, RepositoryCollection> backendBeans = event.getApplicationContext().getBeansOfType(
				RepositoryCollection.class);
		backendBeans.values().forEach(this::addBackend);

		// Create repositories from EntityMetaData in EntityMetaData repo
		for (EntityMetaData emd : entityMetaDataRepository.getMetaDatas())
		{
			if (!emd.isAbstract() && !dataService.hasRepository(emd.getName()))
			{
				RepositoryCollection col = backends.get(emd.getBackend());
				if (col == null) throw new MolgenisDataException("Unknown backend [" + emd.getBackend() + "]");
				Repository repo = col.addEntityMeta(emd);
				dataService.addRepository(repo);
			}
		}

		// Discover EntityMetaData
		Map<String, EntityMetaData> emds = event.getApplicationContext().getBeansOfType(EntityMetaData.class);
		DependencyResolver.resolve(Sets.newHashSet(emds.values())).forEach(this::addEntityMeta);
	}

	@Override
	public Iterator<RepositoryCollection> iterator()
	{
		return backends.values().iterator();
	}

}