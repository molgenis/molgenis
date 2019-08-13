package org.molgenis.api.identities;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_UpdateIncludeCommand.class)
@SuppressWarnings("squid:S1610")
public abstract class UpdateIncludeCommand {

  public abstract String getRole();

  public static UpdateIncludeCommand create(String role) {
    return new AutoValue_UpdateIncludeCommand(role);
  }
}
