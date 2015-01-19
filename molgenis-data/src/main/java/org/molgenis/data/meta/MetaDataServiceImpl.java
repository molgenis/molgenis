package org.molgenis.data.meta;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositoryCollection;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Package;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.util.DependencyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
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
	private ManageableCrudRepositoryCollection defaultBackend;
	private final Map<String, CrudRepositoryCollection> backends = Maps.newHashMap();
	private final DataService dataService;
	private final RepositoryDecoratorFactory repositoryDecoratorFactory;

	public MetaDataServiceImpl(DataService dataService)
	{
		this(dataService, null);
	}

	public MetaDataServiceImpl(DataService dataService, RepositoryDecoratorFactory repositoryDecoratorFactory)
	{
		this.dataService = dataService;
		this.repositoryDecoratorFactory = repositoryDecoratorFactory;
	}

	/**
	 * Sets the Backend, in wich the meta data and the user data is saved
	 * 
	 * Setter for the ManageableCrudRepositoryCollection, to be called after it's created. This resolves the circular
	 * dependency {@link MysqlRepositoryCollection} => decorated {@link MetaDataService} => {@link RepositoryCreator}
	 * 
	 * @param ManageableCrudRepositoryCollection
	 */
	@Override
	public void setDefaultBackend(ManageableCrudRepositoryCollection backend)
	{
		this.defaultBackend = backend;
		backends.put(backend.getName(), backend);

		bootstrapMetaRepos();
	}

	private void bootstrapMetaRepos()
	{
		CrudRepository tagRepo = defaultBackend.addEntityMeta(new TagMetaData());
		dataService.addRepository(decorate(tagRepo));

		packageRepository = new PackageRepository(defaultBackend);
		entityMetaDataRepository = new EntityMetaDataRepository(defaultBackend, packageRepository);
		attributeMetaDataRepository = new AttributeMetaDataRepository(defaultBackend, entityMetaDataRepository);

		dataService.addRepository(decorate(packageRepository.getRepository()));
		dataService.addRepository(decorate(entityMetaDataRepository.getRepository()));
		dataService.addRepository(decorate(attributeMetaDataRepository.getRepository()));
	}

	private CrudRepository decorate(CrudRepository repo)
	{
		if (repositoryDecoratorFactory == null) return repo;
		return repositoryDecoratorFactory.createDecoratedRepository(repo);
	}

	@Override
	public ManageableCrudRepositoryCollection getDefaultBackend()
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
		entityMetaDataRepository.delete(entityName);
		getBackend(emd).deleteEntityMeta(entityName);
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
		getBackend(emd).deleteAttribute(entityName, attributeName);
	}

	private ManageableCrudRepositoryCollection getBackend(EntityMetaData emd)
	{
		String backendName = emd.getBackend() == null ? getDefaultBackend().getName() : emd.getBackend();
		CrudRepositoryCollection backend = backends.get(backendName);
		if (backend == null) throw new RuntimeException("Unknown backend [" + backendName + "]");
		if (!(backend instanceof ManageableCrudRepositoryCollection)) throw new RuntimeException("Backend ["
				+ backendName + "] is not a ManageableCrudRepositoryCollection");
		return (ManageableCrudRepositoryCollection) backend;
	}

	@Transactional
	@Override
	public synchronized CrudRepository addEntityMeta(EntityMetaData emd)
	{
		if (emd.isAbstract())
		{
			return null;
		}

		CrudRepositoryCollection backend = getBackend(emd);

		if (getEntityMetaData(emd.getName()) != null)
		{
			CrudRepository repo = backend.getCrudRepository(emd.getName());
			if (!dataService.hasRepository(emd.getName()))
			{
				dataService.addRepository(repo);
			}

			return repo;
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

			attributeMetaDataRepository.add(mdEntity, att);
		}

		CrudRepository repo = backend.addEntityMeta(getEntityMetaData(emd.getName()));
		dataService.addRepository(repo);

		return repo;
	}

	@Transactional
	@Override
	public void addAttribute(String fullyQualifiedEntityName, AttributeMetaData attr)
	{
		Entity entity = entityMetaDataRepository.getEntity(fullyQualifiedEntityName);
		attributeMetaDataRepository.add(entity, attr);

		DefaultEntityMetaData emd = getEntityMetaData(fullyQualifiedEntityName);
		getBackend(emd).addAttribute(fullyQualifiedEntityName, attr);
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
		for (EntityMetaData emd : Lists
				.reverse(DependencyResolver.resolve(Sets.newLinkedHashSet(getEntityMetaDatas()))))
		{
			deleteEntityMeta(emd.getName());
		}

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
		getBackend(emd).addAttributeSync(entityName, attribute);
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

	@Override
	public synchronized void onApplicationEvent(ContextRefreshedEvent event)
	{
		// Discover all backends
		Map<String, CrudRepositoryCollection> backendBeans = event.getApplicationContext().getBeansOfType(
				CrudRepositoryCollection.class);

		for (CrudRepositoryCollection col : backendBeans.values())
		{
			backends.put(col.getName(), col);
		}

		// Create repositories from EntityMetaData in EntityMetaData repo
		for (EntityMetaData emd : entityMetaDataRepository.getMetaDatas())
		{
			if (!emd.isAbstract())
			{
				CrudRepositoryCollection col = backends.get(emd.getBackend());
				if (col == null) throw new MolgenisDataException("Unknown backend [" + emd.getBackend() + "]");
				CrudRepository repo = col.addEntityMeta(emd);
				dataService.addRepository(repo);
			}
		}
	}
}