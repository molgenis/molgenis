package org.molgenis.data.elasticsearch.generator.model;

import com.google.auto.value.AutoValue;

@AutoValue
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class Index {
  public abstract String getName();

  public static Index create(String name) {
    return new AutoValue_Index(name);
  }
}
