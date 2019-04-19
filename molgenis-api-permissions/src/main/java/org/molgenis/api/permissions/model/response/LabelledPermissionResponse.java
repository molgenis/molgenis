package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import java.util.Set;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_LabelledPermissionResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class LabelledPermissionResponse {
  @Nullable
  public abstract String getUser();

  @Nullable
  public abstract String getRole();

  @Nullable
  public abstract String getObjectId();

  @Nullable
  public abstract String getLabel();

  @Nullable
  public abstract String getTypeLabel();

  @Nullable
  public abstract String getTypeId();

  @Nullable
  public abstract String getPermission();

  @Nullable
  public abstract Set<LabelledPermissionResponse> getInheritedPermissions();

  public static LabelledPermissionResponse create(
      String user,
      String role,
      String typeId,
      String typeLabel,
      String identifier,
      String label,
      String permission,
      Set<LabelledPermissionResponse> inheritedPermissions) {
    return new AutoValue_LabelledPermissionResponse(
        user, role, typeId, typeLabel, identifier, label, permission, inheritedPermissions);
  }
}
