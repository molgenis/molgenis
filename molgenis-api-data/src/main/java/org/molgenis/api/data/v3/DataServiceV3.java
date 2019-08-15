package org.molgenis.api.data.v3;

import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Selection;
import org.molgenis.api.model.Sort;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownRepositoryException;
import org.springframework.transaction.annotation.Transactional;

/** Data API v3 CRUD operations. */
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

  @Transactional(readOnly = true)
  Entities findSubresources(
      String entityTypeId,
      String entityId,
      String attributeName,
      @Nullable @CheckForNull Query query,
      Selection filter,
      Selection expand,
      Sort sort,
      int size,
      int number);

  /**
   * Retrieves all entities matching a query.
   *
   * @param entityTypeId entity type identifier
   * @param query query specifying which entities to find, or null
   * @param filter selection describing the filter attributes
   * @param expand selection describing the expansion attributes
   * @param sort entities sort criteria
   * @param size maximum number of entities to return
   * @param number page number
   * @return entities, never null
   * @throws UnknownRepositoryException if no repository exists for the given entity type identifier
   */
  Entities findAll(
      String entityTypeId,
      @Nullable @CheckForNull Query query,
      Selection filter,
      Selection expand,
      Sort sort,
      int size,
      int number);

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

  /**
   * Deletes all entities matching a query.
   *
   * @param entityTypeId entity type identifier
   * @param query query specifying which entities to delete, or null
   * @throws UnknownRepositoryException if no repository exists for the given entity type identifier
   */
  void deleteAll(String entityTypeId, @Nullable @CheckForNull Query query);
}
