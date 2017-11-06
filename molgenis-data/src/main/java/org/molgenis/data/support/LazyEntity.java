package org.molgenis.data.support;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.time.Instant;
import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

public class LazyEntity implements Entity
{
	private final EntityType entityType;
	private final DataService dataService;
	private final Object id;

	private Entity entity;

	public LazyEntity(EntityType entityType, DataService dataService, Object id)
	{
		this.entityType = requireNonNull(entityType);
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

	public EntityType getEntityType()
	{
		return entityType;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return EntityTypeUtils.getAttributeNames(entityType.getAtomicAttributes());
	}

	@Override
	public Object getLabelValue()
	{
		Attribute idAttr = entityType.getIdAttribute();
		Attribute labelAttr = entityType.getLabelAttribute();
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
		Attribute idAttr = entityType.getIdAttribute();
		if (attributeName.equals(idAttr.getName()))
		{
			return id;
		}
		return getLazyLoadedEntity().get(attributeName);
	}

	@Override
	public String getString(String attributeName)
	{
		Attribute idAttr = entityType.getIdAttribute();
		if (attributeName.equals(idAttr.getName()))
		{
			return (String) id;
		}
		return getLazyLoadedEntity().getString(attributeName);
	}

	@Override
	public Integer getInt(String attributeName)
	{
		Attribute idAttr = entityType.getIdAttribute();
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
	public Instant getInstant(String attributeName)
	{
		return getLazyLoadedEntity().getInstant(attributeName);
	}

	@Override
	public LocalDate getLocalDate(String attributeName)
	{
		return getLazyLoadedEntity().getLocalDate(attributeName);
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
			entity = dataService.findOneById(entityType.getId(), id);
			if (entity == null)
			{
				throw new UnknownEntityException(
						"entity [" + entityType.getId() + "] with " + entityType.getIdAttribute().getName() + " ["
								+ id.toString() + "] does not exist");
			}
		}
		return entity;
	}

	@Override
	public String toString()
	{
		if (entity != null)
		{
			return entity.toString();
		}
		else
		{
			return entityType.getId() + '{' + entityType.getIdAttribute().getName() + '=' + id
					+ ",<lazy attributes not loaded>}";
		}
	}
}