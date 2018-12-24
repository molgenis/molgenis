package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorAttributeIdentifier.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EditorAttributeIdentifier {
  public abstract String getId();

  @Nullable
  @CheckForNull
  public abstract String getLabel();

  @Nullable
  @CheckForNull
  public abstract EditorEntityTypeIdentifier getEntity();

  public static EditorAttributeIdentifier create(String id, @Nullable @CheckForNull String label) {
    return new AutoValue_EditorAttributeIdentifier(id, label, null);
  }

  public static EditorAttributeIdentifier create(
      String id,
      @Nullable @CheckForNull String label,
      @Nullable @CheckForNull EditorEntityTypeIdentifier entity) {
    return new AutoValue_EditorAttributeIdentifier(id, label, entity);
  }
}
