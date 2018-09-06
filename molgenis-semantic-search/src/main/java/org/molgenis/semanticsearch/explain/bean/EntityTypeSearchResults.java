package org.molgenis.semanticsearch.explain.bean;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.molgenis.data.meta.model.EntityType;

@AutoValue
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EntityTypeSearchResults {
  public abstract EntityType getEntityType();

  public abstract List<AttributeSearchResults> getAttributeSearchResults();

  public static EntityTypeSearchResults create(
      EntityType entityType, List<AttributeSearchResults> attributeSearchResults) {
    return new AutoValue_EntityTypeSearchResults(
        entityType, ImmutableList.copyOf(attributeSearchResults));
  }
}
