package org.molgenis.api.identities;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_UpdateExtendsCommand.class)
@SuppressWarnings("squid:S1610")
public abstract class UpdateExtendsCommand {

  public abstract String getRole();

  public static UpdateExtendsCommand create(String role) {
    return new AutoValue_UpdateExtendsCommand(role);
  }
}
