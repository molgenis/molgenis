package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

@AutoValue
public abstract class Folder {
  public abstract String getId();

  public abstract String getLabel();

  @Nullable
  public abstract Folder getParent();

  public static Folder create(String newId, String newLabel, Folder newParent) {
    return new AutoValue_Folder(newId, newLabel, newParent);
  }
}
