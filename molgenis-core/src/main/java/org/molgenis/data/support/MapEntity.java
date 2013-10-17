package org.molgenis.data.support;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;

/**
 * Simple Entity implementation based on a Map
 */
public class MapEntity implements Entity
{
	private static final long serialVersionUID = -8283375007931769373L;
	private Map<String, Object> values = new LinkedHashMap<String, Object>();

	public MapEntity()
	{
	}

	public MapEntity(Map<String, Object> values)
	{
		this.values = values;
	}

	public MapEntity(String attributeName, Object value)
	{
		values.put(attributeName, value);
	}

	@Override
	public Object get(String attributeName)
	{
		return values.get(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		values.put(attributeName, value);
	}

	@Override
	public void set(Entity other)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Integer getIdValue()
	{
		return null;
	}

	@Override
	public String getLabelValue()
	{
		return null;
	}

	@Override
	public String toString()
	{
		return values.toString();
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return values.keySet();
	}

	@Override
	public String getString(String attributeName)
	{
		return DataConverter.toString(get(attributeName));
	}

	@Override
	public Integer getInt(String attributeName)
	{
		return DataConverter.toInt(get(attributeName));
	}

	@Override
	public Long getLong(String attributeName)
	{
		return DataConverter.toLong(get(attributeName));
	}

	@Override
	public Boolean getBoolean(String attributeName)
	{
		return DataConverter.toBoolean(get(attributeName));
	}

	@Override
	public Double getDouble(String attributeName)
	{
		return DataConverter.toDouble(get(attributeName));
	}

	@Override
	public Date getDate(String attributeName)
	{
		return DataConverter.toDate(get(attributeName));
	}

	@Override
	public Timestamp getTimestamp(String attributeName)
	{
		return DataConverter.toTimestamp(get(attributeName));
	}

	@Override
	public List<String> getList(String attributeName)
	{
		return DataConverter.toList(get(attributeName));
	}

	@Override
	public List<Integer> getIntList(String attributeName)
	{
		return DataConverter.toIntList(get(attributeName));
	}

}
