package org.molgenis.api.metadata.v3;

import org.molgenis.api.model.Query;
import org.molgenis.api.model.Sort;
import org.molgenis.data.meta.model.EntityType;

public interface MetadataApiService {

  void createEntityType(EntityType entityType);

  EntityType findEntityType(String entityTypeId);

  EntityTypes findEntityTypes(Query orElse, Sort sort, int size, int page);

  Attributes findAttributes(String entityTypeId, Query orElse, Sort sort, int size, int page);

  void deleteEntityType(String entityTypeId);

  void deleteEntityTypes(Query q);
}
