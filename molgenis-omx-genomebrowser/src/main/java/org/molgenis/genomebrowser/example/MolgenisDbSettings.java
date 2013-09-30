package org.molgenis.genomebrowser.example;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.molgenis.framework.server.MolgenisSettings;

public class MolgenisDbSettings implements MolgenisSettings
{
	private static final Logger logger = Logger.getLogger(MolgenisDbSettings.class);
	private Properties properties = new Properties();

	@Override
	public String getProperty(String key)
	{
		return getProperty(key, null);
	}

	@Override
	public String getProperty(String key, String defaultValue)
	{
		String property = this.properties.getProperty(key);
		if (property == null)
		{
			logger.warn("property '" + key + "' is null");
			return defaultValue;
		}
		return property;
	}

	@Override
	public void setProperty(String key, String value)
	{
		this.properties.put(key,value);
	}

	@Override
	public Boolean getBooleanProperty(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getBooleanProperty(String key, boolean defaultValue) {
		// TODO Auto-generated method stub
		return false;
	}

}
