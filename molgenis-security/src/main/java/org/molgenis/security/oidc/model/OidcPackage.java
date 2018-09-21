package org.molgenis.security.oidc.model;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.auth.SecurityPackage.PACKAGE_SECURITY;

import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.security.auth.SecurityPackage;
import org.springframework.stereotype.Component;

@Component
public class OidcPackage extends SystemPackage {
  private static final String SIMPLE_NAME = "oidc";
  static final String PACKAGE_OIDC = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

  private final SecurityPackage securityPackage;

  public OidcPackage(PackageMetadata packageMetadata, SecurityPackage securityPackage) {
    super(PACKAGE_OIDC, packageMetadata);
    this.securityPackage = requireNonNull(securityPackage);
  }

  @Override
  protected void init() {
    setLabel("OpenID Connect");
    setDescription("OpenID Connect authentication");
    setParent(securityPackage);
  }
}
