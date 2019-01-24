package org.molgenis.security.oidc;

import static java.util.Objects.requireNonNull;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/** Internal exception */
class OidcUserMissingEmailException extends RuntimeException {

  private final transient OidcUser oidcUser;

  OidcUserMissingEmailException(OidcUser oidcUser) {
    this.oidcUser = requireNonNull(oidcUser);
  }

  @Override
  public String getMessage() {
    return "email claim missing for subject '" + oidcUser.getSubject() + '\'';
  }
}
