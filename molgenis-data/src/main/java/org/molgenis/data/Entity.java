package org.molgenis.data;

import java.time.Instant;
import java.time.LocalDate;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.i18n.Identifiable;

/**
 * Entity is a data record which can contain a hash of attribute values. Attribute names are unique.
 * Synonyms are ‘tuple’, ‘record’, ‘row’, ‘hashmap’. Optionally Entity can provide a unique ‘id’ for
 * updates. Optionally Entity can provide a human readable label for lookups
 */
public interface Entity extends Identifiable {
  /**
   * Returns entity meta data
   *
   * @return entity meta data, never null
   */
  EntityType getEntityType();

  /**
   * Get all attribute names
   *
   * <p>TODO remove, use getEntityType to retrieve entity meta data
   */
  Iterable<String> getAttributeNames();

  /**
   * Optional unique id to identify this Entity. Otherwise return null
   *
   * <p>// TODO getIdValue should return id of type of entity (requires generic on Entity)
   */
  Object getIdValue();

  /**
   * Sets the identifier value of this entity. The class type of the id is based on the id attribute
   * data type.
   *
   * @param id identifier value
   */
  void setIdValue(Object id);

  /** Optional human readable label to recognize this Entity. Otherwise return null */
  Object getLabelValue();

  /** Get attribute value */
  Object get(String attributeName);

  default Object get(Attribute attribute) {
    return get(attribute.getName());
  }

  /** Retrieves the value of the designated column as String. */
  String getString(String attributeName);

  default String getString(Attribute attribute) {
    return getString(attribute.getName());
  }

  /** Retrieves the value of the designated column as Integer. */
  Integer getInt(String attributeName);

  default Integer getInt(Attribute attribute) {
    return getInt(attribute.getName());
  }

  /** Retrieves the value of the designated column as Long. */
  Long getLong(String attributeName);

  default Long getLong(Attribute attribute) {
    return getLong(attribute.getName());
  }

  /** Retrieves the value of the designated column as Boolean. */
  Boolean getBoolean(String attributeName);

  default Boolean getBoolean(Attribute attribute) {
    return getBoolean(attribute.getName());
  }

  /** Retrieves the value of the designated column as Double. */
  Double getDouble(String attributeName);

  default Double getDouble(Attribute attribute) {
    return getDouble(attribute.getName());
  }

  /** Retrieves the value of the designated column as {@link java.time.Instant}. */
  Instant getInstant(String attributeName);

  default Instant getInstant(Attribute attribute) {
    return getInstant(attribute.getName());
  }

  /** Retrieves the value of the designated column as {@link java.time.LocalDate}. */
  LocalDate getLocalDate(String attributeName);

  default LocalDate getLocalDate(Attribute attribute) {
    return getLocalDate(attribute.getName());
  }

  /** Retrieves the value of the designated column as entity */
  Entity getEntity(String attributeName);

  default Entity getEntity(Attribute attribute) {
    return getEntity(attribute.getName());
  }

  /** Retrieves the value of the designated column as entity of the give type */
  <E extends Entity> E getEntity(String attributeName, Class<E> clazz);

  default <E extends Entity> E getEntity(Attribute attribute, Class<E> clazz) {
    return getEntity(attribute.getName(), clazz);
  }

  /** Retrieves the value of the designated column as a entity iterable */
  Iterable<Entity> getEntities(String attributeName);

  default Iterable<Entity> getEntities(Attribute attribute) {
    return getEntities(attribute.getName());
  }

  /** Retrieves the value of the designated column as a entity of the given type iterable */
  <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz);

  default <E extends Entity> Iterable<E> getEntities(Attribute attribute, Class<E> clazz) {
    return getEntities(attribute.getName(), clazz);
  }

  /** Change attribute value */
  void set(String attributeName, Object value);

  default void set(Attribute attribute, Object value) {
    set(attribute.getName(), value);
  }

  /**
   * Copy attribute values from another entity
   *
   * <p>TODO remove method, move to utility class
   */
  void set(Entity values);
}
