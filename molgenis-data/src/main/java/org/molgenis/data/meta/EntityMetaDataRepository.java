package org.molgenis.data.meta;

import static org.molgenis.data.meta.EntityMetaDataMetaData.ABSTRACT;
import static org.molgenis.data.meta.EntityMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.meta.EntityMetaDataMetaData.EXTENDS;
import static org.molgenis.data.meta.EntityMetaDataMetaData.FULL_NAME;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ID_ATTRIBUTE;
import static org.molgenis.data.meta.EntityMetaDataMetaData.LABEL;
import static org.molgenis.data.meta.EntityMetaDataMetaData.PACKAGE;
import static org.molgenis.data.meta.EntityMetaDataMetaData.SIMPLE_NAME;

import java.util.Collections;
import java.util.List;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.util.DependencyResolver;

import com.google.common.collect.Lists;

class EntityMetaDataRepository
{
	public static final EntityMetaDataMetaData META_DATA = new EntityMetaDataMetaData();
	private CrudRepository repository;
	private PackageRepository packageRepository;

	public EntityMetaDataRepository(ManageableCrudRepositoryCollection collection,
			PackageRepository packageRepository)
	{
		this.packageRepository = packageRepository;
		this.repository = collection.add(META_DATA);
	}

	public Iterable<EntityMetaData> getEntityMetaDatas()
	{
		List<EntityMetaData> meta = Lists.newArrayList();
		for (Entity entity : repository)
		{
			meta.add(toEntityMetaData(entity));
		}

		return meta;
	}

	/**
	 * Gets all EntityMetaData in a package.
	 * 
	 * @param packageName
	 *            the name of the package
	 */
	public List<EntityMetaData> getPackageEntityMetaDatas(String packageName)
	{
		List<EntityMetaData> meta = Lists.newArrayList();
		Query q = new QueryImpl().eq(EntityMetaDataMetaData.PACKAGE, packageName);

		for (Entity entity : repository.findAll(q))
		{
			meta.add(toEntityMetaData(entity));
		}

		return meta;
	}

	/**
	 * Retrieves an EntityMetaData.
	 * 
	 * @param fullyQualifiedName
	 *            the fully qualified name of the entityMetaData
	 * @return the EntityMetaData or null if none found
	 */
	public EntityMetaData find(String fullyQualifiedName)
	{
		Query q = repository.query().eq(FULL_NAME, fullyQualifiedName);
		Entity entity = repository.findOne(q);
		if (entity == null)
		{
			return null;
		}

		return toEntityMetaData(entity);
	}

	public Entity add(EntityMetaData emd)
	{
		Entity entityMetaDataEntity = new MapEntity();
		entityMetaDataEntity.set(FULL_NAME, emd.getName());
		entityMetaDataEntity.set(SIMPLE_NAME, emd.getSimpleName());
		
		Entity packageEntity = packageRepository.getEntity(emd.getPackage().getName());
		entityMetaDataEntity.set(PACKAGE, packageEntity);
		entityMetaDataEntity.set(DESCRIPTION, emd.getDescription());
		entityMetaDataEntity.set(ABSTRACT, emd.isAbstract());
		if (emd.getIdAttribute() != null) entityMetaDataEntity.set(ID_ATTRIBUTE, emd.getIdAttribute().getName());
		entityMetaDataEntity.set(LABEL, emd.getLabel());
		if (emd.getExtends() != null) entityMetaDataEntity.set(EXTENDS, emd.getExtends().getName());

		repository.add(entityMetaDataEntity);

		return entityMetaDataEntity;
	}

	private DefaultEntityMetaData toEntityMetaData(Entity entity)
	{
		String name = entity.getString(FULL_NAME);
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(name);
		entityMetaData.setAbstract(entity.getBoolean(ABSTRACT));
		entityMetaData.setIdAttribute(entity.getString(ID_ATTRIBUTE));
		entityMetaData.setLabel(entity.getString(LABEL));
		entityMetaData.setDescription(entity.getString(DESCRIPTION));

		// Extends
		String extendsEntityName = entity.getString(EXTENDS);
		if (extendsEntityName != null)
		{
			EntityMetaData extendsEmd = find(extendsEntityName);
			if (extendsEmd == null) throw new MolgenisDataException("Missing super entity [" + extendsEntityName
					+ "] of entity [" + name + "]");
			entityMetaData.setExtends(extendsEmd);
		}

		return entityMetaData;
	}

	public void delete(String entityName)
	{
		repository.delete(getEntity(entityName));
	}

	/**
	 * Deletes all entities, in the right order
	 */
	public void deleteAll()
	{
		List<Entity> importOrderEntities = Lists.newLinkedList(DependencyResolver.resolveSelfReferences(repository,
				META_DATA));
		Collections.reverse(importOrderEntities);
		for (Entity entity : importOrderEntities)
		{
			delete(entity.getString(EntityMetaDataMetaData.FULL_NAME));
		}
	}
	
	public Entity getEntity(String fullyQualifiedName)
	{
		return repository.findOne(new QueryImpl().eq(EntityMetaDataMetaData.FULL_NAME, fullyQualifiedName));
	}
}
