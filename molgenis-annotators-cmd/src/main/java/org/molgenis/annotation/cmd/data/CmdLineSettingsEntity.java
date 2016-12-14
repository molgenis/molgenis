package org.molgenis.annotation.cmd.data;

import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.sql.Timestamp;
import java.util.Map;

public class CmdLineSettingsEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	private final Map<String, Object> values = new LinkedCaseInsensitiveMap<>();

	@Override
	public EntityType getEntityType()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getIdValue()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIdValue(Object id)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getLabelValue()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get(String attributeName)
	{
		return getString(attributeName);
	}

	@Override
	public String getString(String attributeName)
	{
		return DataConverter.toString(values.get(attributeName));
	}

	@Override
	public Integer getInt(String attributeName)
	{
		return DataConverter.toInt(values.get(attributeName));
	}

	@Override
	public Long getLong(String attributeName)
	{
		return DataConverter.toLong(values.get(attributeName));
	}

	@Override
	public Boolean getBoolean(String attributeName)
	{
		return DataConverter.toBoolean(values.get(attributeName));
	}

	@Override
	public Double getDouble(String attributeName)
	{
		return DataConverter.toDouble(values.get(attributeName));
	}

	@Override
	public java.sql.Date getDate(String attributeName)
	{
		java.util.Date utilDate = getUtilDate(attributeName);
		return utilDate != null ? new java.sql.Date(utilDate.getTime()) : null;
	}

	@Override
	public java.util.Date getUtilDate(String attributeName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Timestamp getTimestamp(String attributeName)
	{
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Entity getEntity(String attributeName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(String attributeName, Object value)
	{
		values.put(attributeName, value);
	}

	@Override
	public void set(Entity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString()
	{
		return getLabelValue();
	}
}