package org.molgenis.security.oidc;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.molgenis.security.oidc.model.OidcClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.util.Assert;

/**
 * Similar to {@link InMemoryOAuth2AuthorizedClientService} but with the capability to reset the
 * authorized clients (e.g. on update of a {@link OidcClient}.
 */
public class ResettableOAuth2AuthorizedClientService implements OAuth2AuthorizedClientService {
  private final Map<String, OAuth2AuthorizedClient> authorizedClients = new ConcurrentHashMap<>();
  private final ClientRegistrationRepository clientRegistrationRepository;

  /**
   * Copy of {@link
   * InMemoryOAuth2AuthorizedClientService#InMemoryOAuth2AuthorizedClientService(ClientRegistrationRepository)}
   */
  public ResettableOAuth2AuthorizedClientService(
      ClientRegistrationRepository clientRegistrationRepository) {
    Assert.notNull(clientRegistrationRepository, "clientRegistrationRepository cannot be null");
    this.clientRegistrationRepository = clientRegistrationRepository;
  }

  /** Copy of {@link InMemoryOAuth2AuthorizedClientService#loadAuthorizedClient(String, String)} */
  @SuppressWarnings("unchecked")
  @Override
  public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(
      String clientRegistrationId, String principalName) {
    Assert.hasText(clientRegistrationId, "clientRegistrationId cannot be empty");
    Assert.hasText(principalName, "principalName cannot be empty");
    ClientRegistration registration =
        this.clientRegistrationRepository.findByRegistrationId(clientRegistrationId);
    if (registration == null) {
      return null;
    }
    return (T) this.authorizedClients.get(this.getIdentifier(registration, principalName));
  }

  /**
   * Copy of {@link
   * InMemoryOAuth2AuthorizedClientService#saveAuthorizedClient(OAuth2AuthorizedClient,
   * Authentication)}
   */
  @Override
  public void saveAuthorizedClient(
      OAuth2AuthorizedClient authorizedClient, Authentication principal) {
    Assert.notNull(authorizedClient, "authorizedClient cannot be null");
    Assert.notNull(principal, "principal cannot be null");
    this.authorizedClients.put(
        this.getIdentifier(authorizedClient.getClientRegistration(), principal.getName()),
        authorizedClient);
  }

  /**
   * Copy of {@link InMemoryOAuth2AuthorizedClientService#removeAuthorizedClient(String, String)}
   */
  @Override
  public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
    Assert.hasText(clientRegistrationId, "clientRegistrationId cannot be empty");
    Assert.hasText(principalName, "principalName cannot be empty");
    ClientRegistration registration =
        this.clientRegistrationRepository.findByRegistrationId(clientRegistrationId);
    if (registration != null) {
      this.authorizedClients.remove(this.getIdentifier(registration, principalName));
    }
  }

  /** Copy of {@link InMemoryOAuth2AuthorizedClientService} getIdentity */
  private String getIdentifier(ClientRegistration registration, String principalName) {
    String identifier = "[" + registration.getRegistrationId() + "][" + principalName + "]";
    return Base64.getEncoder().encodeToString(identifier.getBytes(UTF_8));
  }

  public void reset() {
    authorizedClients.clear();
  }

  /** exposed for testability */
  Map<String, OAuth2AuthorizedClient> getAuthorizedClients() {
    return authorizedClients;
  }
}
