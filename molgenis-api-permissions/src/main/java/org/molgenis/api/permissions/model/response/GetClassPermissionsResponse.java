package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GetClassPermissionsResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class GetClassPermissionsResponse {

  @NotEmpty
  public abstract List<IdentityPermissionsResponse> getIdentityPermissions();

  public static GetClassPermissionsResponse create(
      List<IdentityPermissionsResponse> identityPermissionResponses) {
    return builder().setIdentityPermissions(identityPermissionResponses).build();
  }

  public static Builder builder() {
    return new AutoValue_GetClassPermissionsResponse.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setIdentityPermissions(List<IdentityPermissionsResponse> permissions);

    public abstract GetClassPermissionsResponse build();
  }
}
