package org.molgenis.data.aggregation;

import javax.annotation.CheckForNull;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.Attribute;

public interface AggregateQuery {
  @CheckForNull
  Query<Entity> getQuery();

  @CheckForNull
  Attribute getAttributeX();

  @CheckForNull
  Attribute getAttributeY();

  @CheckForNull
  Attribute getAttributeDistinct();
}
