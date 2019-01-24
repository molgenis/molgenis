package org.molgenis.security.oidc;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Set;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.user.UserDetailsServiceImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class MappedOidcUserService extends OidcUserService {
  private final OidcUserMapper oidcUserMapper;
  private final UserDetailsServiceImpl userDetailsServiceImpl;

  public MappedOidcUserService(
      OidcUserMapper oidcUserMapper, UserDetailsServiceImpl userDetailsServiceImpl) {
    this.oidcUserMapper = requireNonNull(oidcUserMapper);
    this.userDetailsServiceImpl = requireNonNull(userDetailsServiceImpl);
  }

  @Override
  public MappedOidcUser loadUser(OidcUserRequest userRequest) {
    // load user first to guarantee successful authentication
    OidcUser oidcUser = super.loadUser(userRequest);
    return createOidcUser(oidcUser, userRequest);
  }

  private MappedOidcUser createOidcUser(OidcUser oidcUser, OidcUserRequest userRequest) {
    User user = oidcUserMapper.toUser(oidcUser, userRequest);
    String userNameAttributeName = getUserNameAttributeName(userRequest);
    Set<GrantedAuthority> authorities = new HashSet<>(userDetailsServiceImpl.getAuthorities(user));
    return new MappedOidcUser(
        authorities,
        oidcUser.getIdToken(),
        oidcUser.getUserInfo(),
        userNameAttributeName,
        user.getUsername());
  }

  /** package-private for testability */
  private String getUserNameAttributeName(OidcUserRequest userRequest) {
    return userRequest
        .getClientRegistration()
        .getProviderDetails()
        .getUserInfoEndpoint()
        .getUserNameAttributeName();
  }
}
