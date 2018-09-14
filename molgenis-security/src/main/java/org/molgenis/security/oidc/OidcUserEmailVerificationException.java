package org.molgenis.security.oidc;

import static java.util.Objects.requireNonNull;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/** internal exception */
class OidcUserEmailVerificationException extends RuntimeException {
  private final transient OidcUser oidcUser;

  OidcUserEmailVerificationException(OidcUser oidcUser) {
    this.oidcUser = requireNonNull(oidcUser);
  }

  @Override
  public String getMessage() {
    return "email verification claim exists but evaluates to false for subject '"
        + oidcUser.getSubject()
        + '\'';
  }
}
