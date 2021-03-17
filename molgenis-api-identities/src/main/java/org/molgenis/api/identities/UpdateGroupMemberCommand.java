package org.molgenis.api.identities;

import com.google.auto.value.AutoValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_UpdateGroupMemberCommand.class)
@ApiModel(description = "Updates the role that a user or group has on a local group")
@SuppressWarnings("java:S1610")
public abstract class UpdateGroupMemberCommand {
  @ApiModelProperty("The name of the new role to grant")
  public abstract String getRoleName();

  public static UpdateGroupMemberCommand updateGroupMember(String roleName) {
    return new AutoValue_UpdateGroupMemberCommand(roleName);
  }
}
