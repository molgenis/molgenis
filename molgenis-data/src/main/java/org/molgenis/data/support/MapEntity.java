package org.molgenis.data.support;

import java.util.HashMap;
import java.util.Map;

import org.molgenis.data.Entity;

/**
 * Simple Entity implementation based on a Map
 */
public class MapEntity implements Entity
{
	private static final long serialVersionUID = -8283375007931769373L;
	private final Map<String, Object> values = new HashMap<String, Object>();

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

}
