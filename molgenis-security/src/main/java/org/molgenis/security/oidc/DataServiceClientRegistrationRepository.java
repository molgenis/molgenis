package org.molgenis.security.oidc;

import static com.google.api.client.util.Maps.newHashMap;
import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.security.oidc.model.OidcClientMetadata.CLAIMS_ROLE_PATH;
import static org.molgenis.security.oidc.model.OidcClientMetadata.CLAIMS_VOGROUP_PATH;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_POST;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.NONE;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.SUB;

import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.settings.AuthenticationSettings;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
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
              .scope(oidcClient.getScopes())
              .registrationId(oidcClient.getRegistrationId());
    } else {
      result =
          ClientRegistration.withRegistrationId(oidcClient.getRegistrationId())
              .authorizationUri(oidcClient.getAuthorizationUri())
              .tokenUri(oidcClient.getTokenUri())
              .jwkSetUri(oidcClient.getJwkSetUri())
              .userInfoUri(oidcClient.getUserInfoUri())
              .redirectUri(DEFAULT_REDIRECT_URI_TEMPLATE)
              .clientAuthenticationMethod(toClientAuthenticationMethod(oidcClient))
              .authorizationGrantType(AUTHORIZATION_CODE)
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
    return Stream.of(CLIENT_SECRET_BASIC, CLIENT_SECRET_POST, NONE)
        .filter(it -> it.getValue().equals(oidcClient.getClientAuthenticationMethod()))
        .findFirst()
        .orElseThrow();
  }
}
