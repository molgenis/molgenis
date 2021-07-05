package org.molgenis.security.oidc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.security.oidc.model.OidcClientMetadata.CLAIMS_ROLE_PATH;
import static org.molgenis.security.oidc.model.OidcClientMetadata.CLAIMS_VOGROUP_PATH;
import static org.springframework.security.config.oauth2.client.CommonOAuth2Provider.GOOGLE;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.SUB;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.VOGroup;
import org.molgenis.data.security.auth.VOGroupRoleMembership;
import org.molgenis.data.security.auth.VOGroupService;
import org.molgenis.data.security.permission.VOGroupRoleMembershipService;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.oidc.model.OidcClientMetadata;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

class MappedOidcUserServiceTest extends AbstractMockitoTest {
  @Mock private OidcUserMapper oidcUserMapper;
  @Mock private UserDetailsService userDetailsService;
  @Mock private UserDetailsChecker userDetailsChecker;
  @Mock private DataService dataService;
  @Mock private OidcUserService delegate;
  @Mock private OidcUserRequest oidcUserRequest;
  @Mock private OidcIdToken oidcIdToken;
  @Mock private OidcClient oidcClient;
  @Mock private UserDetails userDetails;
  @Mock private VOGroupService voGroupService;
  @Mock private VOGroupRoleMembershipService voGroupMembershipService;
  @Mock private VOGroup voGroup;
  @Mock private VOGroupRoleMembership voGroupRoleMembership;
  @Mock private Role role;
  @Mock private AuditEventPublisher auditEventPublisher;

  private MappedOidcUserService mappedOidcUserService;
  private ClientRegistration registration;
  private ClientRegistration registrationWithRolesPath;
  private ClientRegistration registrationWithVOGroupPath;

  @BeforeEach
  void setUpBeforeMethod() {
    mappedOidcUserService =
        new MappedOidcUserService(
            delegate,
            oidcUserMapper,
            userDetailsService,
            userDetailsChecker,
            dataService,
            voGroupService,
            voGroupMembershipService,
            auditEventPublisher);
    registration =
        GOOGLE.getBuilder("google").clientId("clientId").clientSecret("clientSecret").build();
    registrationWithRolesPath =
        GOOGLE
            .getBuilder("google")
            .clientId("clientId")
            .clientSecret("clientSecret")
            .providerConfigurationMetadata(Map.of(CLAIMS_ROLE_PATH, "roles"))
            .build();
    registrationWithVOGroupPath =
        GOOGLE
            .getBuilder("google")
            .clientId("clientId")
            .clientSecret("clientSecret")
            .providerConfigurationMetadata(Map.of(CLAIMS_VOGROUP_PATH, "eduperson_entitlement"))
            .build();
  }

  @Test
  void testMappedOidcUserService() {
    assertThrows(
        NullPointerException.class,
        () -> new MappedOidcUserService(null, null, null, null, null, null, null));
  }

  @Test
  void testLoadUserNormalClaims() {
    Set<GrantedAuthority> tokenAuthorities = Set.of(new SimpleGrantedAuthority("USER"));
    Set<? extends GrantedAuthority> molgenisRoles =
        Set.of(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("ABCDE_EDITOR"));
    Map<String, Object> claims =
        Map.of(SUB, "d8995976-e8d8-4390-839b-007a382fc12b", EMAIL, "user@example.org");
    when(oidcIdToken.getClaims()).thenReturn(claims);
    OidcUser oidcUser = new DefaultOidcUser(tokenAuthorities, oidcIdToken);
    when(delegate.loadUser(oidcUserRequest)).thenReturn(oidcUser);

    when(oidcUserRequest.getClientRegistration()).thenReturn(registration);

    when(dataService.findOneById(OidcClientMetadata.OIDC_CLIENT, "google", OidcClient.class))
        .thenReturn(oidcClient);
    when(oidcClient.getEmailAttributeName()).thenReturn(EMAIL);
    when(oidcClient.getUsernameAttributeName()).thenReturn(SUB);

    var userCaptor = ArgumentCaptor.forClass(OidcUser.class);
    when(oidcUserMapper.toUser(userCaptor.capture(), eq(oidcClient))).thenReturn("molgenis");
    when(userDetailsService.loadUserByUsername("molgenis")).thenReturn(userDetails);

    doReturn(molgenisRoles).when(userDetails).getAuthorities();
    when(userDetails.getUsername()).thenReturn("molgenis");

    OidcUser result = mappedOidcUserService.loadUser(oidcUserRequest);

    assertEquals("molgenis", result.getName());
    assertEquals(molgenisRoles, result.getAuthorities());
    assertEquals("user@example.org", result.getEmail());
  }

