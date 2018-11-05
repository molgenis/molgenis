package org.molgenis.navigator;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PathComponent {
  public abstract String getId();

  public abstract String getLabel();

  public static PathComponent create(String newId, String newLabel) {
    return new AutoValue_PathComponent(newId, newLabel);
  }
}
