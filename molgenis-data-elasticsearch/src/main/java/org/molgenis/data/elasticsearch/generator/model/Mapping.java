package org.molgenis.data.elasticsearch.generator.model;

import static java.util.Collections.emptyList;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class Mapping {
  public abstract String getType();

  public abstract List<FieldMapping> getFieldMappings();

  public static Mapping create(String newType, List<FieldMapping> newFieldMappings) {
    return builder().setType(newType).setFieldMappings(newFieldMappings).build();
  }

  public static Builder builder() {
    return new AutoValue_Mapping.Builder().setFieldMappings(emptyList());
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setType(String newType);

    public abstract Builder setFieldMappings(List<FieldMapping> newFieldMappings);

    public abstract Mapping build();
  }
}
