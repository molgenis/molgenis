package org.molgenis.data.security.audit;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticationUtils {

  static boolean isUser() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return principal instanceof UserDetails;
  }

  static String getUsername() {
    return SecurityContextHolder.getContext().getAuthentication().getName();
  }
}
