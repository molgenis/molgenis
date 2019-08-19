package org.molgenis.api.data.v3;

import java.util.Optional;
import org.molgenis.api.data.v3.model.EntitiesResponse;
import org.molgenis.api.data.v3.model.EntityResponse;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Selection;
import org.molgenis.api.model.Sort;
import org.molgenis.data.Entity;

public interface EntityMapper {
  EntityResponse map(Entity entity, Selection filter, Selection expand);

  EntitiesResponse map(
      String entityTypeId,
      String entityId,
      String fieldId,
      EntityCollection entityCollection,
      Selection filter,
      Selection expand,
      Optional<Query> query,
      Sort sort,
      int size,
      int number,
      int total);

  EntitiesResponse map(
      EntityCollection entityCollection,
      Selection filter,
      Selection expand,
      Optional<Query> query,
      Sort sort,
      int size,
      int number,
      int total);
}
