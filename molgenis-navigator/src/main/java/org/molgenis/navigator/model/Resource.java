package org.molgenis.navigator.model;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Resource.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class Resource {

  public abstract ResourceType getType();

  public abstract String getId();

  public abstract String getLabel();

  @Nullable
  @CheckForNull
  public abstract String getDescription();

  public abstract boolean isHidden();

  public abstract boolean isReadonly();

  public static Resource create(
      ResourceType newType,
      String newId,
      String newLabel,
      String newDescription,
      boolean newHidden,
      boolean newReadonly) {
    return builder()
        .setType(newType)
        .setId(newId)
        .setLabel(newLabel)
        .setDescription(newDescription)
        .setHidden(newHidden)
        .setReadonly(newReadonly)
        .build();
  }

  public static Builder builder() {
    return new AutoValue_Resource.Builder().setHidden(false).setReadonly(false);
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setType(ResourceType newType);

    public abstract Builder setId(String newId);

    public abstract Builder setLabel(String newLabel);

    public abstract Builder setDescription(String newDescription);

    public abstract Builder setHidden(boolean newHidden);

    public abstract Builder setReadonly(boolean newReadonly);

    public abstract Resource build();
  }
}
