package org.molgenis.data.elasticsearch;

import static org.molgenis.data.util.EntityTypeUtils.isReferenceType;

import org.molgenis.data.meta.model.Attribute;

public class AggregateUtils {
  private AggregateUtils() {}

  public static boolean isNestedType(Attribute attr) {
    return isReferenceType(attr);
  }
}
