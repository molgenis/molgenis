package org.molgenis.security.oidc;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.HashSet;
import org.mockito.Mock;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails.UserInfoEndpoint;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataServiceClientRegistrationRepositoryTest extends AbstractMockitoTest {
  @Mock private AuthenticationSettings authenticationSettings;
  private DataServiceClientRegistrationRepository dataServiceClientRegistrationRepository;

  @BeforeMethod
  public void setUpBeforeMethod() {
    dataServiceClientRegistrationRepository =
        new DataServiceClientRegistrationRepository(authenticationSettings);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testDataServiceClientRegistrationRepository() {
    new DataServiceClientRegistrationRepository(null);
  }

  @Test
  public void testFindByRegistrationId() {
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
    String usernameAttributeName = "usernameAttributeName";

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
    when(oidcClient.getUsernameAttributeName()).thenReturn(usernameAttributeName);

    when(authenticationSettings.getOidcClients()).thenReturn(singleton(oidcClient));
    ClientRegistration clientRegistration =
        dataServiceClientRegistrationRepository.findByRegistrationId(registrationId);

    // ClientRegistration doesn't implement equals, so we have to compare fields ourselves
    assertEquals(clientRegistration.getRegistrationId(), registrationId);
    assertEquals(clientRegistration.getClientId(), clientId);
    assertEquals(clientRegistration.getClientName(), clientName);
    assertEquals(clientRegistration.getClientSecret(), clientSecret);
    assertEquals(
        clientRegistration.getClientAuthenticationMethod(), ClientAuthenticationMethod.BASIC);
    assertEquals(
        clientRegistration.getAuthorizationGrantType(), AuthorizationGrantType.AUTHORIZATION_CODE);
    assertEquals(clientRegistration.getScopes(), new HashSet<>(Arrays.asList(scopes)));
    ProviderDetails providerDetails = clientRegistration.getProviderDetails();
    assertEquals(providerDetails.getAuthorizationUri(), authorizationUri);
    assertEquals(providerDetails.getTokenUri(), tokenUri);
    assertEquals(providerDetails.getJwkSetUri(), jwkSetUri);
    UserInfoEndpoint userInfoEndpoint = providerDetails.getUserInfoEndpoint();
    assertEquals(userInfoEndpoint.getUri(), userInfoUri);
    assertEquals(userInfoEndpoint.getUserNameAttributeName(), usernameAttributeName);
    assertEquals(
        clientRegistration.getRedirectUriTemplate(),
        "{baseUrl}/login/oauth2/code/{registrationId}");
  }

  @Test
  public void testFindByRegistrationIdNotExists() {
    String registrationId = "registrationId";
    when(authenticationSettings.getOidcClients()).thenReturn(emptyList());
    assertNull(dataServiceClientRegistrationRepository.findByRegistrationId(registrationId));
  }
}
