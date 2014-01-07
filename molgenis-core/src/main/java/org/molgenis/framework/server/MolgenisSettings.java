package org.molgenis.framework.server;

public interface MolgenisSettings
{
	String getProperty(String key);

	String getProperty(String key, String defaultValue);

	void setProperty(String key, String value);

	Boolean getBooleanProperty(String key);

	boolean getBooleanProperty(String key, boolean defaultValue);
	
	boolean updateProperty(String key, String value);

	boolean propertyExists(String key);
}
