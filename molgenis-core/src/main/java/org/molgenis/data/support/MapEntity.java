package org.molgenis.data.support;

import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.data.Entity;

/**
 * Simple Entity implementation based on a Map
 */
public class MapEntity extends AbstractEntity
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
}
