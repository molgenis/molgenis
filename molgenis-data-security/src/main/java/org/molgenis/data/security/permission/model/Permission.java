package org.molgenis.data.security.permission.model;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.util.AutoGson;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Permission.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class Permission {
  @Nullable
  public abstract ObjectIdentity getObjectIdentity();

  @Nullable
  public abstract Sid getSid();

  @Nullable
  public abstract PermissionSet getPermission();

  public static Permission create(
      ObjectIdentity objectIdentity, Sid sid, PermissionSet permission) {
    return new AutoValue_Permission(objectIdentity, sid, permission);
  }
}
