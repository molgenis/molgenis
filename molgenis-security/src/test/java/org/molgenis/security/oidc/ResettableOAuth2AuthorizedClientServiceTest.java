package org.molgenis.security.oidc;

import static java.util.Collections.emptyMap;
import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResettableOAuth2AuthorizedClientServiceTest extends AbstractMockitoTest {
  @Mock private ClientRegistrationRepository clientRegistrationRepository;
  private ResettableOAuth2AuthorizedClientService oAuth2AuthorizedClientService;

  @BeforeMethod
  public void setUpBeforeMethod() {
    oAuth2AuthorizedClientService =
        new ResettableOAuth2AuthorizedClientService(clientRegistrationRepository);
  }

  @Test
  public void testReset() {
    oAuth2AuthorizedClientService.reset();
    assertEquals(oAuth2AuthorizedClientService.getAuthorizedClients(), emptyMap());
  }
}
