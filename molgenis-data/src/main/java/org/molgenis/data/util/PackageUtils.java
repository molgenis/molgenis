package org.molgenis.data.util;

import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

import com.google.common.collect.TreeTraverser;
import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.molgenis.data.meta.model.Package;

public class PackageUtils {

  private PackageUtils() {}

  /**
   * Returns whether the given package is a system package, i.e. it is the root system package or a
   * descendant of the root system package.
   *
   * @param aPackage package
   * @return whether package is a system package
   */
  public static boolean isSystemPackage(@CheckForNull Package aPackage) {
    if (aPackage == null) {
      return false;
    }
    return runAsSystem(
        () ->
            aPackage.getId().equals(PACKAGE_SYSTEM)
                || (aPackage.getRootPackage() != null
                    && aPackage.getRootPackage().getId().equals(PACKAGE_SYSTEM)));
  }

  /**
   * Looks through a {@link Package} and its children to see if the sought-after package is
   * contained within.
   *
   * @param pack the package to explore
   * @param wantedPackage the sought-after package
   * @return true if the sought-after package was found
   */
  public static boolean contains(Package pack, Package wantedPackage) {
    return new PackageTreeTraverser()
        .postOrderTraversal(pack)
        .anyMatch(childPackage -> Objects.equals(childPackage, wantedPackage));
  }

  /** Basic traverser for traversing {@link Package} trees. */
  public static class PackageTreeTraverser extends TreeTraverser<Package> {
    @Override
    public Iterable<Package> children(@Nonnull Package packageEntity) {
      return packageEntity.getChildren();
    }
  }
}
