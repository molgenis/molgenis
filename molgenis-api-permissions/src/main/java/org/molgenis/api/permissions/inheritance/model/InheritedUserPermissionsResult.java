package org.molgenis.api.permissions.inheritance.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;
import org.springframework.security.acls.model.Sid;

@AutoValue
@AutoGson(autoValueClass = AutoValue_InheritedUserPermissionsResult.class)
public abstract class InheritedUserPermissionsResult {
  public static InheritedUserPermissionsResult create(
      Sid sid,
      String ownPermission,
      List<InheritedUserPermissionsResult> inheritedUserPermissionsResult) {
    return new AutoValue_InheritedUserPermissionsResult(
        sid, ownPermission, inheritedUserPermissionsResult);
  }

  @Nullable
  public abstract Sid getSid();

  @Nullable
  public abstract String getOwnPermission();

  @Nullable
  public abstract List<InheritedUserPermissionsResult> getInheritedUserPermissionsResult();
}
