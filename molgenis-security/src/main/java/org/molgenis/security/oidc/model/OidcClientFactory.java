package org.molgenis.security.oidc.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class OidcClientFactory
    extends AbstractSystemEntityFactory<OidcClient, OidcClientMetadata, String> {
  OidcClientFactory(OidcClientMetadata oidcClientMetadata, EntityPopulator entityPopulator) {
    super(OidcClient.class, oidcClientMetadata, entityPopulator);
  }
}
