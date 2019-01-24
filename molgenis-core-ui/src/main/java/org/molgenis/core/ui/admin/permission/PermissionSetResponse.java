package org.molgenis.core.ui.admin.permission;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import com.google.auto.value.AutoValue;
import java.util.List;
import java.util.Set;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionSet;

@AutoValue
@SuppressWarnings("squid:S1610") // Autovalue needs an abstract class
public abstract class PermissionSetResponse {
  public abstract String getName();

  public abstract List<PermissionResponse> getPermissions();

  public static PermissionSetResponse create(String name, List<PermissionResponse> permissions) {
    return new AutoValue_PermissionSetResponse(name, permissions);
  }

  public static PermissionSetResponse create(
      PermissionSet permissionSet, Set<Permission> permissions) {
    List<PermissionResponse> permissionsGranted =
        permissions
            .stream()
            .map(PermissionResponse::create)
            .sorted(comparing(PermissionResponse::getType))
            .collect(toList());
    return new AutoValue_PermissionSetResponse(permissionSet.name(), permissionsGranted);
  }
}
