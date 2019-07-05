package org.molgenis.api.data.v3;

import org.molgenis.api.model.Selection;
import org.molgenis.data.Entity;

public interface EntityMapper {
  EntityResponse map(Entity entity, Selection filter, Selection expand);

  EntitiesResponse map(EntityCollection entityCollection, Selection filter, Selection expand);
}
