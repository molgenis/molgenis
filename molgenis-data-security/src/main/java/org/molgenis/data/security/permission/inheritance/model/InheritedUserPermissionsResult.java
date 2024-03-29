package org.molgenis.data.security.permission.inheritance.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;
import org.molgenis.gson.AutoGson;
import org.molgenis.security.core.PermissionSet;
import org.springframework.security.acls.model.Sid;

@AutoValue
@AutoGson(autoValueClass = AutoValue_InheritedUserPermissionsResult.class)
public abstract class InheritedUserPermissionsResult {
  public static InheritedUserPermissionsResult create(
      Sid sid,
      PermissionSet ownPermission,
      List<InheritedUserPermissionsResult> inheritedUserPermissionsResult) {
    return new AutoValue_InheritedUserPermissionsResult(
        sid, ownPermission, inheritedUserPermissionsResult);
  }

  @Nullable
  public abstract Sid getSid();

  @Nullable
  public abstract PermissionSet getOwnPermission();

  @Nullable
  public abstract List<InheritedUserPermissionsResult> getInheritedUserPermissionsResult();
}
