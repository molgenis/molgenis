package org.molgenis.data.meta;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.Entity;
import org.molgenis.data.support.AbstractEntity;
import org.molgenis.data.support.MapEntity;

/**
 * Base class for system entities
 */
public abstract class SystemEntity extends AbstractEntity
{
	private Entity entity;

	protected SystemEntity()
	{

	}

	/**
	 * Constructs a system entity that wraps an existing {@link Entity}
	 *
	 * @param entity decorated entity
	 */
	protected SystemEntity(Entity entity)
	{
		this.entity = requireNonNull(entity);
	}
	/**
	 * Constructs a system entity based on the given {@link EntityMetaData}
	 *
	 * @param entityMetaData system entity meta data
	 */
	protected SystemEntity(EntityMetaData entityMetaData)
	{
		init(entityMetaData);
	}

	protected void init(EntityMetaData entityMetaData)
	{
		this.entity = new MapEntity(requireNonNull(entityMetaData));
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entity.getEntityMetaData();
	}

	@Override
	public Object get(String attrName)
	{
		return entity.get(attrName);
	}

	@Override
	public void set(String attrName, Object value)
	{
		entity.set(attrName, value);
	}

	@Override
	public void set(Entity values)
	{
		entity.set(values);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SystemEntity that = (SystemEntity) o;

		return entity.equals(that.entity);

	}

	@Override
	public int hashCode()
	{
		return entity.hashCode();
	}

	@Override
	public String toString()
	{
		return entity.toString();
	}
}
