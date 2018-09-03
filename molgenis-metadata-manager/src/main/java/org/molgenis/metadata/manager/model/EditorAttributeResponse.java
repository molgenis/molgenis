package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorAttributeResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EditorAttributeResponse {
  abstract EditorAttribute getAttribute();

  abstract List<String> getLanguageCodes();

  public static EditorAttributeResponse create(
      EditorAttribute attribute, List<String> languageCodes) {
    return new AutoValue_EditorAttributeResponse(attribute, languageCodes);
  }
}
