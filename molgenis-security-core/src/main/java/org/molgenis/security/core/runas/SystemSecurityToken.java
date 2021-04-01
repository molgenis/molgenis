package org.molgenis.security.core.runas;

import static org.molgenis.security.core.utils.SecurityUtils.ROLE_ACL_GENERAL_CHANGES;
import static org.molgenis.security.core.utils.SecurityUtils.ROLE_ACL_MODIFY_AUDITING;
import static org.molgenis.security.core.utils.SecurityUtils.ROLE_ACL_TAKE_OWNERSHIP;
import static org.molgenis.security.core.utils.SecurityUtils.ROLE_SYSTEM;

import com.google.common.collect.ImmutableList;
import java.security.Principal;
import java.util.Optional;
import javax.annotation.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** Authentication token for the SYSTEM user */
public class SystemSecurityToken extends AbstractAuthenticationToken {

  private static final ImmutableList<SimpleGrantedAuthority> AUTHORITIES =
      ImmutableList.of(
          new SimpleGrantedAuthority(ROLE_SYSTEM),
          new SimpleGrantedAuthority(ROLE_ACL_TAKE_OWNERSHIP),
          new SimpleGrantedAuthority(ROLE_ACL_MODIFY_AUDITING),
          new SimpleGrantedAuthority(ROLE_ACL_GENERAL_CHANGES));

  private final Authentication originalAuthentication;

  private SystemSecurityToken(@Nullable Authentication originalAuthentication) {
    super(AUTHORITIES);

    if (originalAuthentication instanceof SystemSecurityToken) {
      throw new IllegalStateException("Can't \"run as system\" as system");
    }

    this.originalAuthentication = originalAuthentication;
  }

  /**
   * Factory method to create a standard SystemSecurityToken.
   *
   * @return a SystemSecurityToken
   */
  public static SystemSecurityToken create() {
    return new SystemSecurityToken(null);
  }

  /**
   * Factory method to elevate an existing authentication to SYSTEM, a.k.a. "run as system".
   *
   * @param originalAuthentication the (non-system) authentication to elevate
   * @throws IllegalStateException if the authentication is already a SystemSecurityToken
   * @return a SystemSecurityToken with an original authentication
   */
  public static SystemSecurityToken createElevated(Authentication originalAuthentication) {
    return new SystemSecurityToken(originalAuthentication);
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getPrincipal() {
    return SystemPrincipal.getInstance();
  }

  /**
   * Gets the original authentication if this SystemSecurityToken represents an elevated
   * authentication.
   *
   * @return an Optional with the original authentication if present, empty Optional otherwise
   */
  public Optional<Authentication> getOriginalAuthentication() {
    return Optional.ofNullable(originalAuthentication);
  }

  @Override
  public boolean isAuthenticated() {
    return true;
  }

  public static class SystemPrincipal implements Principal {

    private static final SystemPrincipal INSTANCE = new SystemPrincipal();
    private static final String SYSTEM = "SYSTEM";

    private SystemPrincipal() {}

    public static SystemPrincipal getInstance() {
      return SystemPrincipal.INSTANCE;
    }

    @Override
    public String getName() {
      return SYSTEM;
    }

    @Override
    public boolean equals(Object another) {
      return this == another;
    }

    @Override
    public int hashCode() {
      return SystemPrincipal.class.hashCode();
    }

    @Override
    public String toString() {
      return "[principal: " + SYSTEM + "]";
    }
  }
}
