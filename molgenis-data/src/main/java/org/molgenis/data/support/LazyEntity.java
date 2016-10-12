package org.molgenis.data.support;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityMetaData;

import java.sql.Date;
import java.sql.Timestamp;

import static java.util.Objects.requireNonNull;

public class LazyEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	private final EntityMetaData entityMetaData;
	private final DataService dataService;
	private final Object id;

	private Entity entity;

	public LazyEntity(EntityMetaData entityMetaData, DataService dataService, Object id)
	{
		this.entityMetaData = requireNonNull(entityMetaData);
		this.dataService = requireNonNull(dataService);
		this.id = requireNonNull(id);
	}

	@Override
	public Object getIdValue()
	{
		return id;
	}

	@Override
	public void setIdValue(Object id)
	{
		throw new UnsupportedOperationException("Identifier of a lazy entity cannot be modified");
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return EntityMetaDataUtils.getAttributeNames(entityMetaData.getAtomicAttributes());
	}

	@Override
	public Object getLabelValue()
	{
		Attribute idAttr = entityMetaData.getIdAttribute();
		Attribute labelAttr = entityMetaData.getLabelAttribute();
		if (idAttr.equals(labelAttr))
		{
			return id;
		}
		else
		{
			return getLazyLoadedEntity().getLabelValue();
		}
	}

	@Override
	public Object get(String attributeName)
	{
		Attribute idAttr = entityMetaData.getIdAttribute();
		if (attributeName.equals(idAttr.getName()))
		{
			return id;
		}
		return getLazyLoadedEntity().get(attributeName);
	}

	@Override
	public String getString(String attributeName)
	{
		Attribute idAttr = entityMetaData.getIdAttribute();
		if (attributeName.equals(idAttr.getName()))
		{
			return (String) id;
		}
		return getLazyLoadedEntity().getString(attributeName);
	}

	@Override
	public Integer getInt(String attributeName)
	{
		Attribute idAttr = entityMetaData.getIdAttribute();
		if (attributeName.equals(idAttr.getName()))
		{
			return (Integer) id;
		}
		return getLazyLoadedEntity().getInt(attributeName);
	}

	@Override
	public Long getLong(String attributeName)
	{
		return getLazyLoadedEntity().getLong(attributeName);
	}

	@Override
	public Boolean getBoolean(String attributeName)
	{
		return getLazyLoadedEntity().getBoolean(attributeName);
	}

	@Override
	public Double getDouble(String attributeName)
	{
		return getLazyLoadedEntity().getDouble(attributeName);
	}

	@Override
	public Date getDate(String attributeName)
	{
		return getLazyLoadedEntity().getDate(attributeName);
	}

	@Override
	public java.util.Date getUtilDate(String attributeName)
	{
		return getLazyLoadedEntity().getUtilDate(attributeName);
	}

	@Override
	public Timestamp getTimestamp(String attributeName)
	{
		return getLazyLoadedEntity().getTimestamp(attributeName);
	}

	@Override
	public Entity getEntity(String attributeName)
	{
		return getLazyLoadedEntity().getEntity(attributeName);
	}

	@Override
	public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
	{
		return getLazyLoadedEntity().getEntity(attributeName, clazz);
	}

	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		return getLazyLoadedEntity().getEntities(attributeName);
	}

	@Override
	public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
	{
		return getLazyLoadedEntity().getEntities(attributeName, clazz);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		getLazyLoadedEntity().set(attributeName, value);
	}

	@Override
	public void set(Entity values)
	{
		getLazyLoadedEntity().set(values);
	}

	private Entity getLazyLoadedEntity()
	{
		if (entity == null)
		{
			entity = dataService.findOneById(entityMetaData.getName(), id);
			if (entity == null)
			{
				throw new UnknownEntityException(
						"entity [" + entityMetaData.getName() + "] with " + entityMetaData.getIdAttribute().getName()
								+ " [" + id.toString() + "] does not exist");
			}
		}
		return entity;
	}
}