package org.molgenis.data.security.permission.model;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_LabelledObject.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class LabelledObject {
  public abstract String getId();

  public abstract String getLabel();

  public static LabelledObject create(String id, String label) {
    return new AutoValue_LabelledObject(id, label);
  }
}
