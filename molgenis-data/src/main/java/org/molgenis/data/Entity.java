package org.molgenis.data;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.i18n.Identifiable;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Entity is a data record which can contain a hash of attribute values. Attribute names are unique. Synonyms are
 * ‘tuple’, ‘record’, ‘row’, ‘hashmap’. Optionally Entity can provide a unique ‘id’ for updates. Optionally Entity can
 * provide a human readable label for lookups
 */
public interface Entity extends Identifiable
{
	/**
	 * Returns entity meta data
	 *
	 * @return entity meta data, never null
	 */
	EntityType getEntityType();

	/**
	 * Get all attribute names
	 * <p>
	 * TODO remove, use getEntityType to retrieve entity meta data
	 */
	Iterable<String> getAttributeNames();

	/**
	 * Optional unique id to identify this Entity. Otherwise return null
	 * <p>
	 * // TODO getIdValue should return id of type of entity (requires generic on Entity)
	 */
	Object getIdValue();

	/**
	 * Sets the identifier value of this entity. The class type of the id is based on the id attribute data type.
	 *
	 * @param id identifier value
	 */
	void setIdValue(Object id);

	/**
	 * Optional human readable label to recognize this Entity. Otherwise return null
	 */
	Object getLabelValue();

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
	 * Retrieves the value of the designated column as {@link java.time.Instant}.
	 */
	Instant getInstant(String attributeName);

	/**
	 * Retrieves the value of the designated column as {@link java.time.LocalDate}.
	 */
	LocalDate getLocalDate(String attributeName);

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
	 * Change attribute value
	 */
	void set(String attributeName, Object value);

	/**
	 * Copy attribute values from another entity
	 * <p>
	 * TODO remove method, move to utility class
	 */
	void set(Entity values);
}
