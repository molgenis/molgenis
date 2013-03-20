package org.molgenis.framework.server;

public interface MolgenisSettings
{
	public String getProperty(String key);

	public String getProperty(String key, String defaultValue);

	public void setProperty(String key, String value);
}
