package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorEntityTypeResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EditorEntityTypeResponse {
  abstract EditorEntityType getEntityType();

  abstract List<String> getLanguageCodes();

  public static EditorEntityTypeResponse create(
      EditorEntityType entityType, List<String> languageCodes) {
    return new AutoValue_EditorEntityTypeResponse(entityType, languageCodes);
  }
}
