package org.molgenis.data.support;

import static java.util.Objects.requireNonNull;

import java.sql.Date;
import java.sql.Timestamp;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityMetaData;

/**
 * Base class for entities defined in pre-existing Java classes
 */
public abstract class StaticEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	private Entity entity;

	public StaticEntity(Entity entity)
	{
		this.entity = requireNonNull(entity);
	}

	public StaticEntity(EntityMetaData entityMeta)
	{
		this.entity = new DynamicEntity(entityMeta);
	}

	public StaticEntity(Object id, EntityMetaData entityMeta)
	{
		this(entityMeta);
		setIdValue(id);
	}

	protected StaticEntity()
	{

	}

	protected void init(EntityMetaData entityMeta)
	{
		entity = new DynamicEntity(entityMeta);
	}

	@Override
	public Object get(String attributeName)
	{
		return entity.get(attributeName);
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return entity.getAttributeNames();
	}

	@Override
	public Boolean getBoolean(String attributeName)
	{
		return entity.getBoolean(attributeName);
	}

	@Override
	public Date getDate(String attributeName)
	{
		return entity.getDate(attributeName);
	}

	@Override
	public Double getDouble(String attributeName)
	{
		return entity.getDouble(attributeName);
	}

	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		return entity.getEntities(attributeName);
	}

	@Override
	public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
	{
		return entity.getEntities(attributeName, clazz);
	}

	@Override
	public Entity getEntity(String attributeName)
	{
		return entity.getEntity(attributeName);
	}

	@Override
	public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
	{
		return entity.getEntity(attributeName, clazz);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entity.getEntityMetaData();
	}

	@Override
	public Object getIdValue()
	{
		return entity.getIdValue();
	}

	@Override
	public Integer getInt(String attributeName)
	{
		return entity.getInt(attributeName);
	}

	@Override
	public Object getLabelValue()
	{
		return entity.getLabelValue();
	}

	@Override
	public Long getLong(String attributeName)
	{
		return entity.getLong(attributeName);
	}

	@Override
	public String getString(String attributeName)
	{
		return entity.getString(attributeName);
	}

	@Override
	public Timestamp getTimestamp(String attributeName)
	{
		return entity.getTimestamp(attributeName);
	}

	@Override
	public java.util.Date getUtilDate(String attributeName)
	{
		return entity.getUtilDate(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		entity.set(attributeName, value);
	}

	@Override
	public void set(Entity values)
	{
		entity.set(values);
	}

	@Override
	public void setIdValue(Object id)
	{
		entity.setIdValue(id);
	}
}
