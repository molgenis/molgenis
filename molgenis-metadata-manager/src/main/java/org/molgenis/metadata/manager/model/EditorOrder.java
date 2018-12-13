package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorOrder.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EditorOrder {
  public abstract String getAttributeName();

  @CheckForNull
  public abstract String getDirection();

  public static EditorOrder create(String attributeName, @CheckForNull String direction) {
    return new AutoValue_EditorOrder(attributeName, direction);
  }
}
