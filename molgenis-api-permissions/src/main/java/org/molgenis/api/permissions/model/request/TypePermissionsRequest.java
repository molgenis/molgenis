package org.molgenis.api.permissions.model.request;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_TypePermissionsRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class TypePermissionsRequest {
  public abstract String getTypeId();

  public abstract List<ObjectPermissionsRequest> getObjects();

  public static TypePermissionsRequest create(
      String typeId, List<ObjectPermissionsRequest> objects) {
    return new AutoValue_TypePermissionsRequest(typeId, objects);
  }
}
