package org.molgenis.api.permissions.model.request;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_SetTypePermissionsRequest.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class SetTypePermissionsRequest {

  @NotEmpty
  public abstract List<ObjectPermissionsRequest> getObjects();

  public static SetTypePermissionsRequest create(List<ObjectPermissionsRequest> objects) {
    return new AutoValue_SetTypePermissionsRequest(objects);
  }
}
