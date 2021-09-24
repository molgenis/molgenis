package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ObjectPermissionResponse.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class ObjectPermissionResponse {
  public abstract String getId();

  public abstract String getLabel();

  @Nullable
  @CheckForNull
  public abstract String getOwnedByRole();

  @Nullable
  @CheckForNull
  public abstract String getOwnedByUser();

  public abstract boolean isYours();

  public abstract Set<PermissionResponse> getPermissions();

  public static ObjectPermissionResponse create(
      String id,
      String label,
      String ownedByRole,
      String ownedByUser,
      boolean yours,
      Set<PermissionResponse> objectPermissionResponses) {
    return new AutoValue_ObjectPermissionResponse(
        id, label, ownedByRole, ownedByUser, yours, objectPermissionResponses);
  }
}
