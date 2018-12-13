package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorEntityTypeIdentifier.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EditorEntityTypeIdentifier {
  public abstract String getId();

  @Nullable
  @CheckForNull
  public abstract String getLabel();

  public static EditorEntityTypeIdentifier create(String id, @Nullable @CheckForNull String label) {
    return new AutoValue_EditorEntityTypeIdentifier(id, label);
  }
}
