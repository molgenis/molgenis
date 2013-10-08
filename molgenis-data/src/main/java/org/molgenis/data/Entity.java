package org.molgenis.data;

import java.io.Serializable;

/**
 * Entity is a data record which can contain a hash of attribute values. Attribute names are unique. Synonyms are
 * ‘tuple’, ‘record’, ‘row’, ‘hashmap’. Optionally Entity can provide a unique ‘id’ for updates. Optionally Entity can
 * provide a human readable label for lookups
 */
public interface Entity extends Serializable
{
	/**
	 * Get attribute value
	 */
	Object get(String attributeName);

	/**
	 * Change attribute value
	 */
	void set(String attributeName, Object value);

	/**
	 * Copy attribute values from another entity
	 */
	void set(Entity values);

	/**
	 * Optional unique id to identify this Entity. Otherwise return null
	 */
	Integer getIdValue();

	/**
	 * Optional human readable label to recognize this Entity. Otherwise return null
	 */
	String getLabelValue();
}
