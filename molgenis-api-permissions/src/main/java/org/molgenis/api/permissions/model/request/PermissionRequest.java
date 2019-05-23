package org.molgenis.api.permissions.model.request;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.permissions.exceptions.MissingUserOrRoleException;
import org.molgenis.api.permissions.exceptions.UserAndRoleException;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_PermissionRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class PermissionRequest {
  @Nullable
  @CheckForNull
  public abstract String getRole();

  @Nullable
  @CheckForNull
  public abstract String getUser();

  public abstract String getPermission();

  public static PermissionRequest create(String role, String user, String permission) {
    if (isNullOrEmpty(user) && isNullOrEmpty(role)) {
      throw new MissingUserOrRoleException();
    } else if (!isNullOrEmpty(user) && !isNullOrEmpty(role)) {
      throw new UserAndRoleException();
    }
    return builder().setRole(role).setUser(user).setPermission(permission).build();
  }

  private static Builder builder() {
    return new AutoValue_PermissionRequest.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setRole(String role);

    public abstract Builder setUser(String user);

    public abstract Builder setPermission(String permissions);

    public abstract PermissionRequest build();
  }
}
