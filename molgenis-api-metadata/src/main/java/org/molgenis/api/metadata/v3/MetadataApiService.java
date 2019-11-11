package org.molgenis.api.metadata.v3;

import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecution;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Sort;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

public interface MetadataApiService {

  void createEntityType(EntityType entityType);

  EntityType findEntityType(String entityTypeId);

  EntityTypes findEntityTypes(Query orElse, Sort sort, int size, int page);

  Attributes findAttributes(String entityTypeId, Query orElse, Sort sort, int size, int page);

  /**
   * @param attributeId attribute identifier
   * @throws UnknownEntityException if no attribute exists for the given identifier
   */
  Attribute findAttribute(String attributeId);

  MetadataUpsertJobExecution deleteAttribute(String entityTypeId, String attributeId);

  MetadataUpsertJobExecution deleteAttributes(String entityTypeId, Query query);

  /**
   * @param entityTypeId entity type identifier
   * @throws UnknownEntityTypeException if no entity type exists for the given identifier
   * @return MetadataDeleteJobExecution
   */
  MetadataDeleteJobExecution deleteEntityType(String entityTypeId);

  MetadataDeleteJobExecution deleteEntityTypes(Query query);

  /**
   * Updates entity type.
   *
   * @param entityType updated entity type
   */
  MetadataUpsertJobExecution updateEntityType(EntityType entityType);
}
