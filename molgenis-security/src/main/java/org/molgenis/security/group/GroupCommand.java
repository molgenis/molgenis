package org.molgenis.security.group;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GroupCommand.class)
@SuppressWarnings("squid:S1610")
public abstract class GroupCommand {
  public abstract String getName();

  public abstract String getLabel();

  static GroupCommand createGroup(String name, String label) {
    return new AutoValue_GroupCommand(name, label);
  }
}
