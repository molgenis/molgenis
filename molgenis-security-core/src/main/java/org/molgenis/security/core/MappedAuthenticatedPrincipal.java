package org.molgenis.security.core;

import org.springframework.security.core.AuthenticatedPrincipal;

/**
 * Representation of an authenticated <code>Principal</code> that exposes a mapping of the name to
 * another name.
 */
public interface MappedAuthenticatedPrincipal extends AuthenticatedPrincipal {
  /**
   * Returns the mapped name of the authenticated <code>Principal</code>. Never <code>null</code>.
   *
   * @return the mapped name of the authenticated <code>Principal</code>
   */
  String getMappedName();
}
