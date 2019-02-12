package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GetIdentityPermissionResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class GetIdentityPermissionResponse {

  @NotEmpty
  public abstract List<PermissionResponse> getPermissions();

  public static GetIdentityPermissionResponse create(List<PermissionResponse> permissionResponses) {
    return builder().setPermissions(permissionResponses).build();
  }

  public static Builder builder() {
    return new AutoValue_GetIdentityPermissionResponse.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setPermissions(List<PermissionResponse> permissionResponses);

    public abstract GetIdentityPermissionResponse build();
  }
}
