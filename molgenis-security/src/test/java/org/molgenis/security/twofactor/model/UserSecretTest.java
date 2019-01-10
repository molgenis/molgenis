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
      UserSecretMetadata.class,
      UserSecretFactory.class,
      SecurityPackage.class
    })
public class UserSecretTest extends AbstractSystemEntityTest {

  @Autowired UserSecretMetadata metadata;
  @Autowired UserSecretFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, UserSecret.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
