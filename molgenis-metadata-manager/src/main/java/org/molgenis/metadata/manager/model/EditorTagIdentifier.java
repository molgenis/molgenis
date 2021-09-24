package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorTagIdentifier.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EditorTagIdentifier {
  public abstract String getId();

  public abstract String getLabel();

  public static EditorTagIdentifier create(String id, String label) {
    return new AutoValue_EditorTagIdentifier(id, label);
  }
}
