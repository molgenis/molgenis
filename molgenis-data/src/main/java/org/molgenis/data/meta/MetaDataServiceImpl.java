package org.molgenis.data.meta;

import java.util.Collection;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
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
public class MetaDataServiceImpl implements WritableMetaDataService
{
	private static final Logger LOG = LoggerFactory.getLogger(MetaDataServiceImpl.class);

	private PackageRepository packageRepository;
	private EntityMetaDataRepository entityMetaDataRepository;
	private AttributeMetaDataRepository attributeMetaDataRepository;

	/**
	 * Setter for the MysqlRepositoryCollection, to be called after it's created. This resolves the circular dependency
	 * {@link MysqlRepositoryCollection} => decorated {@link WritableMetaDataService} => {@link RepositoryCreator}
	 * 
	 * @param mysqlRepositoryCollection
	 */
	public void setManageableCrudRepositoryCollection(ManageableCrudRepositoryCollection repositoryCollection)
	{
		if (repositoryCollection != null)
		{
			// Create repositories in order of dependency
			repositoryCollection.add(new TagMetaData());
			packageRepository = new PackageRepository(repositoryCollection);
			entityMetaDataRepository = new EntityMetaDataRepository(repositoryCollection, packageRepository);
			attributeMetaDataRepository = new AttributeMetaDataRepository(repositoryCollection,
					entityMetaDataRepository);
		}
	}

	/**
	 * Removes entity meta data if it exists.
	 */
	@Override
	public void removeEntityMetaData(String entityName)
	{
		attributeMetaDataRepository.deleteAllAttributes(entityName);
		entityMetaDataRepository.delete(entityName);
	}

	/**
	 * Removes an attribute from an entity.
	 */
	@Override
	public void removeAttributeMetaData(String entityName, String attributeName)
	{
		// Update AttributeMetaDataRepository
		attributeMetaDataRepository.remove(entityName, attributeName);
	}

	@Override
	public void addEntityMetaData(EntityMetaData emd)
	{
		if (attributeMetaDataRepository == null)
		{
			return;
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
	}

	@Override
	public void addAttributeMetaData(String fullyQualifiedName, AttributeMetaData attr)
	{
		Entity entity = entityMetaDataRepository.getEntity(fullyQualifiedName);
		entityMetaDataRepository.get(fullyQualifiedName).addAttributeMetaData(attr);
		attributeMetaDataRepository.add(entity, attr);
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
}