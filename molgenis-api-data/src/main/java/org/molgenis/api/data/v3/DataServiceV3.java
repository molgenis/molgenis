package org.molgenis.api.data.v3;

import java.util.Map;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownRepositoryException;

interface DataServiceV3 {
  /**
   * Create one entity.
   *
   * @param entityTypeId entity type identifier
   * @param requestValues entity values
   * @return entity. never null
   * @throws UnknownRepositoryException if no repository exists for the given entity type identifier
   */
  Entity create(String entityTypeId, Map<String, Object> requestValues);

  /**
   * Retrieve one entity.
   *
   * @param entityTypeId entity type identifier
   * @param entityId untyped entity identifier
   * @param filter selection describing the filter attributes
   * @param expand selection describing the expansion attributes
   * @return entity, never null
   * @throws UnknownRepositoryException if no repository exists for the given entity type identifier
   * @throws UnknownEntityException if no entity exists for the given entity identifier
   */
  Entity find(String entityTypeId, String entityId, Selection filter, Selection expand);

  /**
   * Update one entity.
   *
   * @param entityTypeId entity type identifier
   * @param entityId untyped entity identifier
   * @throws UnknownRepositoryException if no repository exists for the given entity type identifier
   * @throws UnknownEntityException if no entity exists for the given entity identifier
   */
  void update(String entityTypeId, String entityId, Map<String, Object> requestValues);

  /**
   * Partial update of one entity.
   *
   * @param entityTypeId entity type identifier
   * @param entityId untyped entity identifier
   * @throws UnknownRepositoryException if no repository exists for the given entity type identifier
   * @throws UnknownEntityException if no entity exists for the given entity identifier
   */
  void updatePartial(String entityTypeId, String entityId, Map<String, Object> requestValues);

  /**
   * Delete one entity.
   *
   * @param entityTypeId entity type identifier
   * @param entityId untyped entity identifier
   * @throws UnknownRepositoryException if no repository exists for the given entity type identifier
   * @throws UnknownEntityException if no entity exists for the given entity identifier
   */
  void delete(String entityTypeId, String entityId);
}
