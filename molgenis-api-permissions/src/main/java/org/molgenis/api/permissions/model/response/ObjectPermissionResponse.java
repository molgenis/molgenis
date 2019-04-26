package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import java.util.Set;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ObjectPermissionResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class ObjectPermissionResponse {
  public abstract String getId();

  @Nullable
  public abstract String getLabel();

  public abstract Set<PermissionResponse> getPermissions();

  public static ObjectPermissionResponse create(
      String id, String label, Set<PermissionResponse> objectPermissionResponses) {
    return new AutoValue_ObjectPermissionResponse(id, label, objectPermissionResponses);
  }
}
