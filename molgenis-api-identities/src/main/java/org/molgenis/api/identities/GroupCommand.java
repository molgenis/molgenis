package org.molgenis.api.identities;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GroupCommand.class)
@SuppressWarnings("java:S1610")
public abstract class GroupCommand {
  public abstract String getName();

  public abstract String getLabel();

  static GroupCommand createGroup(String name, String label) {
    return new AutoValue_GroupCommand(name, label);
  }
}