  @Test
  void testLoadUserDisabled() {
    Set<GrantedAuthority> tokenAuthorities = Set.of(new SimpleGrantedAuthority("USER"));
    Set<? extends GrantedAuthority> molgenisRoles =
        Set.of(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("ABCDE_EDITOR"));
    Map<String, Object> claims =
        Map.of(SUB, "d8995976-e8d8-4390-839b-007a382fc12b", EMAIL, "user@example.org");
    when(oidcIdToken.getClaims()).thenReturn(claims);
    OidcUser oidcUser = new DefaultOidcUser(tokenAuthorities, oidcIdToken);
    when(delegate.loadUser(oidcUserRequest)).thenReturn(oidcUser);

    when(oidcUserRequest.getClientRegistration()).thenReturn(registration);

    when(dataService.findOneById(OidcClientMetadata.OIDC_CLIENT, "google", OidcClient.class))
        .thenReturn(oidcClient);
    when(oidcClient.getEmailAttributeName()).thenReturn(EMAIL);
    when(oidcClient.getUsernameAttributeName()).thenReturn(SUB);

    var userCaptor = ArgumentCaptor.forClass(OidcUser.class);
    when(oidcUserMapper.toUser(userCaptor.capture(), eq(oidcClient))).thenReturn("molgenis");
    when(userDetailsService.loadUserByUsername("molgenis")).thenReturn(userDetails);

    doThrow(new DisabledException("User is disabled")).when(userDetailsChecker).check(userDetails);

    assertThrows(DisabledException.class, () -> mappedOidcUserService.loadUser(oidcUserRequest));
  }

  @Test
  void testLoadUserWithRolesClaim() {
    Set<GrantedAuthority> tokenAuthorities = Set.of(new SimpleGrantedAuthority("USER"));
    Set<? extends GrantedAuthority> molgenisRoles =
        Set.of(
            new SimpleGrantedAuthority("USER"),
            new SimpleGrantedAuthority("ABCDE_EDITOR"),
            new SimpleGrantedAuthority("ROLE_A"),
            new SimpleGrantedAuthority("ROLE_B"));
    Map<String, Object> claims =
        Map.of(
            SUB,
            "d8995976-e8d8-4390-839b-007a382fc12b",
            EMAIL,
            "user@example.org",
            "roles",
            List.of("A", "B"));
    when(oidcIdToken.getClaims()).thenReturn(claims);
    OidcUser oidcUser = new DefaultOidcUser(tokenAuthorities, oidcIdToken);
    when(delegate.loadUser(oidcUserRequest)).thenReturn(oidcUser);

    when(oidcUserRequest.getClientRegistration()).thenReturn(registrationWithRolesPath);

    when(dataService.findOneById(OidcClientMetadata.OIDC_CLIENT, "google", OidcClient.class))
        .thenReturn(oidcClient);
    when(oidcClient.getEmailAttributeName()).thenReturn(EMAIL);
    when(oidcClient.getUsernameAttributeName()).thenReturn(SUB);

    var userCaptor = ArgumentCaptor.forClass(OidcUser.class);
    when(oidcUserMapper.toUser(userCaptor.capture(), eq(oidcClient))).thenReturn("molgenis");
    when(userDetailsService.loadUserByUsername("molgenis")).thenReturn(userDetails);

    doReturn(
            List.of(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("ABCDE_EDITOR")))
        .when(userDetails)
        .getAuthorities();
    when(userDetails.getUsername()).thenReturn("molgenis");

    OidcUser result = mappedOidcUserService.loadUser(oidcUserRequest);

    assertEquals("molgenis", result.getName());
    assertEquals(molgenisRoles, result.getAuthorities());
    assertEquals("user@example.org", result.getEmail());
    verify(auditEventPublisher)
        .publish(
            "molgenis",
            "GRANT_ROLES_FROM_CLAIMS",
            Map.of(
                "rolesFromRolesClaim",
                Set.of("ROLE_A", "ROLE_B"),
                "voGroups",
                Set.of(),
                "rolesFromVoGroupsClaim",
                Set.of()));
  }

