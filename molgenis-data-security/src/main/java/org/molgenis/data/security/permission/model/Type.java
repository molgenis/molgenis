package org.molgenis.data.security.permission.model;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Type.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class Type {
  public abstract String getId();

  public abstract String getEntityType();

  public abstract String getLabel();

  public static Type create(String typeId, String entityType, String label) {
    return new AutoValue_Type(typeId, entityType, label);
  }
}
