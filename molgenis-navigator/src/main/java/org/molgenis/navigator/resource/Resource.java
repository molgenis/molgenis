package org.molgenis.navigator.resource;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Resource.class)
public abstract class Resource {
  public enum Type {
    PACKAGE,
    ENTITY_TYPE
  }

  public abstract Type getType();

  public abstract String getId();

  @Nullable
  public abstract String getLabel();

  @Nullable
  public abstract String getDescription();

  public static Resource create(
      Type type, String id, @Nullable String label, @Nullable String description) {
    return new AutoValue_Resource(type, id, label, description);
  }
}
