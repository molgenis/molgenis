package org.molgenis.api.permissions.model.response;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.auto.value.AutoValue;
import com.google.common.base.Strings;
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
    // Permissions can be inhertited from a super ACL, in which case the user/role can be null
    // Permissions can be inhertited from a role for the requested user or role, in which case the
    // Object an Type can be null
    // Permissions can be a layer between a super permission and a "lower" in herited permission, in
    // which case only the inherited permissions are available
    if (!hasAuthority(user, role)
        && !hasInheritedPermissions(inheritedPermissions)
        && !hasPermission(object, permission)) {
      throw new IllegalStateException(
          "No user, role, permission or inherited permissions provided.");
    } else if (!isNullOrEmpty(user) && !isNullOrEmpty(role)) {
      throw new IllegalStateException("Both user and role provided.");
    }
    return new AutoValue_LabelledPermissionResponse(
        user, role, object, type, permission, inheritedPermissions);
  }

  private static boolean hasPermission(ObjectResponse object, String permission) {
    return !Strings.isNullOrEmpty(object.getId()) && !Strings.isNullOrEmpty(permission);
  }

  private static boolean hasInheritedPermissions(
      Set<LabelledPermissionResponse> inheritedPermissions) {
    return inheritedPermissions != null && !inheritedPermissions.isEmpty();
  }

  private static boolean hasAuthority(String user, String role) {
    return !isNullOrEmpty(user) || !isNullOrEmpty(role);
  }
}
