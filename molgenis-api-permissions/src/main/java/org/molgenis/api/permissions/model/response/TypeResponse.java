package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_TypeResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class TypeResponse {
  public abstract String getId();

  public abstract String getEntityType();

  public abstract String getLabel();

  public static TypeResponse create(String typeId, String entityType, String label) {
    return new AutoValue_TypeResponse(typeId, entityType, label);
  }
}
