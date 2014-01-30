package org.molgenis.data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * Entity is a data record which can contain a hash of attribute values. Attribute names are unique. Synonyms are
 * ‘tuple’, ‘record’, ‘row’, ‘hashmap’. Optionally Entity can provide a unique ‘id’ for updates. Optionally Entity can
 * provide a human readable label for lookups
 */
public interface Entity extends Serializable
{
	/**
	 * Get all attribute names
	 */
	Iterable<String> getAttributeNames();

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

	public String getString(String attributeName);

	/**
	 * Retrieves the value of the designated column as Integer.
	 */
	public Integer getInt(String attributeName);

	/**
	 * Retrieves the value of the designated column as Long.
	 */
	public Long getLong(String attributeName);

	/**
	 * Retrieves the value of the designated column as Boolean.
	 */
	public Boolean getBoolean(String attributeName);

	/**
	 * Retrieves the value of the designated column as Double.
	 */
	public Double getDouble(String attributeName);

	/**
	 * Retrieves the value of the designated column as {@link java.sql.Date}.
	 */
	public java.sql.Date getDate(String attributeName);

	/**
	 * Retrieves the value of the designated column as {@link java.util.Date}.
	 */
	public java.util.Date getUtilDate(String attributeName);

	/**
	 * Retrieves the value of the designated column as {@link java.sql.Timestamp}.
	 */
	public Timestamp getTimestamp(String attributeName);

	/**
	 * Retrieves the value of the designated column as List<?>.
	 */
	public List<String> getList(String attributeName);

	/**
	 * Retrieves the value of the designated column as List<Integer>
	 */
	public List<Integer> getIntList(String attributeName);

	List<String> getLabelAttributeNames();

	void set(Entity entity, boolean strict);

	EntityMetaData getEntityMetaData();
}
