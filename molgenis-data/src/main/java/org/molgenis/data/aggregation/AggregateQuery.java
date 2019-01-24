package org.molgenis.data.aggregation;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.Attribute;

public interface AggregateQuery {
  @Nullable
  @CheckForNull
  Query<Entity> getQuery();

  @Nullable
  @CheckForNull
  Attribute getAttributeX();

  @Nullable
  @CheckForNull
  Attribute getAttributeY();

  @Nullable
  @CheckForNull
  Attribute getAttributeDistinct();
}
