package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import java.util.Set;
import javax.annotation.Nullable;
import org.molgenis.api.permissions.model.service.LabelledPermission;
import org.molgenis.util.AutoGson;
import org.springframework.security.acls.model.ObjectIdentity;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ObjectPermission.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class ObjectPermission {

  public abstract ObjectIdentity getObjectIdentity();

  @Nullable
  public abstract String getRole();

  @Nullable
  public abstract String getUser();

  @Nullable
  public abstract String getPermission();

  @Nullable
  public abstract Set<LabelledPermission> getLabelledPermissions();

  public static ObjectPermission create(
      ObjectIdentity objectIdentity,
      String role,
      String user,
      String permission,
      Set<LabelledPermission> labelledPermissions) {
    return new AutoValue_ObjectPermission(
        objectIdentity, role, user, permission, labelledPermissions);
  }
}
