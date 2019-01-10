package org.molgenis.data.security.auth;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      UserMetadata.class,
      UserFactory.class,
      SecurityPackage.class
    })
public class UserTest extends AbstractSystemEntityTest {

  @Autowired UserMetadata metadata;
  @Autowired UserFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, User.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
