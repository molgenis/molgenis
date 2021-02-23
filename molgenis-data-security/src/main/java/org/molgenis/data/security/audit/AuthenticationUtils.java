package org.molgenis.data.security.audit;

import java.util.Optional;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class AuthenticationUtils {

  static boolean isRunByUser() {
    var auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth instanceof SystemSecurityToken) {
      Optional<Authentication> originalAuth =
          ((SystemSecurityToken) auth).getOriginalAuthentication();
      return originalAuth.isPresent() && !(originalAuth.get() instanceof SystemSecurityToken);
    }
    return true;
  }

  static boolean isRunAsSystem() {
    var auth = SecurityContextHolder.getContext().getAuthentication();

    return auth instanceof SystemSecurityToken
        && ((SystemSecurityToken) auth).getOriginalAuthentication().isPresent();
  }

  static String getUsername() {
    var auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth instanceof SystemSecurityToken) {
      Optional<Authentication> originalAuth =
          ((SystemSecurityToken) auth).getOriginalAuthentication();
      if (originalAuth.isPresent()) {
        return originalAuth.get().getName();
      } else {
        return "SYSTEM";
      }
    } else {
      return auth.getName();
    }
  }
}
