package org.molgenis.data.security.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.meta.model.Package;
import org.molgenis.data.security.PackagePermission;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class ParentPackagePermissionDeniedException extends PermissionDeniedException {
  private static final String ERROR_CODE = "DS08";
  private final PackagePermission permission;
  private final transient Package pack;

  public ParentPackagePermissionDeniedException(PackagePermission permission, Package pack) {
    super(ERROR_CODE);
    this.permission = requireNonNull(permission);
    this.pack = requireNonNull(pack);
  }

  @Override
  public String getMessage() {
    return String.format(
        "permission:%s package:%s parent:%s",
        permission.getName(), pack.getId(), pack.getParent().getId());
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {permission.getName(), pack.getLabel()};
  }
}
