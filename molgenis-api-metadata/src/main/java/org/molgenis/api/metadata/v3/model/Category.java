package org.molgenis.api.metadata.v3.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Category {
  public abstract Object getId();

  public abstract String getLabel();

  public static Category create(Object newId, String newLabel) {
    return builder().setId(newId).setLabel(newLabel).build();
  }

  public static Builder builder() {
    return new AutoValue_Category.Builder();
  }

  // Abstract classes without fields should be converted to interfaces
  @SuppressWarnings("java:S1610")
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(Object newId);

    public abstract Builder setLabel(String newLabel);

    public abstract Category build();
  }
}
