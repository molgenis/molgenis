package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GetPermissionsResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class GetPermissionsResponse {

  @NotEmpty
  public abstract List<TypePermissionsResponse> getTypes();

  public static GetPermissionsResponse create(List<TypePermissionsResponse> types) {
    return builder().setTypes(types).build();
  }

  private static Builder builder() {
    return new AutoValue_GetPermissionsResponse.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setTypes(List<TypePermissionsResponse> types);

    public abstract GetPermissionsResponse build();
  }
}
