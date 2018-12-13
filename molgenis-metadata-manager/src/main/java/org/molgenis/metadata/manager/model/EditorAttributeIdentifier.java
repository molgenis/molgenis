package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorAttributeIdentifier.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EditorAttributeIdentifier {
  public abstract String getId();

  @CheckForNull
  public abstract String getLabel();

  @CheckForNull
  public abstract EditorEntityTypeIdentifier getEntity();

  public static EditorAttributeIdentifier create(String id, @CheckForNull String label) {
    return new AutoValue_EditorAttributeIdentifier(id, label, null);
  }

  public static EditorAttributeIdentifier create(
      String id, @CheckForNull String label, @CheckForNull EditorEntityTypeIdentifier entity) {
    return new AutoValue_EditorAttributeIdentifier(id, label, entity);
  }
}
