package org.molgenis.data.security.permission.model;

import com.google.auto.value.AutoValue;
import java.util.Set;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_LabelledObjectPermission.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class LabelledObjectPermission {
  public abstract LabelledObjectIdentity getLabelledObjectIdentity();

  public abstract Set<Permission> getPermissions();

  public static LabelledObjectPermission create(
      LabelledObjectIdentity objectIdentity, Set<Permission> permissions) {
    return new AutoValue_LabelledObjectPermission(objectIdentity, permissions);
  }
}
