package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GetTypePermissionsResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class GetTypePermissionsResponse {

  @NotEmpty
  public abstract List<ObjectPermission> getObjects();

  public static GetTypePermissionsResponse create(List<ObjectPermission> objects) {
    return builder().setObjects(objects).build();
  }

  private static Builder builder() {
    return new AutoValue_GetTypePermissionsResponse.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setObjects(List<ObjectPermission> objects);

    public abstract GetTypePermissionsResponse build();
  }
}
