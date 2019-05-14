package org.molgenis.api.permissions.model.response;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.auto.value.AutoValue;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_LabelledPermissionResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class LabelledPermissionResponse {
  @Nullable
  @CheckForNull
  public abstract String getUser();

  @Nullable
  @CheckForNull
  public abstract String getRole();

  @Nullable
  @CheckForNull
  public abstract ObjectResponse getObject();

  @Nullable
  @CheckForNull
  public abstract TypeResponse getType();

  @Nullable
  @CheckForNull
  public abstract String getPermission();

  /**
   * returns null if inheritance was not requested. If requested but not present an empty set is
   * returned.
   */
  @Nullable
  @CheckForNull
  public abstract Set<LabelledPermissionResponse> getInheritedPermissions();

  public static LabelledPermissionResponse create(
      String user,
      String role,
      ObjectResponse object,
      TypeResponse type,
      String permission,
      Set<LabelledPermissionResponse> inheritedPermissions) {
    if (isNullOrEmpty(user) && isNullOrEmpty(role)) {
      throw new IllegalStateException("No user and role provided.");
    } else if (!isNullOrEmpty(user) && !isNullOrEmpty(role)) {
      throw new IllegalStateException("Both user and role provided.");
    }
    return new AutoValue_LabelledPermissionResponse(
        user, role, object, type, permission, inheritedPermissions);
  }
}
