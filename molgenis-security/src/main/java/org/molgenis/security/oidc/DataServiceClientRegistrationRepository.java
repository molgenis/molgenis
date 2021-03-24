package org.molgenis.security.oidc;

import static com.google.api.client.util.Maps.newHashMap;
import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.security.oidc.model.OidcClientMetadata.CLAIMS_ROLE_PATH;
import static org.molgenis.security.oidc.model.OidcClientMetadata.CLAIMS_VOGROUP_PATH;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.SUB;

import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.settings.AuthenticationSettings;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.stereotype.Component;

@Component
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
    Map<String, Object> configMetadata = newHashMap();
    oidcClient.getClaimsRolePath().ifPresent(path -> configMetadata.put(CLAIMS_ROLE_PATH, path));
    oidcClient
        .getClaimsVOGroupPath()
        .ifPresent(path -> configMetadata.put(CLAIMS_VOGROUP_PATH, path));
    ClientRegistration.Builder result;
    if (oidcClient.getIssuerUri() != null) {
      result =
          ClientRegistrations.fromOidcIssuerLocation(oidcClient.getIssuerUri())
              .registrationId(oidcClient.getRegistrationId());
    } else {
      result =
          ClientRegistration.withRegistrationId(oidcClient.getRegistrationId())
              .authorizationUri(oidcClient.getAuthorizationUri())
              .tokenUri(oidcClient.getTokenUri())
              .jwkSetUri(oidcClient.getJwkSetUri())
              .userInfoUri(oidcClient.getUserInfoUri())
              .redirectUriTemplate(DEFAULT_REDIRECT_URI_TEMPLATE)
              .clientAuthenticationMethod(toClientAuthenticationMethod(oidcClient))
              .authorizationGrantType(toAuthorizationGrantType(oidcClient))
              .scope(oidcClient.getScopes());
    }
    return result
        .clientId(oidcClient.getClientId())
        .clientName(oidcClient.getClientName())
        .clientSecret(oidcClient.getClientSecret())
        .userNameAttributeName(SUB)
        .providerConfigurationMetadata(configMetadata)
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
