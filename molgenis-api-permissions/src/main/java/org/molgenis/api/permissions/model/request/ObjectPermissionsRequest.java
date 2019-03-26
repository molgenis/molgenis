package org.molgenis.api.permissions.model.request;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ObjectPermissionsRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class ObjectPermissionsRequest {
  public abstract String getObjectId();

  public abstract List<PermissionRequest> getPermissions();

  public static ObjectPermissionsRequest create(
      String identifier, List<PermissionRequest> permissionRequests) {
    return new AutoValue_ObjectPermissionsRequest(identifier, permissionRequests);
  }
}
