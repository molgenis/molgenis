package org.molgenis.security.oidc;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

/** {@link DefaultOidcUser} with a mapped username. */
public class MappedOidcUser extends DefaultOidcUser {

  private final String username;

  MappedOidcUser(
      Set<GrantedAuthority> authorities,
      OidcIdToken idToken,
      OidcUserInfo userInfo,
      String username) {
    super(authorities, idToken, userInfo);
    this.username = requireNonNull(username);
  }

  /**
   * User permissions get assigned to the MOLGENIS username.
   *
   * <p>{@link org.springframework.security.acls.domain.PrincipalSid#PrincipalSid(Authentication)}
   * calls {@link Authentication#getName()} to check ACLs. {@link
   * AbstractAuthenticationToken#getName()} calls {@link AuthenticatedPrincipal#getName()} on its
   * {@link AbstractAuthenticationToken#getPrincipal()}
   *
   * <p>Our parent uses the nameAttributeKey but the MOLGENIS username cannot be derived from the
   * claims because the mapping between them is configurable.
   *
   * <p>Fixes: https://github.com/molgenis/molgenis/issues/8985
   */
  @Override
  public String getName() {
    return username;
  }
}
