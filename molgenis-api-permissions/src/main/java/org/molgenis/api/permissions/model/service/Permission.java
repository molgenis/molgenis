package org.molgenis.api.permissions.model.service;

import com.google.auto.value.AutoValue;
import java.util.Set;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.util.AutoGson;
import org.springframework.security.acls.model.Sid;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Permission.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class Permission {
  public abstract Sid getSid();

  public abstract PermissionSet getPermission();

  public abstract LabelledObjectIdentity getLabelledObjectIdentity();

  public abstract Set<LabelledPermission> getLabelledPermissions();

  public static Permission create(
      Sid sid,
      PermissionSet permissionSet,
      LabelledObjectIdentity objectIdentity,
      Set<LabelledPermission> labelledPermissions) {
    return new AutoValue_Permission(sid, permissionSet, objectIdentity, labelledPermissions);
  }
}
