package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GetPermissionsResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class GetPermissionsResponse {
  public abstract List<ObjectPermission> getPermissions();

  public static GetPermissionsResponse create(List<ObjectPermission> permissions) {
    return new AutoValue_GetPermissionsResponse(permissions);
  }
}
