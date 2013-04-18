package org.molgenis.compute.db.util;

import org.molgenis.framework.server.MolgenisSettings;

/**
 * Implementation of MolgenisSettings. Simply returns the default value.
 * 
 * We don't use MolgenisDbSettings so that we don't need a dependency on
 * omx-core
 * 
 * @author erwin
 * 
 */
public class ComputeMolgenisSettings implements MolgenisSettings
{

	@Override
	public String getProperty(String key)
	{
		throw new IllegalArgumentException("Unknown key [" + key + "], please implement in ComputeMolgenisSettings");
	}

	@Override
	public String getProperty(String key, String defaultValue)
	{
		return defaultValue;
	}

	@Override
	public void setProperty(String key, String value)
	{

	}

}
