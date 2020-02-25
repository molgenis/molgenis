package org.molgenis.data.security;

import org.molgenis.data.security.auth.User;
import org.molgenis.security.core.SecurityContextRegistry;

/** Updates security contexts from the {@link SecurityContextRegistry}. */
public interface SessionSecurityContextUpdater {
  void resetAuthorities(User user);
}
