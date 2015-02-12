package org.molgenis.framework.server;

import java.util.HashMap;
import java.util.Map;


/**
 * Simple in-memory implementation of MolgenisSettings.
 * Helpful to create command-line standalone JARs using the
 * MolgenisSettings structure without access to a database
 * 
 * @author jvelde
 * 
 */
public class MolgenisSimpleSettings implements MolgenisSettings
{

	Map<String, Object> settings = new HashMap<String, Object>();

	@Override
	public String getProperty(String key)
	{
		return String.valueOf(settings.get(key));
	}

	@Override
	public String getProperty(String key, String defaultValue)
	{
		return settings.containsKey(key) ? String.valueOf(settings.get(key)) : defaultValue;
	}

	@Override
	public void setProperty(String key, String value)
	{
		settings.put(key, value);
	}

	@Override
	public Boolean getBooleanProperty(String key)
	{
		return Boolean.valueOf(String.valueOf(settings.get(key)));
	}

	@Override
	public Integer getIntegerProperty(String key)
	{
		return Integer.getInteger(String.valueOf(settings.get(key)));
	}

	@Override
	public boolean getBooleanProperty(String key, boolean defaultValue)
	{
		return settings.containsKey(key) ? Boolean.valueOf(String.valueOf(settings.get(key))) : defaultValue;
	}

	@Override
	public boolean updateProperty(String key, String value)
	{
		if (settings.containsKey(key))
		{
			settings.put(key, value);
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean propertyExists(String key)
	{
		if (settings.containsKey(key))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public Map<String, String> getProperties(String keyStartsWith)
	{
		Map<String, String> result = new HashMap<String, String>();
		for (String key : settings.keySet())
		{
			if (key.startsWith(keyStartsWith))
			{
				result.put(key, String.valueOf(settings.get(key)));
			}
		}
		return result;
	}

}
