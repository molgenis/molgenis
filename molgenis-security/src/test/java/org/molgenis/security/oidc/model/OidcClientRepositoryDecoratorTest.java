package org.molgenis.security.oidc.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.stream.Stream;
import org.mockito.Mock;
import org.molgenis.data.Repository;
import org.molgenis.security.oidc.ResettableOAuth2AuthorizedClientService;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OidcClientRepositoryDecoratorTest extends AbstractMockitoTest {
  @Mock private Repository<OidcClient> delegateRepository;
  @Mock private ResettableOAuth2AuthorizedClientService oAuth2AuthorizedClientService;

  private OidcClientRepositoryDecorator oidcClientRepositoryDecorator;

  @BeforeMethod
  public void setUpBeforeMethod() {
    this.oidcClientRepositoryDecorator =
        new OidcClientRepositoryDecorator(delegateRepository, oAuth2AuthorizedClientService);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testOidcClientRepositoryDecorator() {
    new OidcClientRepositoryDecorator(null, null);
  }

  @Test
  public void testUpdate() {
    OidcClient oidcClient = mock(OidcClient.class);
    oidcClientRepositoryDecorator.update(oidcClient);
    verify(delegateRepository).update(oidcClient);
    verify(oAuth2AuthorizedClientService).reset();
  }

  @Test
  public void testUpdateStream() {
    @SuppressWarnings("unchecked")
    Stream<OidcClient> oidcClientStream = mock(Stream.class);
    oidcClientRepositoryDecorator.update(oidcClientStream);
    verify(delegateRepository).update(oidcClientStream);
    verify(oAuth2AuthorizedClientService).reset();
  }

  @Test
  public void testDelete() {
    OidcClient oidcClient = mock(OidcClient.class);
    oidcClientRepositoryDecorator.delete(oidcClient);
    verify(delegateRepository).delete(oidcClient);
    verify(oAuth2AuthorizedClientService).reset();
  }

  @Test
  public void testDeleteById() {
    String oidcClientId = "oidcClientId";
    oidcClientRepositoryDecorator.deleteById(oidcClientId);
    verify(delegateRepository).deleteById(oidcClientId);
    verify(oAuth2AuthorizedClientService).reset();
  }

  @Test
  public void testDeleteAll() {
    oidcClientRepositoryDecorator.deleteAll();
    verify(delegateRepository).deleteAll();
    verify(oAuth2AuthorizedClientService).reset();
  }

  @Test
  public void testDeleteStream() {
    @SuppressWarnings("unchecked")
    Stream<OidcClient> oidcClientStream = mock(Stream.class);
    oidcClientRepositoryDecorator.delete(oidcClientStream);
    verify(delegateRepository).delete(oidcClientStream);
    verify(oAuth2AuthorizedClientService).reset();
  }

  @Test
  public void testDeleteAllStream() {
    @SuppressWarnings("unchecked")
    Stream<Object> oidcClientIdStream = mock(Stream.class);
    oidcClientRepositoryDecorator.deleteAll(oidcClientIdStream);
    verify(delegateRepository).deleteAll(oidcClientIdStream);
    verify(oAuth2AuthorizedClientService).reset();
  }
}
