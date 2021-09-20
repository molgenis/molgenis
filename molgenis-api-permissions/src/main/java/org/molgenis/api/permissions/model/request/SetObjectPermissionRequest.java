package org.molgenis.api.permissions.model.request;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_SetObjectPermissionRequest.class)
@SuppressWarnings({
  "java:S1610",
  "java:S3038"
}) // Abstract classes without fields should be converted to interfaces
public abstract class SetObjectPermissionRequest implements NewOwnerRequest {
  @Nullable
  @CheckForNull
  public abstract String getOwnedByUser();

  @Nullable
  @CheckForNull
  public abstract String getOwnedByRole();

  @Nullable
  @CheckForNull
  public abstract List<PermissionRequest> getPermissions();

  public static SetObjectPermissionRequest create(
      String ownedByUser, String ownedByRole, List<PermissionRequest> permissionRequests) {
    return new AutoValue_SetObjectPermissionRequest(ownedByUser, ownedByRole, permissionRequests);
  }
}
