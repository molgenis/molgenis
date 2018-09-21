package org.molgenis.security.oidc.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class OidcUserMappingFactory
    extends AbstractSystemEntityFactory<OidcUserMapping, OidcUserMappingMetadata, String> {
  OidcUserMappingFactory(
      OidcUserMappingMetadata oidcUserMappingMetadata, EntityPopulator entityPopulator) {
    super(OidcUserMapping.class, oidcUserMappingMetadata, entityPopulator);
  }
}
