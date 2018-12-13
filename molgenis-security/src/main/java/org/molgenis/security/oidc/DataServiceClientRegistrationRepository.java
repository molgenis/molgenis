package org.molgenis.security.oidc;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.settings.AuthenticationSettings;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

public class DataServiceClientRegistrationRepository implements ClientRegistrationRepository {
  private static final String DEFAULT_REDIRECT_URI_TEMPLATE =
      "{baseUrl}/login/oauth2/code/{registrationId}";

  private final AuthenticationSettings authenticationSettings;

  public DataServiceClientRegistrationRepository(AuthenticationSettings authenticationSettings) {
    this.authenticationSettings = requireNonNull(authenticationSettings);
  }

  @Override
  public ClientRegistration findByRegistrationId(String registrationId) {
    return runAsSystem(
        () -> {
          OidcClient oidcClient = findOidcClient(registrationId);
          return oidcClient != null ? toClientRegistration(oidcClient) : null;
        });
  }

  private @Nullable @CheckForNull OidcClient findOidcClient(String registrationId) {
    return stream(authenticationSettings.getOidcClients())
        .filter(oidcClient -> oidcClient.getRegistrationId().equals(registrationId))
        .findFirst()
        .orElse(null);
  }

  private ClientRegistration toClientRegistration(OidcClient oidcClient) {
    return ClientRegistration.withRegistrationId(oidcClient.getRegistrationId())
        .authorizationGrantType(toAuthorizationGrantType(oidcClient))
        .authorizationUri(oidcClient.getAuthorizationUri())
        .clientAuthenticationMethod(toClientAuthenticationMethod(oidcClient))
        .clientId(oidcClient.getClientId())
        .clientName(oidcClient.getClientName())
        .clientSecret(oidcClient.getClientSecret())
        .jwkSetUri(oidcClient.getJwkSetUri())
        .redirectUriTemplate(DEFAULT_REDIRECT_URI_TEMPLATE)
        .scope(oidcClient.getScopes())
        .tokenUri(oidcClient.getTokenUri())
        .userInfoUri(oidcClient.getUserInfoUri())
        .userNameAttributeName(oidcClient.getUsernameAttributeName())
        .build();
  }

  private ClientAuthenticationMethod toClientAuthenticationMethod(OidcClient oidcClient) {
    ClientAuthenticationMethod clientAuthenticationMethod;
    String oidcClientClientAuthenticationMethod = oidcClient.getClientAuthenticationMethod();
    switch (oidcClientClientAuthenticationMethod) {
      case "basic":
        clientAuthenticationMethod = ClientAuthenticationMethod.BASIC;
        break;
      case "post":
        clientAuthenticationMethod = ClientAuthenticationMethod.POST;
        break;
      default:
        clientAuthenticationMethod =
            new ClientAuthenticationMethod(oidcClientClientAuthenticationMethod);
        break;
    }
    return clientAuthenticationMethod;
  }

  private AuthorizationGrantType toAuthorizationGrantType(OidcClient oidcClient) {
    AuthorizationGrantType authorizationGrantType;
    String oidcClientAuthorizationGrantType = oidcClient.getAuthorizationGrantType();
    switch (oidcClientAuthorizationGrantType) {
      case "authorization_code":
        authorizationGrantType = AuthorizationGrantType.AUTHORIZATION_CODE;
        break;
      case "implicit":
        authorizationGrantType = AuthorizationGrantType.IMPLICIT;
        break;
      default:
        authorizationGrantType = new AuthorizationGrantType(oidcClientAuthorizationGrantType);
        break;
    }
    return authorizationGrantType;
  }
}
