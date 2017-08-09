package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;

import java.time.Instant;
import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

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

	public StaticEntity(EntityType entityType)
	{
		this.entity = new DynamicEntity(entityType);
	}

	public StaticEntity(Object id, EntityType entityType)
	{
		this(entityType);
		setIdValue(id);
	}

	protected StaticEntity()
	{

	}

	protected void init(Entity entity)
	{
		this.entity = requireNonNull(entity);
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

	public EntityType getEntityType()
	{
		return entity.getEntityType();
	}

	@Override
	public Object getIdValue()
	{
		return entity.getIdValue();
	}

	@Override
	public Integer getInt(String attributeName)
	{
		if (null == entity) return null;
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
	public LocalDate getLocalDate(String attributeName)
	{
		return entity.getLocalDate(attributeName);
	}

	@Override
	public Instant getInstant(String attributeName)
	{
		return entity.getInstant(attributeName);
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

	@Override
	public String toString()
	{
		return entity.toString();
	}
}
