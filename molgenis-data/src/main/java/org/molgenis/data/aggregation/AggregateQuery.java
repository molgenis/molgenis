package org.molgenis.data.aggregation;

import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.Attribute;

public interface AggregateQuery {
  @Nullable
  Query<Entity> getQuery();

  @Nullable
  Attribute getAttributeX();

  @Nullable
  Attribute getAttributeY();

  @Nullable
  Attribute getAttributeDistinct();
}
