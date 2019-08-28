package org.molgenis.api.data.v3;

import java.util.Map;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;

interface EntityManagerV3 {
  Entity create(EntityType entityType);

  void populate(EntityType entityType, Entity entity, Map<String, Object> requestValues);
}
