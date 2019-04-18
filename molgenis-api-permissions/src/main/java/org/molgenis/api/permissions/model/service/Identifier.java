package org.molgenis.api.permissions.model.service;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Identifier.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class Identifier {
  public abstract Object getIdentifier();

  public abstract String getLabel();

  public static Identifier create(Object identifier, String label) {
    return new AutoValue_Identifier(identifier, label);
  }
}
