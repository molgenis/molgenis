package org.molgenis.security.core.runas;

import static org.molgenis.security.core.utils.SecurityUtils.ROLE_ACL_GENERAL_CHANGES;
import static org.molgenis.security.core.utils.SecurityUtils.ROLE_ACL_MODIFY_AUDITING;
import static org.molgenis.security.core.utils.SecurityUtils.ROLE_ACL_TAKE_OWNERSHIP;

import com.google.common.collect.ImmutableList;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** Authentication token for the SYSTEM user */
public class SystemSecurityToken extends AbstractAuthenticationToken {
  private static final SystemSecurityToken INSTANCE = new SystemSecurityToken();

  public static final String ROLE_SYSTEM = "ROLE_SYSTEM";

  private SystemSecurityToken() {
    super(
        ImmutableList.of(
            new SimpleGrantedAuthority(ROLE_SYSTEM),
            new SimpleGrantedAuthority(ROLE_ACL_TAKE_OWNERSHIP),
            new SimpleGrantedAuthority(ROLE_ACL_MODIFY_AUDITING),
            new SimpleGrantedAuthority(ROLE_ACL_GENERAL_CHANGES)));
  }

  public static SystemSecurityToken getInstance() {
    return INSTANCE;
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getPrincipal() {
    return SystemPrincipal.getInstance();
  }

  public static class SystemPrincipal {
    private static final SystemPrincipal INSTANCE = new SystemPrincipal();

    private SystemPrincipal() {}

    public static SystemPrincipal getInstance() {
      return SystemPrincipal.INSTANCE;
    }
  }
}
