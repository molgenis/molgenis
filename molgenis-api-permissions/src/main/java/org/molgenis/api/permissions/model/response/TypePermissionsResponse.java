package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import java.util.Set;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_TypePermissionsResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class TypePermissionsResponse {
  public abstract String getId();

  public abstract String getLabel();

  public abstract Set<ObjectPermissionResponse> getObjects();

  public static TypePermissionsResponse create(
      String id, String label, Set<ObjectPermissionResponse> objects) {
    return new AutoValue_TypePermissionsResponse(id, label, objects);
  }
}
