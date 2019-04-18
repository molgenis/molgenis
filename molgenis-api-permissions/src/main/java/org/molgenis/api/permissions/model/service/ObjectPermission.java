package org.molgenis.api.permissions.model.service;

import com.google.auto.value.AutoValue;
import java.util.Set;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ObjectPermission.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class ObjectPermission {

  public abstract ObjectIdentity getObjectIdentity();

  @Nullable
  public abstract Sid getSid();

  @Nullable
  public abstract String getPermission();

  @Nullable
  public abstract Set<LabelledPermission> getInheritedPermissions();

  public static ObjectPermission create(
      ObjectIdentity objectIdentity,
      Sid sid,
      String permission,
      Set<LabelledPermission> inheritedPermissions) {
    return new AutoValue_ObjectPermission(objectIdentity, sid, permission, inheritedPermissions);
  }
}
