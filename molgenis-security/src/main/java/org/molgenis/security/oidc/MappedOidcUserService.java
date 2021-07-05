package org.molgenis.security.oidc;

import static com.jayway.jsonpath.Option.DEFAULT_PATH_LEAF_TO_NULL;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.security.oidc.model.OidcClientMetadata.CLAIMS_ROLE_PATH;
import static org.molgenis.security.oidc.model.OidcClientMetadata.CLAIMS_VOGROUP_PATH;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.collect.Sets;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.VOGroup;
import org.molgenis.data.security.auth.VOGroupRoleMembership;
import org.molgenis.data.security.auth.VOGroupService;
import org.molgenis.data.security.permission.VOGroupRoleMembershipService;
import org.molgenis.security.core.SidUtils;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.oidc.model.OidcClientMetadata;
import org.molgenis.security.user.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class MappedOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

  private final OidcUserMapper oidcUserMapper;
  private final UserDetailsServiceImpl userDetailsServiceImpl;
  private final DataService dataService;
  private final OidcUserService delegate;
  private final VOGroupService voGroupService;
  private final VOGroupRoleMembershipService voGroupRoleMembershipService;
  private final AuditEventPublisher auditEventPublisher;

  private static final Logger LOGGER = getLogger(MappedOidcUserService.class);
  public static final Configuration JSONPATH_CONFIGURATION =
      Configuration.builder().options(DEFAULT_PATH_LEAF_TO_NULL).build();

  public MappedOidcUserService(
      OidcUserMapper oidcUserMapper,
      UserDetailsServiceImpl userDetailsServiceImpl,
      DataService dataService,
      VOGroupService voGroupService,
      VOGroupRoleMembershipService voGroupRoleMembershipService,
      AuditEventPublisher auditEventPublisher) {
    this(
        new OidcUserService(),
        oidcUserMapper,
        userDetailsServiceImpl,
        dataService,
        voGroupService,
        voGroupRoleMembershipService,
        auditEventPublisher);
  }

  MappedOidcUserService(
      OidcUserService delegate,
      OidcUserMapper oidcUserMapper,
      UserDetailsServiceImpl userDetailsServiceImpl,
      DataService dataService,
      VOGroupService voGroupService,
      VOGroupRoleMembershipService voGroupRoleMembershipService,
      AuditEventPublisher auditEventPublisher) {
    this.delegate = requireNonNull(delegate);
    this.oidcUserMapper = requireNonNull(oidcUserMapper);
    this.userDetailsServiceImpl = requireNonNull(userDetailsServiceImpl);
    this.dataService = requireNonNull(dataService);
    this.voGroupService = requireNonNull(voGroupService);
    this.voGroupRoleMembershipService = requireNonNull(voGroupRoleMembershipService);
    this.auditEventPublisher = requireNonNull(auditEventPublisher);
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
    authorities.addAll(
        getAuthoritiesFromClaims(
            user.getUsername(), oidcUser.getClaims(), userRequest.getClientRegistration()));
    var result =
        new MappedOidcUser(oidcUserFromParent, authorities, emailAttributeName, user.getUsername());
    LOGGER.debug("Mapped to {}.", result);
    return result;
  }

  /**
   * Retrieves authorities from claims.
   *
   * @param claims the {@link OidcIdToken} to retrieve the roles from
   * @param clientRegistration the {@link ClientRegistration} for the OIDC client.
   * @return Set of {@link GrantedAuthority}s retrieved from the ID token's claims
   */
  private Set<GrantedAuthority> getAuthoritiesFromClaims(
      String principal, Map<String, Object> claims, ClientRegistration clientRegistration) {
    final var configurationMetadata =
        clientRegistration.getProviderDetails().getConfigurationMetadata();
    Set<String> rolesFromClaim =
        streamClaimsForPath(claims, configurationMetadata.get(CLAIMS_ROLE_PATH))
            .map(SidUtils::createRoleAuthority)
            .collect(toSet());
    Set<String> voGroupsFromClaim =
        streamClaimsForPath(claims, configurationMetadata.get(CLAIMS_VOGROUP_PATH))
            .collect(toSet());
    Collection<VOGroup> voGroups = voGroupService.getGroups(voGroupsFromClaim);
    Set<String> rolesFromVoGroups =
        voGroupRoleMembershipService.getCurrentMemberships(voGroups).stream()
            .map(VOGroupRoleMembership::getRole)
            .map(Role::getName)
            .map(SidUtils::createRoleAuthority)
            .collect(Collectors.toSet());
    final var allRoles = Sets.union(rolesFromClaim, rolesFromVoGroups);
    if (!allRoles.isEmpty()) {
      auditEventPublisher.publish(
          principal,
          "GRANT_ROLES_FROM_CLAIMS",
          Map.of(
              "rolesFromRolesClaim", rolesFromClaim,
              "voGroups", voGroupsFromClaim,
              "rolesFromVoGroupsClaim", rolesFromVoGroups));
    }
    return allRoles.stream().map(SimpleGrantedAuthority::new).collect(toSet());
  }

  static Stream<String> streamClaimsForPath(Map<String, Object> claims, @Nullable Object path) {
    return Optional.ofNullable(path).filter(String.class::isInstance).map(String.class::cast)
        .map(
            pathString ->
                JsonPath.using(JSONPATH_CONFIGURATION).parse(claims).<List<String>>read(pathString))
        .stream()
        .flatMap(List::stream);
  }
}
