package org.molgenis.api.permissions.model.request;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_IdentityPermissionsRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class IdentityPermissionsRequest {
  public abstract String getIdentifier();

  public abstract List<PermissionRequest> getPermissions();

  public static IdentityPermissionsRequest create(
      String identifier, List<PermissionRequest> permissionRequests) {
    return new AutoValue_IdentityPermissionsRequest(identifier, permissionRequests);
  }
}
