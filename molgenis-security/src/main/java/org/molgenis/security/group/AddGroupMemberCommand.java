package org.molgenis.security.group;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AddGroupMemberCommand.class)
@SuppressWarnings("squid:S1610")
public abstract class AddGroupMemberCommand {
  public abstract String getUsername();

  public abstract String getRoleName();

  public static AddGroupMemberCommand addGroupMember(String user, String role) {
    return new AutoValue_AddGroupMemberCommand(user, role);
  }
}
