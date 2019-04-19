package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import java.util.Set;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AllPermissionsResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class AllPermissionsResponse {
  public abstract Set<LabelledPermissionResponse> getPermissions();

  public static AllPermissionsResponse create(Set<LabelledPermissionResponse> permissions) {
    return new AutoValue_AllPermissionsResponse(permissions);
  }
}
