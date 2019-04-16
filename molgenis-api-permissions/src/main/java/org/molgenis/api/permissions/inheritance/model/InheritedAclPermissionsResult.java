package org.molgenis.api.permissions.inheritance.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;
import org.springframework.security.acls.model.Acl;

@AutoValue
@AutoGson(autoValueClass = AutoValue_InheritedAclPermissionsResult.class)
public abstract class InheritedAclPermissionsResult {
  public static InheritedAclPermissionsResult create(
      Acl acl,
      String ownPermission,
      List<InheritedUserPermissionsResult> parentRolePermissions,
      InheritedAclPermissionsResult parentAclPermissions) {
    return new AutoValue_InheritedAclPermissionsResult(
        acl, ownPermission, parentRolePermissions, parentAclPermissions);
  }

  public abstract Acl getAcl();

  @Nullable
  public abstract String getOwnPermission();

  @Nullable
  public abstract List<InheritedUserPermissionsResult> getParentRolePermissions();

  @Nullable
  public abstract InheritedAclPermissionsResult getParentAclPermissions();
}
