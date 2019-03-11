package org.molgenis.data;

import java.util.stream.Stream;
import org.molgenis.data.meta.model.EntityType;

/**
 * Entity manager responsible for creating entity references and resolving references of reference
 * attributes.
 */
public interface EntityManager {
  /**
   * Creation mode that defines whether or not to populate data with auto generated ando/or default
   * values.
   */
  enum CreationMode {
    POPULATE,
    NO_POPULATE
  }

  /**
   * Creates an entity.
   *
   * @param entityType entity meta data
   * @param creationMode entity creation mode that defines initial population mode
   * @return new entity
   */
  Entity create(EntityType entityType, CreationMode creationMode);

  /**
   * Creates an entity based on the given fetch.
   *
   * @param entityType entity meta data
   * @param fetch entity data fetch
   * @return new entity
   */
  Entity createFetch(EntityType entityType, Fetch fetch);

  /**
   * Get an instance, whose state may be lazily fetched. If the requested instance does not exist in
   * the repository, an <code>UnknownEntityException</code> is thrown when the instance state is
   * first accessed.
   *
   * @param entityType entity meta data
   * @param id entity identifier
   * @return entity
   */
  Entity getReference(EntityType entityType, Object id);

  /**
   * Get instances, whose state may be lazily fetched. If a requested instance does not exist in the
   * repository, an <code>UnknownEntityException</code> is thrown when the instance state is first
   * accessed.
   *
   * @param entityType entity meta data
   * @param ids entity identifiers
   * @return data
   */
  Iterable<Entity> getReferences(EntityType entityType, Iterable<?> ids);

  /**
   * Resolve data referenced by a given entity based on provided fetch information. Given entity is
   * modified by setting references.
   *
   * @param entityType entity meta data
   * @param entity entity
   * @param fetch entity data fetch
   * @return entity with resolved references
   */
  Entity resolveReferences(EntityType entityType, Entity entity, Fetch fetch);

  /**
   * Resolve data referenced by a given list of data based on provided fetch information. Given data
   * are modified by setting references.
   *
   * @param entityType entity meta data
   * @param entities data
   * @param fetch entity data fetch
   * @return data with resolved references
   */
  Stream<Entity> resolveReferences(EntityType entityType, Stream<Entity> entities, Fetch fetch);
}
