package org.molgenis.data.security.permission.inheritance.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_InheritedPermissionsResult.class)
public abstract class InheritedPermissionsResult {
  public static InheritedPermissionsResult create(
      List<InheritedUserPermissionsResult> requestedAclParentRolesPermissions,
      InheritedAclPermissionsResult parentAclPermission) {
    return new AutoValue_InheritedPermissionsResult(
        requestedAclParentRolesPermissions, parentAclPermission);
  }

  @Nullable
  public abstract List<InheritedUserPermissionsResult> getRequestedAclParentRolesPermissions();

  @Nullable
  public abstract InheritedAclPermissionsResult getParentAclPermission();
}
