package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class Path {
  public abstract List<PathComponent> getComponents();

  public static Path create(List<PathComponent> newComponents) {
    return new AutoValue_Path(newComponents);
  }
}
