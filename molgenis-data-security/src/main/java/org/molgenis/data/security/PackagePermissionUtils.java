package org.molgenis.data.security;

import static org.molgenis.data.security.PackagePermission.ADD_ENTITY_TYPE;
import static org.molgenis.data.security.PackagePermission.ADD_PACKAGE;
import static org.molgenis.data.security.PackagePermission.VIEW;

import org.molgenis.data.meta.model.Package;
import org.molgenis.data.util.PackageUtils;
import org.molgenis.security.core.UserPermissionEvaluator;

public class PackagePermissionUtils {
  private PackagePermissionUtils() {}

  /** @return whether the current user can add entity types or add packages to this package */
  public static boolean isReadablePackage(
      Package aPackage, UserPermissionEvaluator userPermissionEvaluator) {
    return isPermittedPackage(aPackage, userPermissionEvaluator, VIEW);
  }

  /** @return whether the current user can add entity types or add packages to this package */
  public static boolean isWritablePackage(
      Package aPackage, UserPermissionEvaluator userPermissionEvaluator) {
    return isPermittedPackage(aPackage, userPermissionEvaluator, ADD_PACKAGE);
  }

  private static boolean isPermittedPackage(
      Package aPackage,
      UserPermissionEvaluator userPermissionEvaluator,
      PackagePermission packagePermission) {
    PackageIdentity packageIdentity = new PackageIdentity(aPackage);
    return !PackageUtils.isSystemPackage(aPackage)
        && (userPermissionEvaluator.hasPermission(packageIdentity, ADD_ENTITY_TYPE)
            || userPermissionEvaluator.hasPermission(packageIdentity, packagePermission));
  }
}
