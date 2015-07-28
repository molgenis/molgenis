package org.molgenis.framework.server;

public interface MolgenisSettings
{
	public String getProperty(String key);

	public String getProperty(String key, String defaultValue);

	public void setProperty(String key, String value);

	public Boolean getBooleanProperty(String key);

	public boolean getBooleanProperty(String key, boolean defaultValue);
}
