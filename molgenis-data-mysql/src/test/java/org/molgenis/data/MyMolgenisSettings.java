package org.molgenis.data;

import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.stereotype.Component;

@Component
public class MyMolgenisSettings implements MolgenisSettings
{
	@Override
	public String getProperty(String key)
	{
		return null;
	}

	@Override
	public String getProperty(String key, String defaultValue)
	{
		return null;
	}

	@Override
	public void setProperty(String key, String value)
	{

	}

	@Override
	public Boolean getBooleanProperty(String key)
	{
		return null;
	}

	@Override
	public boolean getBooleanProperty(String key, boolean defaultValue)
	{
		return false;
	}

	@Override
	public boolean updateProperty(String key, String value)
	{
		return false;
	}

	@Override
	public boolean propertyExists(String key)
	{
		return false;
	}
}
