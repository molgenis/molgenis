package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ObjectResponse.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class ObjectResponse {
  public abstract String getId();

  public abstract String getLabel();

  @Nullable
  @CheckForNull
  public abstract String getOwnedByRole();

  @Nullable
  @CheckForNull
  public abstract String getOwnedByUser();

  public abstract boolean isYours();

  public static ObjectResponse create(
      String typeId, String label, String ownedByRole, String ownedByUser, boolean yours) {
    return new AutoValue_ObjectResponse(typeId, label, ownedByRole, ownedByUser, yours);
  }
}
