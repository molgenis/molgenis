package org.molgenis.api.data.v3;

import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownRepositoryException;

interface DataServiceV3 {
  /**
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
   * @param entityTypeId entity type identifier
   * @param entityId untyped entity identifier
   * @throws UnknownRepositoryException if no repository exists for the given entity type identifier
   * @throws UnknownEntityException if no entity exists for the given entity identifier
   */
  void delete(String entityTypeId, String entityId);
}
