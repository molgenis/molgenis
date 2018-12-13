package org.molgenis.security.core.utils;

import java.util.Collection;
import javax.annotation.CheckForNull;
import org.molgenis.security.core.MappedAuthenticatedPrincipal;
import org.molgenis.security.core.runas.SystemSecurityToken.SystemPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils {
  public static final String ANONYMOUS_USERNAME = "anonymous";

  public static final String AUTHORITY_SU = "ROLE_SU";
  public static final String AUTHORITY_ANONYMOUS = "ROLE_ANONYMOUS";
  public static final String AUTHORITY_USER = "ROLE_USER";
  public static final String ROLE_SYSTEM = "ROLE_SYSTEM";

  public static final String ROLE_ACL_TAKE_OWNERSHIP = "ROLE_ACL_TAKE_OWNERSHIP";
  public static final String ROLE_ACL_MODIFY_AUDITING = "ROLE_ACL_MODIFY_AUDITING";
  public static final String ROLE_ACL_GENERAL_CHANGES = "ROLE_ACL_GENERAL_CHANGES";

  private SecurityUtils() {}

  /**
   * Returns the username of the current authentication.
   *
   * @return username or <tt>null</tt> if 1) the current authentication is null or 2) the currently
   *     authenticated principal is the system.
   */
  public static @CheckForNull String getCurrentUsername() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return null;
    }
    return getUsername(authentication);
  }

  private static @CheckForNull String getUsername(Authentication authentication) {
    String username;

    Object principal = authentication.getPrincipal();
    if (principal == null || principal instanceof SystemPrincipal) {
      username = null;
    } else if (principal instanceof UserDetails) {
      username = ((UserDetails) principal).getUsername();
    } else if (principal instanceof MappedAuthenticatedPrincipal) {
      username = ((MappedAuthenticatedPrincipal) principal).getMappedName();
    } else {
      username = principal.toString();
    }

    return username;
  }

  /** Returns whether the current user has at least one of the given roles */
  public static boolean currentUserHasRole(String... roles) {
    if (roles == null || roles.length == 0) return false;

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
      Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
      if (authorities == null) throw new IllegalStateException("No user currently logged in");

      for (String role : roles) {
        for (GrantedAuthority grantedAuthority : authorities) {
          if (role.equals(grantedAuthority.getAuthority())) return true;
        }
      }
    }
    return false;
  }

  /** Returns whether the current user is a superuser or the system user. */
  public static boolean currentUserIsSuOrSystem() {
    return currentUserIsSu() || currentUserIsSystem();
  }

  /** Returns whether the current user is a super user */
  public static boolean currentUserIsSu() {
    return currentUserHasRole(AUTHORITY_SU);
  }

  /** Returns whether the current user is the system user. */
  public static boolean currentUserIsSystem() {
    return currentUserHasRole(ROLE_SYSTEM);
  }

  /** Returns whether the current user is authenticated and not the anonymous user */
  public static boolean currentUserIsAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null && authentication.isAuthenticated() && !currentUserIsAnonymous();
  }

  public static boolean currentUserIsAnonymous() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication == null || currentUserHasRole(AUTHORITY_ANONYMOUS);
  }
}
