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
	private String idAttributeName = null;

	public MapEntity()
	{
	}

	public MapEntity(Entity other)
	{
		set(other);
	}

	public MapEntity(String idAttributeName)
	{
		this.idAttributeName = idAttributeName;
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
		Object value = values.get(attributeName);
		if (value == null)
		{
			value = values.get(attributeName.toLowerCase());
		}

		return value;
	}

	@Override
	public void set(String attributeName, Object value)
	{
		values.put(attributeName, value);
	}

	@Override
	public void set(Entity other)
	{
		for (String attributeName : other.getAttributeNames())
		{
			set(attributeName, other.get(attributeName));
		}
	}

	@Override
	public Integer getIdValue()
	{
		if (idAttributeName == null)
		{
			return null;
		}

		return (Integer) get(idAttributeName);
	}

	@Override
	public String getLabelValue()
	{
		return null;
	}

	public String getIdAttributeName()
	{
		return idAttributeName;
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
