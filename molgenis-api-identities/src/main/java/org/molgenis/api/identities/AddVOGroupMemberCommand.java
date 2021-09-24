package org.molgenis.api.identities;

import com.google.auto.value.AutoValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AddVOGroupMemberCommand.class)
@ApiModel(description = "Command to grant VO group members a role on a local group")
@SuppressWarnings("java:S1610")
public abstract class AddVOGroupMemberCommand {
  @ApiModelProperty("ID of the VO group to add")
  public abstract String getVOGroupID();

  @ApiModelProperty("The name of the role to grant to the VO group members")
  public abstract String getRoleName();

  public static AddVOGroupMemberCommand addVOGroupMember(String voGroupID, String role) {
    return new AutoValue_AddVOGroupMemberCommand(voGroupID, role);
  }
}
