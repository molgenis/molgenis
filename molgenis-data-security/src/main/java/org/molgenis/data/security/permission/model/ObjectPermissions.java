package org.molgenis.data.security.permission.model;

import com.google.auto.value.AutoValue;
import java.util.Set;
import org.molgenis.util.AutoGson;
import org.springframework.security.acls.model.ObjectIdentity;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ObjectPermissions.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class ObjectPermissions {
  public abstract ObjectIdentity getObjectIdentity();

  public abstract Set<Permission> getPermissions();

  public static ObjectPermissions create(
      ObjectIdentity objectIdentity, Set<Permission> permissions) {
    return new AutoValue_ObjectPermissions(objectIdentity, permissions);
  }
}
