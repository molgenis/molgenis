package org.molgenis.data.mysql.meta;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.Query;
import org.molgenis.data.meta.MetaDataRepositories;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class MysqlMetaDataRepositories implements MetaDataRepositories
{
	private final MysqlPackageRepository packageRepository;
	private final MysqlEntityMetaDataRepository entityMetaDataRepository;
	private final MysqlAttributeMetaDataRepository attributeMetaDataRepository;

	public MysqlMetaDataRepositories(DataSource ds)
	{
		packageRepository = new MysqlPackageRepository(ds);
		entityMetaDataRepository = new MysqlEntityMetaDataRepository(ds);
		attributeMetaDataRepository = new MysqlAttributeMetaDataRepository(ds);
	}

	public void setRepositoryCollection(MysqlRepositoryCollection mysqlRepositoryCollection)
	{
		packageRepository.setRepositoryCollection(mysqlRepositoryCollection);
		entityMetaDataRepository.setRepositoryCollection(mysqlRepositoryCollection);
		attributeMetaDataRepository.setRepositoryCollection(mysqlRepositoryCollection);
	}

	/**
	 * Drops and creates the metadata repositories.
	 *
	 */
	public void recreateMetaDataRepositories()
	{
		attributeMetaDataRepository.drop();
		entityMetaDataRepository.drop();
		packageRepository.drop();
		packageRepository.create();
		entityMetaDataRepository.create();
		attributeMetaDataRepository.create();
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
					.getEntityAttributeMetaData(entityMetaDataWithAttributes.getName()))
			{
				entityMetaDataWithAttributes.addAttributeMetaData(attributeMetaData);
			}
		}

		// read the refEntity
		for (Entity attribute : attributeMetaDataRepository)
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
		for (String name : metadata.keySet())
		{
			metadataSet.add(metadata.get(name));
		}

		return metadataSet;
	}

	@Override
	public void removeEntityMetaData(String entityName)
	{
		// delete metadata
		attributeMetaDataRepository.delete(attributeMetaDataRepository.findAll(new QueryImpl().eq(
				AttributeMetaDataMetaData.ENTITY, entityName)));
		entityMetaDataRepository.delete(entityMetaDataRepository.findAll(new QueryImpl().eq(
				EntityMetaDataMetaData.FULL_NAME, entityName)));
	}

	@Override
	public void removeAttributeMetaData(String entityName, String attributeName)
	{
		// Update AttributeMetaDataRepository
		attributeMetaDataRepository.removeAttributeMetaData(entityName, attributeName);
	}

	@Override
	public void createAndUpgradeMetaDataTables()
	{
		createMetaDataTables();
		upgradeMetaDataTables();
	}

	private void createMetaDataTables()
	{
		// create meta data tables if they do not yet exist
		if (packageRepository.createTableIfNotExists())
		{
			if (entityMetaDataRepository.createTableIfNotExists())
			{
				attributeMetaDataRepository.createTableIfNotExists();
			}
		}
		else if (attributeMetaDataRepository.count() == 0)
		{
			// Update table structure to prevent errors in apps that don't use emx
			recreateMetaDataRepositories();
		}
	}

	private void upgradeMetaDataTables()
	{
		// Update attributes table if needed
		attributeMetaDataRepository.addAttributeToTable(AttributeMetaDataMetaData.AGGREGATEABLE);
		attributeMetaDataRepository.addAttributeToTable(AttributeMetaDataMetaData.RANGE_MIN);
		attributeMetaDataRepository.addAttributeToTable(AttributeMetaDataMetaData.RANGE_MAX);
		attributeMetaDataRepository.addAttributeToTable(AttributeMetaDataMetaData.ENUM_OPTIONS);
		attributeMetaDataRepository.addAttributeToTable(AttributeMetaDataMetaData.LABEL_ATTRIBUTE);
		attributeMetaDataRepository.addAttributeToTable(AttributeMetaDataMetaData.READ_ONLY);
		attributeMetaDataRepository.addAttributeToTable(AttributeMetaDataMetaData.UNIQUE);
	}

	@Override
	public void addEntityMetaData(EntityMetaData emd)
	{
		// add packages
		List<Package> packages = Lists.newArrayList();
		Package p = emd.getPackage();
		while (p != null)
		{
			packages.add(p);
			p = p.getParent();
		}

		Collections.reverse(packages);
		for (Package pack : packages)
		{
			if (packageRepository.getPackage(pack.getName()) == null)
			{
				packageRepository.addPackage(pack);
			}
		}

		entityMetaDataRepository.addEntityMetaData(emd);

		// add attribute metadata
		for (AttributeMetaData att : emd.getAttributes())
		{
			attributeMetaDataRepository.addAttributeMetaData(emd.getName(), att);
		}
	}

	@Override
	public boolean hasEntity(EntityMetaData emd)
	{
		Query q = new QueryImpl().eq(EntityMetaDataMetaData.FULL_NAME, emd.getName());
		return entityMetaDataRepository.findOne(q) != null;
	}

	@Override
	public void addAttributeMetaData(String name, AttributeMetaData attr)
	{
		attributeMetaDataRepository.addAttributeMetaData(name, attr);
	}

	@Override
	public Iterable<AttributeMetaData> getEntityAttributeMetaData(String entityName)
	{
		return attributeMetaDataRepository.getEntityAttributeMetaData(entityName);
	}

	@Override
	public EntityMetaData getEntityMetaData(String fullyQualifiedName)
	{
		return entityMetaDataRepository.getEntityMetaData(fullyQualifiedName);
	}

	@Override
	public void addPackage(Package p)
	{
		packageRepository.addPackage(p);
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

}