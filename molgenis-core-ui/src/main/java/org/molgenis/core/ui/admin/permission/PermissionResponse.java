package org.molgenis.core.ui.admin.permission;

import com.google.auto.value.AutoValue;
import org.molgenis.security.core.Permission;

@AutoValue
@SuppressWarnings("squid:S1610") // Autovalue needs an abstract class
public abstract class PermissionResponse {
  public abstract String getType();

  public abstract String getName();

  public abstract String getDescription();

  public static PermissionResponse create(String type, String name, String description) {
    return new AutoValue_PermissionResponse(type, name, description);
  }

  public static PermissionResponse create(Permission permission) {
    return new AutoValue_PermissionResponse(
        permission.getType(), permission.name(), permission.getDefaultDescription());
  }
}
