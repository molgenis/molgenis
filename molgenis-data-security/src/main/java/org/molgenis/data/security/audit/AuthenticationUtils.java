package org.molgenis.data.security.audit;

import org.molgenis.security.core.runas.SystemSecurityToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class AuthenticationUtils {

  private AuthenticationUtils() {}

  static boolean isRunByUser() {
    var auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth instanceof SystemSecurityToken) {
      return ((SystemSecurityToken) auth).getOriginalAuthentication().isPresent();
    } else {
      return true;
    }
  }

  static boolean isRunAsSystem() {
    var auth = SecurityContextHolder.getContext().getAuthentication();

    return auth instanceof SystemSecurityToken
        && ((SystemSecurityToken) auth).getOriginalAuthentication().isPresent();
  }

  static String getUsername() {
    var auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth instanceof SystemSecurityToken) {
      return ((SystemSecurityToken) auth)
          .getOriginalAuthentication()
          .map(Authentication::getName)
          .orElse("SYSTEM");
    } else {
      return auth.getName();
    }
  }
}
