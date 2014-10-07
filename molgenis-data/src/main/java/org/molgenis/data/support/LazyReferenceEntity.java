package org.molgenis.data.support;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.UnknownEntityException;

/**
 * Entity whose state may be lazily fetched.
 */
public class LazyReferenceEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	private final Object id;
	private final EntityMetaData entityMetaData;
	private final DataService dataService;

	private transient Entity entity;

	public LazyReferenceEntity(Object id, EntityMetaData entityMetaData, DataService dataService)
	{
		if (id == null) throw new IllegalArgumentException("id is null");
		if (entityMetaData == null) throw new IllegalArgumentException("entityMetaData is null");
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.id = id;
		this.entityMetaData = entityMetaData;
		this.dataService = dataService;
	}

	public EntityMetaData getEntityMetaData()
	{
		return getEntity().getEntityMetaData();
	}

	public Iterable<String> getAttributeNames()
	{
		return getEntity().getAttributeNames();
	}

	public Object getIdValue()
	{
		return id;
	}

	public String getLabelValue()
	{
		return getEntity().getLabelValue();
	}

	public List<String> getLabelAttributeNames()
	{
		return getEntity().getLabelAttributeNames();
	}

	public Object get(String attributeName)
	{
		if (entityMetaData.getIdAttribute().getName().equals(attributeName))
		{
			return id;
		}
		return getEntity().get(attributeName);
	}

	public String getString(String attributeName)
	{
		return getEntity().getString(attributeName);
	}

	public Integer getInt(String attributeName)
	{
		return getEntity().getInt(attributeName);
	}

	public Long getLong(String attributeName)
	{
		return getEntity().getLong(attributeName);
	}

	public Boolean getBoolean(String attributeName)
	{
		return getEntity().getBoolean(attributeName);
	}

	public Double getDouble(String attributeName)
	{
		return getEntity().getDouble(attributeName);
	}

	public Date getDate(String attributeName)
	{
		return getEntity().getDate(attributeName);
	}

	public java.util.Date getUtilDate(String attributeName)
	{
		return getEntity().getUtilDate(attributeName);
	}

	public Timestamp getTimestamp(String attributeName)
	{
		return getEntity().getTimestamp(attributeName);
	}

	public Entity getEntity(String attributeName)
	{
		return getEntity().getEntity(attributeName);
	}

	@Override
	public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
	{
		return getEntity().getEntity(attributeName, clazz);
	}

	public Iterable<Entity> getEntities(String attributeName)
	{
		return getEntity().getEntities(attributeName);
	}

	@Override
	public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
	{
		return getEntity().getEntities(attributeName, clazz);
	}

	public List<String> getList(String attributeName)
	{
		return getEntity().getList(attributeName);
	}

	public List<Integer> getIntList(String attributeName)
	{
		return getEntity().getIntList(attributeName);
	}

	public void set(String attributeName, Object value)
	{
		getEntity().set(attributeName, value);
	}

	public void set(Entity values)
	{
		getEntity().set(values);
	}

	public void set(Entity entity, boolean strict)
	{
		getEntity().set(entity, strict);
	}

	private Entity getEntity()
	{
		if (entity == null)
		{
			entity = dataService.findOne(entityMetaData.getName(), id);
			if (entity == null)
			{
				throw new UnknownEntityException(entityMetaData.getName() + " with id [" + id + "] does not exist");
			}
		}
		return entity;
	}
}
