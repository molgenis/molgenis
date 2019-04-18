package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_TypePermissionsResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class TypePermissionsResponse {
  public abstract String getTypeId();

  @Nullable
  public abstract String getLabel();

  public abstract List<ObjectPermission> getObjects();

  public static TypePermissionsResponse create(
      String typeId, String label, List<ObjectPermission> objects) {
    return new AutoValue_TypePermissionsResponse(typeId, label, objects);
  }
}
