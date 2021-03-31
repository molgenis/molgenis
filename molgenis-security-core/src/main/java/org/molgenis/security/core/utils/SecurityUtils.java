package org.molgenis.security.core.utils;

import java.util.Collection;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

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
   * @return a username, null if the current authentication is null
   */
  public static @Nullable @CheckForNull String getCurrentUsername() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getPrincipal() == null) {
      return null;
    } else {
      return authentication.getName();
    }
  }

  /**
   * Returns the actual username of the current authentication: in case of an elevated
   * authentication will return the original username.
   *
   * @return a username, null if the current authentication is null
   */
  public static @Nullable @CheckForNull String getActualUsername() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getPrincipal() == null) {
      return null;
    } else if (authentication instanceof SystemSecurityToken) {
      return ((SystemSecurityToken) authentication)
          .getOriginalAuthentication()
          .map(Authentication::getName)
          .orElse(authentication.getName());
    } else {
      return authentication.getName();
    }
  }

  /** Returns whether the current user has at least one of the given roles */
  public static boolean currentUserHasRole(String... roles) {
    if (roles == null || roles.length == 0) {
      return false;
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
      Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
      if (authorities == null) {
        throw new IllegalStateException("No user currently logged in");
      }

      for (String role : roles) {
        for (GrantedAuthority grantedAuthority : authorities) {
          if (role.equals(grantedAuthority.getAuthority())) {
            return true;
          }
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
    return authentication == null || (authentication instanceof AnonymousAuthenticationToken);
  }

  /**
   * Returns whether the current user is an actual user or the system. Also returns true if a user
   * is running with an elevated SystemSecurityToken.
   */
  public static boolean currentUserIsUser() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof SystemSecurityToken) {
      return ((SystemSecurityToken) auth).getOriginalAuthentication().isPresent();
    } else {
      return true;
    }
  }

  /** Returns whether the current user is running with an elevated SystemSecurityToken. */
  public static boolean currentUserIsRunningAsSystem() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    return auth instanceof SystemSecurityToken
        && ((SystemSecurityToken) auth).getOriginalAuthentication().isPresent();
  }
}
