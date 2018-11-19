package org.molgenis.security.core;

import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;

/**
 * Util class to create security identities for users and roles.
 *
 * @see Sid
 */
public class SidUtils {

  public static final String ROLE_PREFIX = "ROLE_";

  private SidUtils() {}

  /** @return security identity for the current security context, never <tt>null</tt>. */
  public static Sid createSecurityContextSid() {
    Sid sid;

    if (SecurityUtils.currentUserIsSystem()) {
      sid = createSystemSid();
    } else if (SecurityUtils.currentUserIsAnonymous()) {
      sid = createAnonymousSid();
    } else {
      String username = SecurityUtils.getCurrentUsername();
      if (username == null) {
        throw new NullPointerException("SecurityUtils.getCurrentUsername is null");
      }
      sid = new PrincipalSid(username);
    }

    return sid;
  }

  public static Sid createUserSid(String username) {
    if (username.equals(SecurityUtils.ANONYMOUS_USERNAME)) {
      return createAnonymousSid();
    } else {
      return new PrincipalSid(username);
    }
  }

  public static Sid createRoleSid(String rolename) {
    return createAuthoritySid(createRoleAuthority(rolename));
  }

  public static Sid createAuthoritySid(String authority) {
    return new GrantedAuthoritySid(authority);
  }

  public static String createRoleAuthority(String roleName) {
    return ROLE_PREFIX + roleName;
  }

  public static String getRoleName(String authority) {
    if (!authority.startsWith(ROLE_PREFIX)) {
      throw new IllegalArgumentException("Not an authority: " + authority);
    }
    return authority.substring(ROLE_PREFIX.length());
  }

  private static Sid createSystemSid() {
    return new GrantedAuthoritySid(SecurityUtils.ROLE_SYSTEM);
  }

  public static Sid createAnonymousSid() {
    return new GrantedAuthoritySid(SecurityUtils.AUTHORITY_ANONYMOUS);
  }
}
