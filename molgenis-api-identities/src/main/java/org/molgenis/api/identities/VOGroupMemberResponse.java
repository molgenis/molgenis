package org.molgenis.api.identities;

import com.google.auto.value.AutoValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.molgenis.data.security.auth.VOGroupRoleMembership;

@AutoValue
@ApiModel(
    description =
        "Represents the fact that a VO group's members are granted a role in a local group.")
@SuppressWarnings("java:S1610")
public abstract class VOGroupMemberResponse {
  @ApiModelProperty("The VO group")
  public abstract VOGroupResponse getVOGroup();

  @ApiModelProperty("The role that is granted")
  public abstract RoleResponse getRole();

  static VOGroupMemberResponse fromEntity(VOGroupRoleMembership membership) {
    return new AutoValue_VOGroupMemberResponse(
        VOGroupResponse.fromEntity(membership.getVOGroup()),
        RoleResponse.fromEntity(membership.getRole()));
  }
}
