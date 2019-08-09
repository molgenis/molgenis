package org.molgenis.security.oidc;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.security.oidc.model.OidcClientMetadata.ID_TOKEN_CLAIM_ROLE_PATH;

import com.jayway.jsonpath.JsonPath;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.user.UserDetailsServiceImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
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
    authorities.addAll(
        getAuthoritiesFromToken(oidcUser.getIdToken(), userRequest.getClientRegistration()));
    return new MappedOidcUser(
        authorities,
        oidcUser.getIdToken(),
        oidcUser.getUserInfo(),
        userNameAttributeName,
        user.getUsername());
  }

  /**
   * Retrieves authorities from ID token's claims.
   *
   * @param token the {@link OidcIdToken} to retrieve the roles from
   * @param clientRegistration the {@link ClientRegistration} for the OIDC client.
   * @return Set of {@link GrantedAuthority}s retrieved from the ID token's claims
   */
  private static Set<GrantedAuthority> getAuthoritiesFromToken(
      OidcIdToken token, ClientRegistration clientRegistration) {
    Object jsonPath =
        clientRegistration
            .getProviderDetails()
            .getConfigurationMetadata()
            .get(ID_TOKEN_CLAIM_ROLE_PATH);
    if (jsonPath instanceof String) {
      return JsonPath.<List<String>>read(token.getClaims(), (String) jsonPath).stream()
          .map(SimpleGrantedAuthority::new)
          .collect(toSet());
    }
    return Collections.emptySet();
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
