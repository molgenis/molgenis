package org.molgenis.data.security.auth;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.data.security.config.SecurityTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      PasswordResetTokenMetadata.class,
      PasswordResetTokenFactory.class,
      SecurityPackage.class,
      SecurityTestConfig.class
    })
public class PasswordResetTokenTest extends AbstractSystemEntityTest {

  @Autowired PasswordResetTokenMetadata metadata;
  @Autowired PasswordResetTokenFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata,
        PasswordResetToken.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
