package org.molgenis.framework.server;

import java.util.Map;

public interface MolgenisSettings
{
	String getProperty(String key);

	String getProperty(String key, String defaultValue);

	void setProperty(String key, String value);

	Boolean getBooleanProperty(String key);

	boolean getBooleanProperty(String key, boolean defaultValue);
	
	boolean updateProperty(String key, String value);

	boolean propertyExists(String key);
	
	/**
	 * Fetches properties with a key starting with a certain value.
	 * @param keyStartsWith String that the key must start with.
	 * @returns Map containing the properties found. 
	 * 	The param @keyStartsWith is trimmed from the property keys. 
	 */
	Map<String, String> getProperties(String keyStartsWith);
}
