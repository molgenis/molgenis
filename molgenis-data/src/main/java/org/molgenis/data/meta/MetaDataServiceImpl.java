package org.molgenis.data.meta;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.Package;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class MetaDataServiceImpl implements WritableMetaDataService
{
	private PackageRepository packageRepository;
	private EntityMetaDataRepository entityMetaDataRepository;
	private AttributeMetaDataRepository attributeMetaDataRepository;
	private static final Logger LOG = Logger.getLogger(MetaDataServiceImpl.class);

	/**
	 * Setter for the MysqlRepositoryCollection, to be called after it's created. This resolves the circular dependency
	 * {@link MysqlRepositoryCollection} => decorated {@link WritableMetaDataService} => {@link RepositoryCreator}
	 * 
	 * @param mysqlRepositoryCollection
	 */
	public void setManageableCrudRepositoryCollection(ManageableCrudRepositoryCollection repositoryCreator)
	{
		if (repositoryCreator != null)
		{
			packageRepository = new PackageRepository(repositoryCreator);
			entityMetaDataRepository = new EntityMetaDataRepository(repositoryCreator, packageRepository);
			attributeMetaDataRepository = new AttributeMetaDataRepository(repositoryCreator, entityMetaDataRepository);
		}
	}

	@Override
	public Set<EntityMetaData> getEntityMetaDatas()
	{
		Map<String, EntityMetaData> metadata = Maps.newLinkedHashMap();

		// read the entity meta data
		for (EntityMetaData entityMetaData : entityMetaDataRepository.getEntityMetaDatas())
		{
			DefaultEntityMetaData entityMetaDataWithAttributes = new DefaultEntityMetaData(entityMetaData);
			metadata.put(entityMetaDataWithAttributes.getName(), entityMetaDataWithAttributes);

			// add the attribute meta data of the entity
			for (AttributeMetaData attributeMetaData : attributeMetaDataRepository
					.findForEntity(entityMetaDataWithAttributes.getName()))
			{
				entityMetaDataWithAttributes.addAttributeMetaData(attributeMetaData);
			}
		}

		// read the refEntity
		for (Entity attribute : attributeMetaDataRepository.getAttributeEntities())
		{
			if (attribute.getString(AttributeMetaDataMetaData.REF_ENTITY) != null)
			{
				EntityMetaData entityMetaData = metadata.get(attribute.getString(AttributeMetaDataMetaData.ENTITY));
				DefaultAttributeMetaData attributeMetaData = (DefaultAttributeMetaData) entityMetaData
						.getAttribute(attribute.getString(AttributeMetaDataMetaData.NAME));
				EntityMetaData ref = metadata.get(attribute.getString(AttributeMetaDataMetaData.REF_ENTITY));
				if (ref == null) throw new RuntimeException("refEntity '" + attribute.getString("refEntity")
						+ "' missing for " + entityMetaData.getName() + "." + attributeMetaData.getName());
				attributeMetaData.setRefEntity(ref);
			}
		}

		Set<EntityMetaData> metadataSet = Sets.newLinkedHashSet();
		metadataSet.add(PackageRepository.META_DATA);
		metadataSet.add(EntityMetaDataRepository.META_DATA);
		metadataSet.add(AttributeMetaDataRepository.META_DATA);

		for (String name : metadata.keySet())
		{
			metadataSet.add(metadata.get(name));
		}

		return metadataSet;
	}

	@Override
	public void removeEntityMetaData(String entityName)
	{
		attributeMetaDataRepository.deleteAllAttributes(entityName);
		entityMetaDataRepository.delete(entityName);
	}

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

		packageRepository.add(emd.getPackage());

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
		attributeMetaDataRepository.add(entity, attr);
	}

	@Override
	public Iterable<AttributeMetaData> getEntityAttributeMetaData(String entityName)
	{
		return attributeMetaDataRepository.findForEntity(entityName);
	}

	@Override
	public EntityMetaData getEntityMetaData(String fullyQualifiedName)
	{
		// at construction time, will be called when entityMetaDataRepository is still null
		if (attributeMetaDataRepository == null)
		{
			return null;
		}
		return entityMetaDataRepository.find(fullyQualifiedName);
	}

	@Override
	public void addPackage(Package p)
	{
		packageRepository.add(p);
	}

	@Override
	public List<EntityMetaData> getPackageEntityMetaDatas(String packageName)
	{
		return entityMetaDataRepository.getPackageEntityMetaDatas(packageName);
	}

	@Override
	public Package getPackage(String string)
	{
		return packageRepository.getPackage(string);
	}

	@Override
	public Iterable<Package> getPackages()
	{
		return packageRepository.getPackages();
	}

	/**
	 * Empties all metadata tables for the sake of testability.
	 */
	public void recreateMetaDataRepositories()
	{
		attributeMetaDataRepository.deleteAll();
		entityMetaDataRepository.deleteAll();
		packageRepository.deleteAll();
		packageRepository.addDefaultPackage();
	}
}