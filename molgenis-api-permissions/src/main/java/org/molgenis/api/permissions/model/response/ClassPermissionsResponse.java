package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ClassPermissionsResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class ClassPermissionsResponse {
  public abstract String getClassId();

  @Nullable
  public abstract String getLabel();

  public abstract List<IdentityPermissionsResponse> getRowPermissions();

  public static ClassPermissionsResponse create(
      String classId, String label, List<IdentityPermissionsResponse> permissions) {
    return new AutoValue_ClassPermissionsResponse(classId, label, permissions);
  }
}
