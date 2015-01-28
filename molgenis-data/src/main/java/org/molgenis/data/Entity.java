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
	EntityMetaData getEntityMetaData();

	/**
	 * Get all attribute names
	 */
	Iterable<String> getAttributeNames();

	/**
	 * Optional unique id to identify this Entity. Otherwise return null
	 */
	Object getIdValue();

	/**
	 * Optional human readable label to recognize this Entity. Otherwise return null
	 */
	String getLabelValue();

	/**
	 * Get attribute value
	 */
	Object get(String attributeName);

	/**
	 * Retrieves the value of the designated column as String.
	 */
	String getString(String attributeName);

	/**
	 * Retrieves the value of the designated column as Integer.
	 */
	Integer getInt(String attributeName);

	/**
	 * Retrieves the value of the designated column as Long.
	 */
	Long getLong(String attributeName);

	/**
	 * Retrieves the value of the designated column as Boolean.
	 */
	Boolean getBoolean(String attributeName);

	/**
	 * Retrieves the value of the designated column as Double.
	 */
	Double getDouble(String attributeName);

	/**
	 * Retrieves the value of the designated column as {@link java.sql.Date}.
	 */
	java.sql.Date getDate(String attributeName);

	/**
	 * Retrieves the value of the designated column as {@link java.util.Date}.
	 */
	java.util.Date getUtilDate(String attributeName);

	/**
	 * Retrieves the value of the designated column as {@link java.sql.Timestamp}.
	 */
	Timestamp getTimestamp(String attributeName);

	/**
	 * Retrieves the value of the designated column as entity
	 */
	Entity getEntity(String attributeName);

	/**
	 * Retrieves the value of the designated column as entity of the give type
	 */
	<E extends Entity> E getEntity(String attributeName, Class<E> clazz);

	/**
	 * Retrieves the value of the designated column as a entity iterable
	 */
	Iterable<Entity> getEntities(String attributeName);

	/**
	 * Retrieves the value of the designated column as a entity of the given type iterable
	 */
	<E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz);

	/**
	 * Retrieves the value of the designated column as List<String>.
	 */
	List<String> getList(String attributeName);

	/**
	 * Retrieves the value of the designated column as List<Integer>
	 */
	List<Integer> getIntList(String attributeName);

	/**
	 * Change attribute value
	 */
	void set(String attributeName, Object value);

	/**
	 * Copy attribute values from another entity
	 */
	void set(Entity values);
}
