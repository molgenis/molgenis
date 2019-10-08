package org.molgenis.security.oidc.model;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      OidcUserMappingMetadata.class,
      OidcUserMappingFactory.class,
      OidcTestConfig.class,
      OidcPackage.class
    })
public class OidcUserMappingTest extends AbstractSystemEntityTest {

  @Autowired OidcUserMappingMetadata metadata;
  @Autowired OidcUserMappingFactory factory;

  @SuppressWarnings("squid:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, OidcUserMapping.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
