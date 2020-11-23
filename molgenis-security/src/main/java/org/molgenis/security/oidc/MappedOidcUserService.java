package org.molgenis.security.oidc;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashSet;
import java.util.Set;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.oidc.model.OidcClientMetadata;
import org.molgenis.security.user.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class MappedOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {
  private final OidcUserMapper oidcUserMapper;
  private final UserDetailsServiceImpl userDetailsServiceImpl;
  private final DataService dataService;
  private final OidcUserService delegate;

  private static final Logger LOGGER = getLogger(MappedOidcUserService.class);

  public MappedOidcUserService(
      OidcUserMapper oidcUserMapper,
      UserDetailsServiceImpl userDetailsServiceImpl,
      DataService dataService) {
    this(new OidcUserService(), oidcUserMapper, userDetailsServiceImpl, dataService);
  }

  MappedOidcUserService(
      OidcUserService delegate,
      OidcUserMapper oidcUserMapper,
      UserDetailsServiceImpl userDetailsServiceImpl,
      DataService dataService) {
    this.delegate = requireNonNull(delegate);
    this.oidcUserMapper = requireNonNull(oidcUserMapper);
    this.userDetailsServiceImpl = requireNonNull(userDetailsServiceImpl);
    this.dataService = requireNonNull(dataService);
  }

  private OidcClient getOidcClient(OidcUserRequest userRequest) {
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    OidcClient oidcClient =
        dataService.findOneById(OidcClientMetadata.OIDC_CLIENT, registrationId, OidcClient.class);
    if (oidcClient == null) {
      throw new UnknownEntityException(OidcClientMetadata.OIDC_CLIENT, registrationId);
    }
    return oidcClient;
  }

  @RunAsSystem
  @Override
  public MappedOidcUser loadUser(OidcUserRequest userRequest) {
    // load user first to guarantee successful authentication
    OidcUser oidcUserFromParent = delegate.loadUser(userRequest);
    return createOidcUser(oidcUserFromParent, userRequest);
  }

  private MappedOidcUser createOidcUser(OidcUser oidcUserFromParent, OidcUserRequest userRequest) {
    OidcClient oidcClient = getOidcClient(userRequest);
    String emailAttributeName = oidcClient.getEmailAttributeName();
    String usernameAttributeName = oidcClient.getUsernameAttributeName();
    LOGGER.debug(
        "Mapping OidcUser {} with email attribute '{}' and username attribute '{}'...",
        oidcUserFromParent,
        emailAttributeName,
        usernameAttributeName);
    OidcUser oidcUser =
        new MappedOidcUser(oidcUserFromParent, emailAttributeName, usernameAttributeName);
    User user = oidcUserMapper.toUser(oidcUser, oidcClient);
    Set<GrantedAuthority> authorities = new HashSet<>(userDetailsServiceImpl.getAuthorities(user));
    var result =
        new MappedOidcUser(oidcUserFromParent, authorities, emailAttributeName, user.getUsername());
    LOGGER.debug("Mapped to {}.", result);
    return result;
  }
}
