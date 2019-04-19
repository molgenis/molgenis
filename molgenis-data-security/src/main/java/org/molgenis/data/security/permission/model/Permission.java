package org.molgenis.data.security.permission.model;

import com.google.auto.value.AutoValue;
import java.util.Set;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;
import org.springframework.security.acls.model.Sid;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Permission.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class Permission {

  @Nullable
  public abstract Sid getSid();

  @Nullable
  public abstract String getPermission();

  @Nullable
  public abstract Set<LabelledPermission> getInheritedPermissions();

  public static Permission create(
      Sid sid, String permission, Set<LabelledPermission> inheritedPermissions) {
    return new AutoValue_Permission(sid, permission, inheritedPermissions);
  }
}
