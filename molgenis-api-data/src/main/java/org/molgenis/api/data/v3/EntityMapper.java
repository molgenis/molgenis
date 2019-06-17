package org.molgenis.api.data.v3;

import org.molgenis.data.Entity;

public interface EntityMapper {
  EntityResponse map(Entity entity, Selection filterSelection, Selection expandSelection);

  // EntitiesResponse map(Entity entity, Selection filterSelection, Selection expandSelection);
}
