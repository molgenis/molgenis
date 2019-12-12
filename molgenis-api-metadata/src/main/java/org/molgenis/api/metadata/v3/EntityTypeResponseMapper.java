package org.molgenis.api.metadata.v3;

import org.molgenis.api.metadata.v3.model.EntityTypeResponse;
import org.molgenis.api.metadata.v3.model.EntityTypesResponse;
import org.molgenis.data.meta.model.EntityType;

public interface EntityTypeResponseMapper {
  /**
   * Creates an entity type response from an entity type.
   *
   * @param entityType entity type to map
   * @param flattenAttributes whether to include attributes from parents
   * @param i18n whether to include localized labels and descriptions
   * @return entity types response
   */
  EntityTypeResponse toEntityTypeResponse(
      EntityType entityType, boolean flattenAttributes, boolean i18n);

  /**
   * Creates an entity types response from entity types.
   *
   * @param entityTypes entity types to map
   * @param size number of entity types
   * @param page page number
   * @return entity types response
   */
  EntityTypesResponse toEntityTypesResponse(EntityTypes entityTypes, int size, int page);
}
