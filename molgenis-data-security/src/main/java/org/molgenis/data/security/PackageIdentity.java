package org.molgenis.data.security;

import org.molgenis.data.meta.model.Package;
import org.springframework.security.acls.domain.ObjectIdentityImpl;

public class PackageIdentity extends ObjectIdentityImpl {
  public static final String PACKAGE = "package";

  public PackageIdentity(Package aPackage) {
    this(aPackage.getId());
  }

  public PackageIdentity(String packageId) {
    super(PACKAGE, packageId);
  }
}