  @Test
  void testLoadUserWithVOGroupClaim() {
    Set<GrantedAuthority> tokenAuthorities = Set.of(new SimpleGrantedAuthority("USER"));
    Set<? extends GrantedAuthority> molgenisRoles =
        Set.of(
            new SimpleGrantedAuthority("USER"),
            new SimpleGrantedAuthority("ABCDE_EDITOR"),
            new SimpleGrantedAuthority("ROLE_A"));
    Map<String, Object> claims =
        Map.of(
            SUB,
            "d8995976-e8d8-4390-839b-007a382fc12b",
            EMAIL,
            "user@example.org",
            "eduperson_entitlement",
            List.of("urn:mace:surf.nl:sram:group:molgenis:dev"));
    when(oidcIdToken.getClaims()).thenReturn(claims);
    OidcUser oidcUser = new DefaultOidcUser(tokenAuthorities, oidcIdToken);
    when(delegate.loadUser(oidcUserRequest)).thenReturn(oidcUser);

    when(oidcUserRequest.getClientRegistration()).thenReturn(registrationWithVOGroupPath);

    when(dataService.findOneById(OidcClientMetadata.OIDC_CLIENT, "google", OidcClient.class))
        .thenReturn(oidcClient);
    when(oidcClient.getEmailAttributeName()).thenReturn(EMAIL);
    when(oidcClient.getUsernameAttributeName()).thenReturn(SUB);

    var userCaptor = ArgumentCaptor.forClass(OidcUser.class);
    when(oidcUserMapper.toUser(userCaptor.capture(), eq(oidcClient))).thenReturn("molgenis");
    when(userDetailsService.loadUserByUsername("molgenis")).thenReturn(userDetails);

    doReturn(
            List.of(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("ABCDE_EDITOR")))
        .when(userDetails)
        .getAuthorities();
    when(userDetails.getUsername()).thenReturn("molgenis");

    when(voGroupService.getGroups(Set.of("urn:mace:surf.nl:sram:group:molgenis:dev")))
        .thenReturn(List.of(voGroup));
    when(voGroupMembershipService.getCurrentMemberships(List.of(voGroup)))
        .thenReturn(List.of(voGroupRoleMembership));
    when(voGroupRoleMembership.getRole()).thenReturn(role);
    when(role.getName()).thenReturn("A");

    OidcUser result = mappedOidcUserService.loadUser(oidcUserRequest);

    assertEquals("molgenis", result.getName());
    assertEquals(molgenisRoles, result.getAuthorities());
    assertEquals("user@example.org", result.getEmail());
    verify(auditEventPublisher)
        .publish(
            "molgenis",
            "GRANT_ROLES_FROM_CLAIMS",
            Map.of(
                "rolesFromRolesClaim",
                Set.of(),
                "voGroups",
                Set.of("urn:mace:surf.nl:sram:group:molgenis:dev"),
                "rolesFromVoGroupsClaim",
                Set.of("ROLE_A")));
  }

  @Test
  void testLoadUserCustomEmailClaim() {
    Set<GrantedAuthority> tokenAuthorities = Set.of(new SimpleGrantedAuthority("USER"));
    Set<? extends GrantedAuthority> molgenisRoles =
        Set.of(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("ABCDE_EDITOR"));
    Map<String, Object> claims =
        Map.of(SUB, "d8995976-e8d8-4390-839b-007a382fc12b", "emailAddress", "user@example.org");
    when(oidcIdToken.getClaims()).thenReturn(claims);
    OidcUser oidcUser = new DefaultOidcUser(tokenAuthorities, oidcIdToken);
    when(delegate.loadUser(oidcUserRequest)).thenReturn(oidcUser);

    when(oidcUserRequest.getClientRegistration()).thenReturn(registration);

    when(dataService.findOneById(OidcClientMetadata.OIDC_CLIENT, "google", OidcClient.class))
        .thenReturn(oidcClient);
    when(oidcClient.getEmailAttributeName()).thenReturn("emailAddress");
    when(oidcClient.getUsernameAttributeName()).thenReturn(SUB);

    var userCaptor = ArgumentCaptor.forClass(OidcUser.class);
    when(oidcUserMapper.toUser(userCaptor.capture(), eq(oidcClient))).thenReturn("molgenis");
    when(userDetailsService.loadUserByUsername("molgenis")).thenReturn(userDetails);

    doReturn(molgenisRoles).when(userDetails).getAuthorities();
    when(userDetails.getUsername()).thenReturn("molgenis");

    OidcUser result = mappedOidcUserService.loadUser(oidcUserRequest);

    assertEquals("molgenis", result.getName());
    assertEquals(molgenisRoles, result.getAuthorities());
    assertEquals("user@example.org", result.getEmail());
  }
}
