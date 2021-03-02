package org.molgenis.security.core.runas;

import static java.util.Objects.requireNonNull;

import org.springframework.security.core.Authentication;

/**
 * A system token that can be used to elevate a non-system authentication to system.
 */
public class RunAsSystemToken extends SystemSecurityToken {

  private final Authentication originalAuthentication;

  public RunAsSystemToken(Authentication originalAuthentication) {
    super();

    this.originalAuthentication = requireNonNull(originalAuthentication);

    if (originalAuthentication instanceof SystemSecurityToken) {
      throw new IllegalStateException("Can't \"run as system\" as system");
    }
  }

  /**
   * Gets the original authentication
   *
   * @return the original authentication
   */
  public Authentication getOriginalAuthentication() {
    return originalAuthentication;
  }
}
