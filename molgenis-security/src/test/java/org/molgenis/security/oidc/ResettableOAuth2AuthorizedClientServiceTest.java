package org.molgenis.security.oidc;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

class ResettableOAuth2AuthorizedClientServiceTest extends AbstractMockitoTest {
  @Mock private ClientRegistrationRepository clientRegistrationRepository;
  private ResettableOAuth2AuthorizedClientService oAuth2AuthorizedClientService;

  @BeforeEach
  void setUpBeforeMethod() {
    oAuth2AuthorizedClientService =
        new ResettableOAuth2AuthorizedClientService(clientRegistrationRepository);
  }

  @Test
  void testReset() {
    oAuth2AuthorizedClientService.reset();
    assertEquals(oAuth2AuthorizedClientService.getAuthorizedClients(), emptyMap());
  }
}
