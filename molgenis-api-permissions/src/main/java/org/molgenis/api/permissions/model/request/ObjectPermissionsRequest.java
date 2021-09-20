package org.molgenis.api.permissions.model.request;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ObjectPermissionsRequest.class)
@SuppressWarnings({
  "java:S1610",
  "java:S3038"
}) // Abstract classes without fields should be converted to interfaces
public abstract class ObjectPermissionsRequest implements NewOwnerRequest {
  public abstract String getObjectId();

  @Nullable
  @CheckForNull
  public abstract String getOwnedByUser();

  @Nullable
  @CheckForNull
  public abstract String getOwnedByRole();

  @Nullable
  @CheckForNull
  public abstract List<PermissionRequest> getPermissions();

  public static ObjectPermissionsRequest create(
      String identifier,
      String ownedByUser,
      String ownedByRole,
      List<PermissionRequest> permissionRequests) {
    return new AutoValue_ObjectPermissionsRequest(
        identifier, ownedByUser, ownedByRole, permissionRequests);
  }
}
