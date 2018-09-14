package org.molgenis.security.oidc.model;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.molgenis.security.oidc.ResettableOAuth2AuthorizedClientService;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
public class OidcClientRepositoryDecoratorFactory
    extends AbstractSystemRepositoryDecoratorFactory<OidcClient, OidcClientMetadata> {

  private final ResettableOAuth2AuthorizedClientService oAuth2AuthorizedClientService;

  public OidcClientRepositoryDecoratorFactory(
      OidcClientMetadata oidcClientMetadata,
      ResettableOAuth2AuthorizedClientService oAuth2AuthorizedClientService) {
    super(oidcClientMetadata);
    this.oAuth2AuthorizedClientService = requireNonNull(oAuth2AuthorizedClientService);
  }

  @Override
  public Repository<OidcClient> createDecoratedRepository(Repository<OidcClient> repository) {
    return new OidcClientRepositoryDecorator(repository, oAuth2AuthorizedClientService);
  }
}
