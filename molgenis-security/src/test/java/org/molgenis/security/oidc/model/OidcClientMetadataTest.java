package org.molgenis.security.oidc.model;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.data.security.auth.SecurityPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      OidcClientMetadata.class,
      OidcClientFactory.class,
      OidcPackage.class,
      SecurityPackage.class
    })
class OidcClientMetadataTest extends AbstractSystemEntityTest {
  @Autowired OidcClientMetadata metadata;
  @Autowired OidcClientFactory factory;

  @SuppressWarnings({"java:S2699", "java:S5786"})
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, OidcClient.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }

  protected List<String> getExcludedAttrs() {
    return List.of("scopes");
  }
}
