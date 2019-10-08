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
      RecoveryCodeMetadata.class,
      RecoveryCodeFactory.class,
      SecurityPackage.class
    })
public class RecoveryCodeTest extends AbstractSystemEntityTest {

  @Autowired RecoveryCodeMetadata metadata;
  @Autowired RecoveryCodeFactory factory;

  @SuppressWarnings("squid:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, RecoveryCode.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
