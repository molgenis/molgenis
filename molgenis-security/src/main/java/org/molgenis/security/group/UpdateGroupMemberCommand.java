package org.molgenis.security.group;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_UpdateGroupMemberCommand.class)
@SuppressWarnings("squid:S1610")
public abstract class UpdateGroupMemberCommand {
  public abstract String getRoleName();

  public static UpdateGroupMemberCommand updateGroupMember(String roleName) {
    return new AutoValue_UpdateGroupMemberCommand(roleName);
  }
}
