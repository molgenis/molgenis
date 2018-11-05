package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ResourceIdentifier.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class ResourceIdentifier {

  public abstract ResourceType getType();

  public abstract String getId();

  public static ResourceIdentifier create(ResourceType newType, String newId) {
    return builder().setType(newType).setId(newId).build();
  }

  public static Builder builder() {
    return new AutoValue_ResourceIdentifier.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setType(ResourceType newType);

    public abstract Builder setId(String newId);

    public abstract ResourceIdentifier build();
  }
}
