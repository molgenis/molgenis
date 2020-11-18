package org.molgenis.security.oidc;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.BASIC;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.SUB;

import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails.UserInfoEndpoint;

class DataServiceClientRegistrationRepositoryTest extends AbstractMockitoTest {
  @Mock private AuthenticationSettings authenticationSettings;
  private DataServiceClientRegistrationRepository dataServiceClientRegistrationRepository;

  @BeforeEach
  void setUpBeforeMethod() {
    dataServiceClientRegistrationRepository =
        new DataServiceClientRegistrationRepository(authenticationSettings);
  }

  @Test
  void testDataServiceClientRegistrationRepository() {
    assertThrows(
        NullPointerException.class, () -> new DataServiceClientRegistrationRepository(null));
  }

  @Test
  void testFindByRegistrationId() {
    String registrationId = "registrationId";
    String clientId = "clientId";
    String clientName = "clientName";
    String clientSecret = "clientSecret";
    String clientAuthenticationMethod = "basic";
    String authorizationGrantType = "authorization_code";
    String authorizationUri = "authorizationUri";
    String tokenUri = "tokenUri";
    String jwkSetUri = "jwkSetUri";
    String[] scopes = new String[] {"openid", "profile", "email"};
    String userInfoUri = "userInfoUri";

    OidcClient oidcClient = mock(OidcClient.class);
    when(oidcClient.getRegistrationId()).thenReturn(registrationId);
    when(oidcClient.getClientId()).thenReturn(clientId);
    when(oidcClient.getClientName()).thenReturn(clientName);
    when(oidcClient.getClientSecret()).thenReturn(clientSecret);
    when(oidcClient.getClientAuthenticationMethod()).thenReturn(clientAuthenticationMethod);
    when(oidcClient.getAuthorizationGrantType()).thenReturn(authorizationGrantType);
    when(oidcClient.getAuthorizationUri()).thenReturn(authorizationUri);
    when(oidcClient.getTokenUri()).thenReturn(tokenUri);
    when(oidcClient.getJwkSetUri()).thenReturn(jwkSetUri);
    when(oidcClient.getScopes()).thenReturn(scopes);
    when(oidcClient.getUserInfoUri()).thenReturn(userInfoUri);

    when(authenticationSettings.getOidcClients()).thenReturn(singleton(oidcClient));
    ClientRegistration clientRegistration =
        dataServiceClientRegistrationRepository.findByRegistrationId(registrationId);

    // ClientRegistration doesn't implement equals, so we have to compare fields ourselves
    assertEquals(registrationId, clientRegistration.getRegistrationId());
    assertEquals(clientId, clientRegistration.getClientId());
    assertEquals(clientName, clientRegistration.getClientName());
    assertEquals(clientSecret, clientRegistration.getClientSecret());
    assertEquals(BASIC, clientRegistration.getClientAuthenticationMethod());
    assertEquals(AUTHORIZATION_CODE, clientRegistration.getAuthorizationGrantType());
    assertEquals(new HashSet<>(asList(scopes)), clientRegistration.getScopes());
    ProviderDetails providerDetails = clientRegistration.getProviderDetails();
    assertEquals(authorizationUri, providerDetails.getAuthorizationUri());
    assertEquals(tokenUri, providerDetails.getTokenUri());
    assertEquals(jwkSetUri, providerDetails.getJwkSetUri());
    UserInfoEndpoint userInfoEndpoint = providerDetails.getUserInfoEndpoint();
    assertEquals(userInfoUri, userInfoEndpoint.getUri());
    assertEquals(SUB, userInfoEndpoint.getUserNameAttributeName());
    assertEquals(
        "{baseUrl}/login/oauth2/code/{registrationId}",
        clientRegistration.getRedirectUriTemplate());
  }

  @Test
  void testFindByRegistrationIdNotExists() {
    String registrationId = "registrationId";
    when(authenticationSettings.getOidcClients()).thenReturn(emptyList());
    assertNull(dataServiceClientRegistrationRepository.findByRegistrationId(registrationId));
  }
}
