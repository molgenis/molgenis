package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
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

  public static Resource create(Type type, String id) {
    return new AutoValue_Resource(type, id);
  }
}
