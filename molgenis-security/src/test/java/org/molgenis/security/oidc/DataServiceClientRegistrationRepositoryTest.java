package org.molgenis.security.oidc;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.security.oidc.model.OidcClientMetadata.CLAIMS_ROLE_PATH;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.SUB;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

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
    String clientAuthenticationMethod = CLIENT_SECRET_BASIC.getValue();
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
    when(oidcClient.getAuthorizationUri()).thenReturn(authorizationUri);
    when(oidcClient.getTokenUri()).thenReturn(tokenUri);
    when(oidcClient.getJwkSetUri()).thenReturn(jwkSetUri);
    when(oidcClient.getScopes()).thenReturn(scopes);
    when(oidcClient.getUserInfoUri()).thenReturn(userInfoUri);
    when(oidcClient.getClaimsRolePath()).thenReturn(Optional.of("roles"));

    when(authenticationSettings.getOidcClients()).thenReturn(singleton(oidcClient));
    ClientRegistration clientRegistration =
        dataServiceClientRegistrationRepository.findByRegistrationId(registrationId);

    var expected =
        ClientRegistration.withRegistrationId(registrationId)
            .clientId(clientId)
            .clientName(clientName)
            .clientSecret(clientSecret)
            .clientAuthenticationMethod(CLIENT_SECRET_BASIC)
            .authorizationGrantType(AUTHORIZATION_CODE)
            .scope("openid", "profile", "email")
            .authorizationUri(authorizationUri)
            .tokenUri(tokenUri)
            .jwkSetUri(jwkSetUri)
            .userInfoUri(userInfoUri)
            .userNameAttributeName(SUB)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .providerConfigurationMetadata(Map.of(CLAIMS_ROLE_PATH, "roles"))
            .build();

    assertTrue(
        new ReflectionEquals(expected, "serialVersionUID", "providerDetails")
            .matches(clientRegistration));
    assertTrue(
        new ReflectionEquals(expected.getProviderDetails(), "serialVersionUID", "userInfoEndpoint")
            .matches(clientRegistration.getProviderDetails()));
    assertTrue(
        new ReflectionEquals(
                expected.getProviderDetails().getUserInfoEndpoint(), "serialVersionUID")
            .matches(clientRegistration.getProviderDetails().getUserInfoEndpoint()));
  }

  @Test
  void testFindByRegistrationIdNotExists() {
    String registrationId = "registrationId";
    when(authenticationSettings.getOidcClients()).thenReturn(emptyList());
    assertNull(dataServiceClientRegistrationRepository.findByRegistrationId(registrationId));
  }
}
