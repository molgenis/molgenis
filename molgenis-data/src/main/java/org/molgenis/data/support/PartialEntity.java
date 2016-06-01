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
		if (fetch.hasField(attributeName))
		{
			return decoratedEntity.getString(attributeName);
		}
		else
		{
			return entityManager.getReference(getEntityMetaData(), getIdValue()).getString(attributeName);
		}
	}

	@Override
	public Integer getInt(String attributeName)
	{
		if (fetch.hasField(attributeName))
		{

			return decoratedEntity.getInt(attributeName);
		}
		else
		{
			return entityManager.getReference(getEntityMetaData(), getIdValue()).getInt(attributeName);
		}
	}

	@Override
	public Long getLong(String attributeName)
	{
		if (fetch.hasField(attributeName))
		{
			return decoratedEntity.getLong(attributeName);
		}
		else
		{
			return entityManager.getReference(getEntityMetaData(), getIdValue()).getLong(attributeName);
		}
	}

	@Override
	public Boolean getBoolean(String attributeName)
	{
		if (fetch.hasField(attributeName))
		{
			return decoratedEntity.getBoolean(attributeName);
		}
		else
		{
			return entityManager.getReference(getEntityMetaData(), getIdValue()).getBoolean(attributeName);
		}
	}

	@Override
	public Double getDouble(String attributeName)
	{
		if (fetch.hasField(attributeName))
		{
			return decoratedEntity.getDouble(attributeName);
		}
		else
		{
			return entityManager.getReference(getEntityMetaData(), getIdValue()).getDouble(attributeName);
		}
	}

	@Override
	public Date getDate(String attributeName)
	{
		if (fetch.hasField(attributeName))
		{
			return decoratedEntity.getDate(attributeName);
		}
		else
		{
			return entityManager.getReference(getEntityMetaData(), getIdValue()).getDate(attributeName);
		}
	}

	@Override
	public java.util.Date getUtilDate(String attributeName)
	{
		if (fetch.hasField(attributeName))
		{
			return decoratedEntity.getUtilDate(attributeName);
		}
		else
		{
			return entityManager.getReference(getEntityMetaData(), getIdValue()).getUtilDate(attributeName);
		}
	}

	@Override
	public Timestamp getTimestamp(String attributeName)
	{
		if (fetch.hasField(attributeName))
		{
			return decoratedEntity.getTimestamp(attributeName);
		}
		else
		{
			return entityManager.getReference(getEntityMetaData(), getIdValue()).getTimestamp(attributeName);
		}
	}

	@Override
	public Entity getEntity(String attributeName)
	{
		if (fetch.hasField(attributeName))
		{
			return decoratedEntity.getEntity(attributeName);
		}
		else
		{
			return entityManager.getReference(getEntityMetaData(), getIdValue()).getEntity(attributeName);
		}
	}

	@Override
	public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
	{
		if (fetch.hasField(attributeName))
		{
			return decoratedEntity.getEntity(attributeName, clazz);
		}
		else
		{
			return entityManager.getReference(getEntityMetaData(), getIdValue()).getEntity(attributeName, clazz);
		}
	}

	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		if (fetch.hasField(attributeName))
		{
			return decoratedEntity.getEntities(attributeName);
		}
		else
		{
			return entityManager.getReference(getEntityMetaData(), getIdValue()).getEntities(attributeName);
		}
	}

	@Override
	public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
	{
		if (fetch.hasField(attributeName))
		{
			return decoratedEntity.getEntities(attributeName, clazz);
		}
		else
		{
			return entityManager.getReference(getEntityMetaData(), getIdValue()).getEntities(attributeName, clazz);
		}
	}

	@Override
	public List<String> getList(String attributeName)
	{
		if (fetch.hasField(attributeName))
		{
			return decoratedEntity.getList(attributeName);
		}
		else
		{
			return entityManager.getReference(getEntityMetaData(), getIdValue()).getList(attributeName);
		}
	}

	@Override
	public List<Integer> getIntList(String attributeName)
	{
		if (fetch.hasField(attributeName))
		{
			return decoratedEntity.getIntList(attributeName);
		}
		else
		{
			return entityManager.getReference(getEntityMetaData(), getIdValue()).getIntList(attributeName);
		}
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
