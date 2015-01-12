package org.molgenis.data.meta;

import java.util.Collection;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private ManageableCrudRepositoryCollection backend;
	private DataService dataService;

	public void setDataService(DataService dataService)
	{
		this.dataService = dataService;
	}

	/**
	 * Sets the Backend, in wich the meta data and the user data is saved
	 * 
	 * Setter for the ManageableCrudRepositoryCollection, to be called after it's created. This resolves the circular
	 * dependency {@link MysqlRepositoryCollection} => decorated {@link WritableMetaDataService} =>
	 * {@link RepositoryCreator}
	 * 
	 * @param ManageableCrudRepositoryCollection
	 */
	public void setBackend(ManageableCrudRepositoryCollection backend)
	{
		this.backend = backend;

		if (backend != null)
		{
			// Create repositories in order of dependency
			backend.addEntityMeta(new TagMetaData());
			packageRepository = new PackageRepository(backend);
			entityMetaDataRepository = new EntityMetaDataRepository(backend, packageRepository);
			attributeMetaDataRepository = new AttributeMetaDataRepository(backend, entityMetaDataRepository);
		}
	}

	/**
	 * Removes entity meta data if it exists.
	 */
	@Override
	public void deleteEntityMeta(String entityName)
	{
		dataService.removeRepository(entityName);
		attributeMetaDataRepository.deleteAllAttributes(entityName);
		entityMetaDataRepository.delete(entityName);
		backend.deleteEntityMeta(entityName);
	}

	/**
	 * Removes an attribute from an entity.
	 */
	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		// Update AttributeMetaDataRepository
		attributeMetaDataRepository.remove(entityName, attributeName);
		backend.deleteAttribute(entityName, attributeName);
	}

	@Override
	public CrudRepository addEntityMeta(EntityMetaData emd)
	{
		if (attributeMetaDataRepository == null)
		{
			return null;
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

		CrudRepository repo = backend.addEntityMeta(emd);
		dataService.addRepository(repo);

		return repo;
	}

	@Override
	public void addAttribute(String fullyQualifiedName, AttributeMetaData attr)
	{
		Entity entity = entityMetaDataRepository.getEntity(fullyQualifiedName);
		entityMetaDataRepository.get(fullyQualifiedName).addAttributeMetaData(attr);
		attributeMetaDataRepository.add(entity, attr);
		backend.addAttribute(fullyQualifiedName, attr);
	}

	@Override
	public EntityMetaData getEntityMetaData(String fullyQualifiedName)
	{
		// at construction time, will be called when entityMetaDataRepository is still null
		if (attributeMetaDataRepository == null)
		{
			return null;
		}
		return entityMetaDataRepository.get(fullyQualifiedName);
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
	public void recreateMetaDataRepositories()
	{
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

	@Override
	public void refreshCaches()
	{
		packageRepository.updatePackageCache();
		entityMetaDataRepository.fillEntityMetaDataCache();
		attributeMetaDataRepository.fillAllEntityAttributes();
	}

	@Override
	public void updateEntityMeta(EntityMetaData entityMeta)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void updateAttribute(String entityName, AttributeMetaData attribute)
	{
		// TODO Auto-generated method stub

	}
}