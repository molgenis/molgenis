package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorEntityTypeParent.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EditorEntityTypeParent {
  public abstract String getId();

  @Nullable
  @CheckForNull
  public abstract String getLabel();

  public abstract List<EditorAttributeIdentifier> getAttributes();

  @Nullable
  @CheckForNull
  public abstract EditorEntityTypeParent getParent();

  public static EditorEntityTypeParent create(
      String id,
      @Nullable @CheckForNull String label,
      List<EditorAttributeIdentifier> attributes,
      @Nullable @CheckForNull EditorEntityTypeParent parent) {
    return new AutoValue_EditorEntityTypeParent(id, label, attributes, parent);
  }
}
