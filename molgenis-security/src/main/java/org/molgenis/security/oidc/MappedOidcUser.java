package org.molgenis.security.oidc;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import org.molgenis.security.core.MappedAuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

/** {@link DefaultOidcUser} with a mapped username. */
public class MappedOidcUser extends DefaultOidcUser implements MappedAuthenticatedPrincipal {

  private final String username;

  MappedOidcUser(
      Set<GrantedAuthority> authorities,
      OidcIdToken idToken,
      OidcUserInfo userInfo,
      String nameAttributeKey,
      String username) {
    super(authorities, idToken, userInfo, nameAttributeKey);
    this.username = requireNonNull(username);
  }

  @Override
  public String getMappedName() {
    return username;
  }

  /**
   * The constructor of PrincipalSid does a toString() when the principal of the Authentication is
   * not a UserDetails (which it isn't in the case of a MappedOidcUser).
   *
   * <p>Fixes: https://github.com/molgenis/molgenis/issues/8978
   */
  @Override
  public String toString() {
    return username;
  }
}
