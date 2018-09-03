package org.molgenis.data.postgresql.identifier;

import com.google.auto.value.AutoValue;

@AutoValue
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class AttributeDescription {
  public abstract String getName();

  public static AttributeDescription create(String name) {
    return new AutoValue_AttributeDescription(name);
  }
}
