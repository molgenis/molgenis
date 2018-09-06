package org.molgenis.security.core.model;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

@AutoValue
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class PackageValue {
  public abstract String getName();

  public abstract String getLabel();

  @Nullable
  public abstract String getDescription();

  public static Builder builder() {
    return new AutoValue_PackageValue.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setName(String value);

    public abstract Builder setLabel(String value);

    public abstract Builder setDescription(String value);

    public abstract PackageValue build();
  }
}
