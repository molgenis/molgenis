package org.molgenis.data.util;

import static org.molgenis.data.meta.model.EntityTypeMetadata.ATTRIBUTES;
import static org.molgenis.data.meta.model.EntityTypeMetadata.BACKEND;
import static org.molgenis.data.meta.model.EntityTypeMetadata.DESCRIPTION;
import static org.molgenis.data.meta.model.EntityTypeMetadata.EXTENDS;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ID;
import static org.molgenis.data.meta.model.EntityTypeMetadata.IS_ABSTRACT;
import static org.molgenis.data.meta.model.EntityTypeMetadata.LABEL;
import static org.molgenis.data.meta.model.EntityTypeMetadata.PACKAGE;
import static org.molgenis.data.meta.model.EntityTypeMetadata.TAGS;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

import javax.annotation.Nullable;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.Package;

public class MetaUtils {
  private MetaUtils() {}

  public static Fetch getEntityTypeFetch() {
    // TODO simplify fetch creation (in this case *all* attributes and expand xref/mrefs)
    return new Fetch()
        .field(ID)
        .field(PACKAGE)
        .field(LABEL)
        .field(DESCRIPTION)
        .field(ATTRIBUTES)
        .field(IS_ABSTRACT)
        .field(EXTENDS)
        .field(TAGS)
        .field(BACKEND);
  }

  /**
   * Returns whether the given package is a system package, i.e. it is the root system package or a
   * descendent of the root system package.
   *
   * @param aPackage package
   * @return whether package is a system package
   */
  public static boolean isSystemPackage(@Nullable Package aPackage) {
    if (aPackage == null) {
      return false;
    }
    return runAsSystem(
        () ->
            aPackage.getId().equals(PACKAGE_SYSTEM)
                || (aPackage.getRootPackage() != null
                    && aPackage.getRootPackage().getId().equals(PACKAGE_SYSTEM)));
  }
}
