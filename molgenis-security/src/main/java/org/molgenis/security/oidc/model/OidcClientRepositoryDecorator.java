package org.molgenis.security.oidc.model;

import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.security.oidc.ResettableOAuth2AuthorizedClientService;

/** {@link OidcClient} decorator that resets cached authorized clients on configuration change. */
class OidcClientRepositoryDecorator extends AbstractRepositoryDecorator<OidcClient> {
  private final ResettableOAuth2AuthorizedClientService oAuth2AuthorizedClientService;

  OidcClientRepositoryDecorator(
      Repository<OidcClient> delegateRepository,
      ResettableOAuth2AuthorizedClientService oAuth2AuthorizedClientService) {
    super(delegateRepository);
    this.oAuth2AuthorizedClientService = requireNonNull(oAuth2AuthorizedClientService);
  }

  @Override
  public void update(OidcClient entity) {
    super.update(entity);
    oAuth2AuthorizedClientService.reset();
  }

  @Override
  public void update(Stream<OidcClient> entities) {
    super.update(entities);
    oAuth2AuthorizedClientService.reset();
  }

  @Override
  public void delete(OidcClient entity) {
    super.delete(entity);
    oAuth2AuthorizedClientService.reset();
  }

  @Override
  public void deleteById(Object id) {
    super.deleteById(id);
    oAuth2AuthorizedClientService.reset();
  }

  @Override
  public void deleteAll() {
    super.deleteAll();
    oAuth2AuthorizedClientService.reset();
  }

  @Override
  public void delete(Stream<OidcClient> entities) {
    super.delete(entities);
    oAuth2AuthorizedClientService.reset();
  }

  @Override
  public void deleteAll(Stream<Object> ids) {
    super.deleteAll(ids);
    oAuth2AuthorizedClientService.reset();
  }
}
