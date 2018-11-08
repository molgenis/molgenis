package org.molgenis.data.resource;

import com.google.auto.value.AutoValue;
import javax.validation.constraints.NotNull;
import org.molgenis.util.AutoGson;

/** Wrapper class for resources. Stores the ID and the type. */
@AutoValue
@AutoGson(autoValueClass = AutoValue_Resource.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class Resource {

  public enum ResourceType {
    PACKAGE,
    ENTITY_TYPE
  }

  @NotNull
  public abstract ResourceType getType();

  @NotNull
  public abstract String getId();

  public static Resource of(ResourceType type, String id) {
    return new AutoValue_Resource(type, id);
  }
}
