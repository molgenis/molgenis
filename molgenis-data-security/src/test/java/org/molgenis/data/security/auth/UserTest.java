package org.molgenis.data.security.auth;

import org.junit.jupiter.api.Test;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.data.security.config.SecurityTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {SecurityTestConfig.class})
public class UserTest extends AbstractSystemEntityTest {

  @Autowired UserMetadata metadata;
  @Autowired UserFactory factory;

  @SuppressWarnings("squid:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, User.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
