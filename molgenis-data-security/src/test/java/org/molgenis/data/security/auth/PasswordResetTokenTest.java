package org.molgenis.data.security.auth;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.data.security.config.SecurityTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

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
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata,
        PasswordResetToken.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
