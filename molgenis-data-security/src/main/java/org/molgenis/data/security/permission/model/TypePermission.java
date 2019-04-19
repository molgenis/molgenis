package org.molgenis.data.security.permission.model;

import com.google.auto.value.AutoValue;
import java.util.Set;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_TypePermission.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class TypePermission {

  @Nullable
  public abstract String getTypeLabel();

  @Nullable
  public abstract String getTypeId();

  @Nullable
  public abstract Set<LabelledObjectPermission> getObjectPermissions();

  public static TypePermission create(
      String typeId, String typeLabel, Set<LabelledObjectPermission> objectPermissions) {
    return new AutoValue_TypePermission(typeId, typeLabel, objectPermissions);
  }
}
