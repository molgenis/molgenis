package org.molgenis.security.twofactor.model;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.data.security.auth.SecurityPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

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

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, RecoveryCode.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
