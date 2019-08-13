package org.molgenis.api.permissions.model.request;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_SetObjectPermissionRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class SetObjectPermissionRequest {

  @NotEmpty
  public abstract List<PermissionRequest> getPermissions();

  public static SetObjectPermissionRequest create(List<PermissionRequest> permissionRequests) {
    return new AutoValue_SetObjectPermissionRequest(permissionRequests);
  }
}
