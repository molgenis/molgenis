package org.molgenis.api.identities;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AddExtendsCommand.class)
@SuppressWarnings("squid:S1610")
public abstract class AddExtendsCommand {
  public abstract String getGroupRoleName();

  public abstract String getMemberRoleName();

  public static AddExtendsCommand create(String groupRole, String memberRole) {
    return new AutoValue_AddExtendsCommand(groupRole, memberRole);
  }
}
