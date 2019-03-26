package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ObjectPermissionsResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class ObjectPermissionsResponse {
  public abstract String getObjectId();

  @Nullable
  public abstract String getLabel();

  public abstract List<PermissionResponse> getPermissions();

  public static ObjectPermissionsResponse create(
      String identifier, String label, List<PermissionResponse> permissionResponses) {
    return new AutoValue_ObjectPermissionsResponse(identifier, label, permissionResponses);
  }
}
