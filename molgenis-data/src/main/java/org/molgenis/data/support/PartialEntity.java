package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.EntityType;

import java.sql.Date;
import java.sql.Timestamp;

import static java.util.Objects.requireNonNull;

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

	public EntityType getEntityType()
	{
		return decoratedEntity.getEntityType();
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
	public void setIdValue(Object id)
	{
		decoratedEntity.setIdValue(id);
	}

	@Override
	public Object getLabelValue()
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
			return entityManager.getReference(getEntityType(), getIdValue()).get(attributeName);
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
			return entityManager.getReference(getEntityType(), getIdValue()).getString(attributeName);
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
			return entityManager.getReference(getEntityType(), getIdValue()).getInt(attributeName);
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
			return entityManager.getReference(getEntityType(), getIdValue()).getLong(attributeName);
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
			return entityManager.getReference(getEntityType(), getIdValue()).getBoolean(attributeName);
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
			return entityManager.getReference(getEntityType(), getIdValue()).getDouble(attributeName);
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
			return entityManager.getReference(getEntityType(), getIdValue()).getDate(attributeName);
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
			return entityManager.getReference(getEntityType(), getIdValue()).getUtilDate(attributeName);
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
			return entityManager.getReference(getEntityType(), getIdValue()).getTimestamp(attributeName);
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
			return entityManager.getReference(getEntityType(), getIdValue()).getEntity(attributeName);
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
			return entityManager.getReference(getEntityType(), getIdValue()).getEntity(attributeName, clazz);
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
			return entityManager.getReference(getEntityType(), getIdValue()).getEntities(attributeName);
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
			return entityManager.getReference(getEntityType(), getIdValue()).getEntities(attributeName, clazz);
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
