package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ObjectResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class ObjectResponse {
  public abstract String getId();

  public abstract String getLabel();

  public static ObjectResponse create(String typeId, String label) {
    return new AutoValue_ObjectResponse(typeId, label);
  }
}
