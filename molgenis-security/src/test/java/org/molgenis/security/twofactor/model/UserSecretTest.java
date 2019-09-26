package org.molgenis.security.twofactor.model;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.data.security.auth.SecurityPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      UserSecretMetadata.class,
      UserSecretFactory.class,
      SecurityPackage.class
    })
public class UserSecretTest extends AbstractSystemEntityTest {

  @Autowired UserSecretMetadata metadata;
  @Autowired UserSecretFactory factory;

  @SuppressWarnings("squid:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, UserSecret.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
