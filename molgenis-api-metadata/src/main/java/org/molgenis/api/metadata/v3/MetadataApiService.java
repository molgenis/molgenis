package org.molgenis.api.metadata.v3;

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

  /**
   * @param entityTypeId entity type identifier
   * @throws UnknownEntityTypeException if no entity type exists for the given identifier
   */
  void deleteEntityType(String entityTypeId);

  void deleteEntityTypes(Query q);
}
