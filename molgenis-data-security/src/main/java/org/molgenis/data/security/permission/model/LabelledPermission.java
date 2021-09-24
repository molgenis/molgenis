package org.molgenis.data.security.permission.model;

import com.google.auto.value.AutoValue;
import java.util.Set;
import javax.annotation.Nullable;
import org.molgenis.gson.AutoGson;
import org.molgenis.security.core.PermissionSet;
import org.springframework.security.acls.model.Sid;

@AutoValue
@AutoGson(autoValueClass = AutoValue_LabelledPermission.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class LabelledPermission {
  @Nullable
  public abstract Sid getSid();

  @Nullable
  public abstract LabelledObjectIdentity getLabelledObjectIdentity();

  @Nullable
  public abstract PermissionSet getPermission();

  @Nullable
  public abstract Set<LabelledPermission> getInheritedPermissions();

  public static LabelledPermission create(
      Sid sid,
      LabelledObjectIdentity labelledObjectIdentity,
      PermissionSet permission,
      Set<LabelledPermission> inhertiedPermissions) {
    return new AutoValue_LabelledPermission(
        sid, labelledObjectIdentity, permission, inhertiedPermissions);
  }
}
