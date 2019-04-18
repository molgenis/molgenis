package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import java.util.Set;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ObjectPermission.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class ObjectPermission {
  @Nullable
  public abstract String getRole();

  @Nullable
  public abstract String getUser();

  @Nullable
  public abstract String getPermission();

  @Nullable
  public abstract Set<InheritedPermission> getInheritedPermissions();

  public static ObjectPermission create(
      String role, String user, String permission, Set<InheritedPermission> inheritedPermissions) {
    return builder()
        .setRole(role)
        .setUser(user)
        .setPermission(permission)
        .setInheritedPermissions(inheritedPermissions)
        .build();
  }

  private static Builder builder() {
    return new AutoValue_ObjectPermission.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setRole(String role);

    public abstract Builder setUser(String user);

    public abstract Builder setPermission(String permissions);

    public abstract Builder setInheritedPermissions(Set<InheritedPermission> permissions);

    public abstract ObjectPermission build();
  }
}
