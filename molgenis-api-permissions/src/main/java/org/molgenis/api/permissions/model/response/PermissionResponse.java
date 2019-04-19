package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import java.util.Set;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_PermissionResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class PermissionResponse {

  @Nullable
  public abstract String getUser();

  @Nullable
  public abstract String getRole();

  @Nullable
  public abstract String getPermission();

  @Nullable
  public abstract Set<LabelledPermissionResponse> getInheritedPermissions();;

  public static PermissionResponse create(
      String user,
      String role,
      String permission,
      Set<LabelledPermissionResponse> inheritedPermissions) {
    return new AutoValue_PermissionResponse(user, role, permission, inheritedPermissions);
  }
}
