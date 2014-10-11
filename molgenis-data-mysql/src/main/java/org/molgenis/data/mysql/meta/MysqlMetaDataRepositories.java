package org.molgenis.data.mysql.meta;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.meta.AttributeMetaDataRepositoryDecoratorFactory;
import org.molgenis.data.meta.EntityMetaDataRepositoryDecoratorFactory;
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
	public MysqlPackageRepository packageRepository;
	public MysqlEntityMetaDataRepository entityMetaDataRepository;
	public MysqlAttributeMetaDataRepository attributeMetaDataRepository;
	public EntityMetaDataRepositoryDecoratorFactory entityMetaDataRepositoryDecoratorFactory;
	public AttributeMetaDataRepositoryDecoratorFactory attributeMetaDataRepositoryDecoratorFactory;

	public MysqlMetaDataRepositories(MysqlPackageRepository packageRepository,
			MysqlEntityMetaDataRepository entityMetaDataRepository,
			MysqlAttributeMetaDataRepository attributeMetaDataRepository)
	{
		this(packageRepository, entityMetaDataRepository, attributeMetaDataRepository, null, null);
	}

	public MysqlMetaDataRepositories(MysqlPackageRepository packageRepository,
			MysqlEntityMetaDataRepository entityMetaDataRepository,
			MysqlAttributeMetaDataRepository attributeMetaDataRepository,
			EntityMetaDataRepositoryDecoratorFactory entityMetaDataRepositoryDecoratorFactory,
			AttributeMetaDataRepositoryDecoratorFactory attributeMetaDataRepositoryDecoratorFactory)
	{
		this.packageRepository = packageRepository;
		this.entityMetaDataRepository = entityMetaDataRepository;
		this.attributeMetaDataRepository = attributeMetaDataRepository;
		this.entityMetaDataRepositoryDecoratorFactory = entityMetaDataRepositoryDecoratorFactory;
		this.attributeMetaDataRepositoryDecoratorFactory = attributeMetaDataRepositoryDecoratorFactory;
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
	public Set<EntityMetaData> getAllEntityMetaDataIncludingAbstract()
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

	public void dropEntityMetaData(String entityName)
	{
		// delete metadata
		attributeMetaDataRepository.delete(attributeMetaDataRepository.findAll(new QueryImpl().eq(
				AttributeMetaDataMetaData.ENTITY, entityName)));
		entityMetaDataRepository.delete(entityMetaDataRepository.findAll(new QueryImpl().eq(
				EntityMetaDataMetaData.FULL_NAME, entityName)));
	}

	public void dropAttributeMetaData(String entityName, String attributeName)
	{
		// Update AttributeMetaDataRepository
		attributeMetaDataRepository.removeAttributeMetaData(entityName, attributeName);
	}

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
	public void registerEntityMetaData(EntityMetaData emd)
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
			// do not use getAttributeMetaDataRepository(), actions already take place during addEntityMetaData
			attributeMetaDataRepository.addAttributeMetaData(emd.getName(), att);
		}
	}

}