package org.molgenis.api.permissions.model.request;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ClassPermissionsRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class ClassPermissionsRequest {
  public abstract String getClassId();

  public abstract List<IdentityPermissionsRequest> getRowPermissions();

  public static ClassPermissionsRequest create(
      String classId, List<IdentityPermissionsRequest> permissions) {
    return new AutoValue_ClassPermissionsRequest(classId, permissions);
  }
}
