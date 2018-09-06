package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorEntityTypeParent.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EditorEntityTypeParent {
  public abstract String getId();

  @Nullable
  public abstract String getLabel();

  public abstract List<EditorAttributeIdentifier> getAttributes();

  @Nullable
  public abstract EditorEntityTypeParent getParent();

  public static EditorEntityTypeParent create(
      String id,
      @Nullable String label,
      List<EditorAttributeIdentifier> attributes,
      @Nullable EditorEntityTypeParent parent) {
    return new AutoValue_EditorEntityTypeParent(id, label, attributes, parent);
  }
}
