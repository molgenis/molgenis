package org.molgenis.data.meta;

import static org.molgenis.data.meta.EntityMetaDataMetaData.ABSTRACT;
import static org.molgenis.data.meta.EntityMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.meta.EntityMetaDataMetaData.EXTENDS;
import static org.molgenis.data.meta.EntityMetaDataMetaData.FULL_NAME;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ID_ATTRIBUTE;
import static org.molgenis.data.meta.EntityMetaDataMetaData.LABEL;
import static org.molgenis.data.meta.EntityMetaDataMetaData.LABEL_ATTRIBUTE;
import static org.molgenis.data.meta.EntityMetaDataMetaData.PACKAGE;
import static org.molgenis.data.meta.EntityMetaDataMetaData.SIMPLE_NAME;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.util.DependencyResolver;

import com.google.common.collect.Lists;

/**
 * Helper class around the {@link EntityMetaDataMetaData} repository. Caches the metadata in
 * {@link DefaultEntityMetaData}. Internal implementation class, use {@link MetaDataServiceImpl} instead.
 * 
 */
class EntityMetaDataRepository
{
	public static final EntityMetaDataMetaData META_DATA = new EntityMetaDataMetaData();
	private CrudRepository repository;
	private PackageRepository packageRepository;
	private Map<String, DefaultEntityMetaData> entityMetaDataCache = new HashMap<String, DefaultEntityMetaData>();

	public EntityMetaDataRepository(ManageableCrudRepositoryCollection collection, PackageRepository packageRepository)
	{
		this.packageRepository = packageRepository;
		this.repository = collection.add(META_DATA);
		fillEntityMetaDataCache();
	}

	/**
	 * Fills the {@link #entityMetaDataCache} with {@link EntityMetaData}, based on the entities in {@link #repository}
	 * and the {@link PackageImpl}s in {@link #packageRepository}. Adds the entities to the {@link #packageRepository}'s
	 * {@link PackageImpl}s.
	 */
	void fillEntityMetaDataCache()
	{
		List<Entity> entities = new ArrayList<Entity>();
		for (Entity entity : repository)
		{
			entities.add(entity);
			String name = entity.getString(SIMPLE_NAME);
			DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(name);
			entityMetaData.setAbstract(entity.getBoolean(ABSTRACT));
			entityMetaData.setIdAttribute(entity.getString(ID_ATTRIBUTE));
			entityMetaData.setLabelAttribute(entity.getString(LABEL_ATTRIBUTE));
			entityMetaData.setLabel(entity.getString(LABEL));
			entityMetaData.setDescription(entity.getString(DESCRIPTION));
			entityMetaDataCache.put(entity.getString(FULL_NAME), entityMetaData);
		}
		for (Entity entity : entities)
		{
			final Entity extendsEntity = entity.getEntity(EXTENDS);
			final DefaultEntityMetaData entityMetaData = entityMetaDataCache.get(entity.get(FULL_NAME));
			if (extendsEntity != null)
			{
				final DefaultEntityMetaData extendsEntityMetaData = entityMetaDataCache.get(extendsEntity
						.get(FULL_NAME));
				entityMetaData.setExtends(extendsEntityMetaData);
			}
			final Entity packageEntity = entity.getEntity(PACKAGE);
			
			PackageImpl p = (PackageImpl) packageRepository.getPackage(packageEntity
					.getString(PackageMetaData.FULL_NAME));
			if (null != p)
			{
				entityMetaData.setPackage(p);
				p.addEntity(entityMetaData);
			}
		}
	}

	/**
	 * Retrieves an EntityMetaData.
	 * 
	 * @param fullyQualifiedName
	 *            the fully qualified name of the entityMetaData
	 * @return the EntityMetaData or null if none found
	 */
	public DefaultEntityMetaData get(String fullyQualifiedName)
	{
		return entityMetaDataCache.get(fullyQualifiedName);
	}

	/**
	 * Adds an {@link EntityMetaData} to the repository. Attributes are ignored.
	 * 
	 * @param entityMetaData
	 *            the {@link EntityMetaData} to add.
	 * @return Entity representing the {@link EntityMetaData}.
	 */
	public Entity add(EntityMetaData entityMetaData)
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData(entityMetaData.getSimpleName());

		emd.setLabel(entityMetaData.getLabel());
		emd.setAbstract(entityMetaData.isAbstract());
		emd.setDescription(entityMetaData.getDescription());
		if (entityMetaData.getExtends() != null)
		{
			emd.setExtends(entityMetaDataCache.get(entityMetaData.getExtends().getName()));
		}
		if (entityMetaData.getPackage() == null)
		{
			emd.setPackage(PackageImpl.defaultPackage);
		}
		else
		{
			emd.setPackage(entityMetaData.getPackage());
		}
		if (packageRepository.getPackage(emd.getPackage().getName()) == null)
		{
			packageRepository.add(emd.getPackage());
		}
		((PackageImpl) packageRepository.getPackage(emd.getPackage().getName())).addEntity(emd);
		Entity entity = toEntity(emd);
		AttributeMetaData labelAttribute = entityMetaData.getLabelAttribute();
		if (labelAttribute != null)
		{
			emd.setLabelAttribute(labelAttribute.getName());
			entity.set(LABEL_ATTRIBUTE, labelAttribute.getName());
		}
		AttributeMetaData idAttribute = entityMetaData.getIdAttribute();
		if (idAttribute != null)
		{
			emd.setIdAttribute(idAttribute.getName());
			entity.set(ID_ATTRIBUTE, idAttribute.getName());
		}
		repository.add(entity);
		entityMetaDataCache.put(emd.getName(), emd);
		return toEntity(emd);
	}

	private Entity toEntity(EntityMetaData emd)
	{
		Entity entityMetaDataEntity = new MapEntity();
		entityMetaDataEntity.set(FULL_NAME, emd.getName());
		entityMetaDataEntity.set(SIMPLE_NAME, emd.getSimpleName());
		entityMetaDataEntity.set(PACKAGE, packageRepository.getEntity(emd.getPackage().getName()));
		entityMetaDataEntity.set(DESCRIPTION, emd.getDescription());
		entityMetaDataEntity.set(ABSTRACT, emd.isAbstract());
		entityMetaDataEntity.set(LABEL, emd.getLabel());
		if (emd.getExtends() != null)
		{
			entityMetaDataEntity.set(EXTENDS, getEntity(emd.getExtends().getName()));
		}
		return entityMetaDataEntity;
	}

	public void delete(String entityName)
	{
		Entity entity = getEntity(entityName);
		if (entity != null)
		{
			repository.deleteById(entityName);
			entityMetaDataCache.remove(entityName);
		}
	}

	/**
	 * Deletes all entities, in the right order
	 */
	public void deleteAll()
	{
		List<Entity> entities = Lists.newLinkedList(DependencyResolver.resolveSelfReferences(repository, META_DATA));
		Collections.reverse(entities);
		for (Entity entity : entities)
		{
			delete(entity.getString(EntityMetaDataMetaData.FULL_NAME));
		}
	}

	public Entity getEntity(String fullyQualifiedName)
	{
		if (!entityMetaDataCache.containsKey(fullyQualifiedName))
		{
			return null;
		}
		return toEntity(entityMetaDataCache.get(fullyQualifiedName));
	}

	public Collection<EntityMetaData> getMetaDatas()
	{
		return Collections.<EntityMetaData> unmodifiableCollection(entityMetaDataCache.values());
	}
}
