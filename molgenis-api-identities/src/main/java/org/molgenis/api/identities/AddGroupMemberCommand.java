package org.molgenis.api.identities;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AddGroupMemberCommand.class)
@SuppressWarnings("java:S1610")
public abstract class AddGroupMemberCommand {
  public abstract String getUsername();

  public abstract String getRoleName();

  public static AddGroupMemberCommand addGroupMember(String user, String role) {
    return new AutoValue_AddGroupMemberCommand(user, role);
  }
}
