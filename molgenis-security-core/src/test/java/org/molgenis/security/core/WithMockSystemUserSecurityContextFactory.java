package org.molgenis.security.core;

import static java.util.Collections.emptySet;

import org.molgenis.security.core.runas.SystemSecurityToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

/** Factory for the @WithMockSystemUser annotation. */
class WithMockSystemUserSecurityContextFactory
    implements WithSecurityContextFactory<WithMockSystemUser> {

  @Override
  public SecurityContext createSecurityContext(WithMockSystemUser systemUser) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();

    var name = systemUser.originalUsername();
    if (!name.isEmpty()) {
      var user = new User(name, name, emptySet());
      var auth = new UsernamePasswordAuthenticationToken(user, name, emptySet());
      context.setAuthentication(SystemSecurityToken.createElevated(auth));
    } else {
      context.setAuthentication(SystemSecurityToken.create());
    }

    return context;
  }
}
