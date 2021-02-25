package org.molgenis.data.security.audit;

import static java.util.Collections.emptySet;

import org.molgenis.security.core.runas.SystemSecurityToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

class SecurityContextTestUtils {

  static void withUser(String name) {
    var user = new User(name, name, emptySet());
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, name, emptySet()));
  }

  static void withSystemToken() {
    SecurityContextHolder.getContext().setAuthentication(SystemSecurityToken.create());
  }

  static void withElevatedUser(String name) {
    var user = new User(name, name, emptySet());
    var auth = new UsernamePasswordAuthenticationToken(user, name, emptySet());
    SecurityContextHolder.getContext()
        .setAuthentication(SystemSecurityToken.createFromElevated(auth));
  }
}
