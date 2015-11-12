package org.molgenis.data.support;

import static java.util.Objects.requireNonNull;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;

/**
 * Entity with partially loaded attributes based on a fetch. Requesting attributes not included in the fetch are
 * retrieved on demand.
 */
public class PartialEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	private final Entity decoratedEntity;
	private final Fetch fetch;
	private final EntityManager entityManager;

	public PartialEntity(Entity decoratedEntity, Fetch fetch, EntityManager entityManager)
	{
		this.decoratedEntity = requireNonNull(decoratedEntity);
		this.fetch = requireNonNull(fetch);
		this.entityManager = requireNonNull(entityManager);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decoratedEntity.getEntityMetaData();
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return decoratedEntity.getAttributeNames();
	}

	@Override
	public Object getIdValue()
	{
		return decoratedEntity.getIdValue();
	}

	@Override
	public String getLabelValue()
	{
		return decoratedEntity.getLabelValue();
	}

	@Override
	public Object get(String attributeName)
	{
		if (fetch.hasField(attributeName))
		{
			return decoratedEntity.get(attributeName);
		}
		else
		{
			return entityManager.getReference(getEntityMetaData(), getIdValue()).get(attributeName);
		}
	}

	@Override
	public String getString(String attributeName)
	{
		return decoratedEntity.getString(attributeName);
	}

	@Override
	public Integer getInt(String attributeName)
	{
		return decoratedEntity.getInt(attributeName);
	}

	@Override
	public Long getLong(String attributeName)
	{
		return decoratedEntity.getLong(attributeName);
	}

	@Override
	public Boolean getBoolean(String attributeName)
	{
		return decoratedEntity.getBoolean(attributeName);
	}

	@Override
	public Double getDouble(String attributeName)
	{
		return decoratedEntity.getDouble(attributeName);
	}

	@Override
	public Date getDate(String attributeName)
	{
		return decoratedEntity.getDate(attributeName);
	}

	@Override
	public java.util.Date getUtilDate(String attributeName)
	{
		return decoratedEntity.getUtilDate(attributeName);
	}

	@Override
	public Timestamp getTimestamp(String attributeName)
	{
		return decoratedEntity.getTimestamp(attributeName);
	}

	@Override
	public Entity getEntity(String attributeName)
	{
		return decoratedEntity.getEntity(attributeName);
	}

	@Override
	public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
	{
		return decoratedEntity.getEntity(attributeName, clazz);
	}

	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		return decoratedEntity.getEntities(attributeName);
	}

	@Override
	public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
	{
		return decoratedEntity.getEntities(attributeName, clazz);
	}

	@Override
	public List<String> getList(String attributeName)
	{
		return decoratedEntity.getList(attributeName);
	}

	@Override
	public List<Integer> getIntList(String attributeName)
	{
		return decoratedEntity.getIntList(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		decoratedEntity.set(attributeName, value);
	}

	@Override
	public void set(Entity values)
	{
		decoratedEntity.set(values);
	}
}
