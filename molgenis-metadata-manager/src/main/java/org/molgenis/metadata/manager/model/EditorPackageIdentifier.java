package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorPackageIdentifier.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EditorPackageIdentifier {
  public abstract String getId();

  @Nullable
  @CheckForNull
  public abstract String getLabel();

  public static EditorPackageIdentifier create(String id, @Nullable @CheckForNull String label) {
    return new AutoValue_EditorPackageIdentifier(id, label);
  }
}
