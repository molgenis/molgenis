package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Folder.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class Folder {
  public abstract String getId();

  public abstract String getLabel();

  @Nullable
  public abstract Folder getParent();

  public static Folder create(String newId, String newLabel, Folder newParent) {
    return builder().setId(newId).setLabel(newLabel).setParent(newParent).build();
  }

  public static Builder builder() {
    return new AutoValue_Folder.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String newId);

    public abstract Builder setLabel(String newLabel);

    public abstract Builder setParent(Folder newParent);

    public abstract Folder build();
  }
}
