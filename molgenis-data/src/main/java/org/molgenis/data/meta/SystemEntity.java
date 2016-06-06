package org.molgenis.data.meta;

import static java.lang.String.format;
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
	 * Constructs a system entity that wraps an existing {@link Entity}.
	 *
	 * @param entity     decorated entity
	 * @param entityName entity name
	 */
	protected SystemEntity(Entity entity, String entityName)
	{
		validateEntityName(entity.getEntityMetaData().getName(), entityName);

		this.entity = requireNonNull(entity);
	}

	/**
	 * Constructs a system entity based on the given {@link EntityMetaData meta data}.
	 *
	 * @param entityMeta system entity meta data
	 */
	protected SystemEntity(EntityMetaData entityMeta)
	{
		init(entityMeta);
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

	private void validateEntityName(String entityName, String expectedEntityName)
	{
		if (!expectedEntityName.equals(entityName))
		{
			throw new IllegalArgumentException(
					format("Entity must be of type [%s] instead of [%s]", expectedEntityName, entityName));
		}
	}
}
