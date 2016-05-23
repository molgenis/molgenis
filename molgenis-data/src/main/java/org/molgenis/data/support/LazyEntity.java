package org.molgenis.data.support;

import static java.util.Objects.requireNonNull;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.UnknownEntityException;

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
	public String getLabelValue()
	{
		AttributeMetaData idAttr = getEntityMetaData().getIdAttribute();
		AttributeMetaData labelAttr = getEntityMetaData().getLabelAttribute();
		if (idAttr.equals(labelAttr))
		{
			return DataConverter.toString(getIdValue());
		}
		else
		{
			return getLazyLoadedEntity().getLabelValue();
		}
	}

	@Override
	public Object get(String attributeName)
	{
		AttributeMetaData idAttr = entityMetaData.getIdAttribute();
		if (attributeName.equals(idAttr.getName()))
		{
			return getIdValue();
		}
		return getLazyLoadedEntity().get(attributeName);
	}

	@Override
	public String getString(String attributeName)
	{
		AttributeMetaData idAttr = entityMetaData.getIdAttribute();
		if (attributeName.equals(idAttr.getName()))
		{
			return DataConverter.toString(getIdValue());
		}
		return getLazyLoadedEntity().getString(attributeName);
	}

	@Override
	public Integer getInt(String attributeName)
	{
		AttributeMetaData idAttr = entityMetaData.getIdAttribute();
		if (attributeName.equals(idAttr.getName()))
		{
			return DataConverter.toInt(getIdValue());
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
	public List<String> getList(String attributeName)
	{
		return getLazyLoadedEntity().getList(attributeName);
	}

	@Override
	public List<Integer> getIntList(String attributeName)
	{
		return getLazyLoadedEntity().getIntList(attributeName);
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
			entity = dataService.findOne(getEntityMetaData().getName(), id);
			if (entity == null)
			{
				throw new UnknownEntityException("entity [" + getEntityMetaData().getName() + "] with "
						+ getEntityMetaData().getIdAttribute().getName() + " [" + getIdValue().toString()
						+ "] does not exist");
			}
		}
		return entity;
	}
}