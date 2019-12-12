package org.molgenis.api.metadata.v3;

import java.util.Map;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.data.meta.model.EntityType;

public interface EntityTypeRequestMapper {
  /**
   * Creates a new entity type from a entity type request.
   *
   * @param entityTypeRequest entity type request
   * @return new entity type
   */
  EntityType toEntityType(CreateEntityTypeRequest entityTypeRequest);

  /**
   * Updates entity type in-place.
   *
   * @param entityType entity type to update
   * @param entityTypeValues entity type values to apply
   */
  void updateEntityType(EntityType entityType, Map<String, Object> entityTypeValues);
}
