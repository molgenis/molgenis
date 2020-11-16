package org.molgenis.security.oidc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.security.config.oauth2.client.CommonOAuth2Provider.GOOGLE;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.EMAIL;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.SUB;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.oidc.model.OidcClientMetadata;
import org.molgenis.security.user.UserDetailsServiceImpl;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

class MappedOidcUserServiceTest extends AbstractMockitoTest {
  @Mock private OidcUserMapper oidcUserMapper;
  @Mock private UserDetailsServiceImpl userDetailsService;
  @Mock private DataService dataService;
  @Mock private OidcUserService delegate;
  @Mock private OidcUserRequest oidcUserRequest;
  @Mock private OidcIdToken oidcIdToken;
  @Mock private OidcClient oidcClient;
  @Mock private User user;

  private MappedOidcUserService mappedOidcUserService;
  private ClientRegistration registration;

  @BeforeEach
  void setUpBeforeMethod() {
    mappedOidcUserService =
        new MappedOidcUserService(delegate, oidcUserMapper, userDetailsService, dataService);
    registration =
        GOOGLE.getBuilder("google").clientId("clientId").clientSecret("clientSecret").build();
  }

  @Test
  void testMappedOidcUserService() {
    assertThrows(NullPointerException.class, () -> new MappedOidcUserService(null, null, null));
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
    when(oidcUserMapper.toUser(userCaptor.capture(), eq(oidcClient))).thenReturn(user);

    doReturn(molgenisRoles).when(userDetailsService).getAuthorities(user);
    when(user.getUsername()).thenReturn("molgenis");

    OidcUser result = mappedOidcUserService.loadUser(oidcUserRequest);

    assertEquals("molgenis", result.getName());
    assertEquals(molgenisRoles, result.getAuthorities());
    assertEquals("user@example.org", result.getEmail());
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
    when(oidcUserMapper.toUser(userCaptor.capture(), eq(oidcClient))).thenReturn(user);

    doReturn(molgenisRoles).when(userDetailsService).getAuthorities(user);
    when(user.getUsername()).thenReturn("molgenis");

    OidcUser result = mappedOidcUserService.loadUser(oidcUserRequest);

    assertEquals("molgenis", result.getName());
    assertEquals(molgenisRoles, result.getAuthorities());
    assertEquals("user@example.org", result.getEmail());
  }
}
