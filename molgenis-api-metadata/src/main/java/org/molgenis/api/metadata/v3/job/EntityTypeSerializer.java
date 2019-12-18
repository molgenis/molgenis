package org.molgenis.api.metadata.v3.job;

import org.molgenis.data.meta.model.EntityType;

public interface EntityTypeSerializer {
  String serializeEntityType(EntityType entityType);

  EntityType deserializeEntityType(String serializedEntityType);
}
