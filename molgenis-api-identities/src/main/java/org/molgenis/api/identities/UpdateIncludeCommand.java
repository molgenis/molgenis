package org.molgenis.api.identities;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_UpdateIncludeCommand.class)
@SuppressWarnings("java:S1610")
public abstract class UpdateIncludeCommand {

  public abstract String getRole();

  public static UpdateIncludeCommand create(String role) {
    return new AutoValue_UpdateIncludeCommand(role);
  }
}
