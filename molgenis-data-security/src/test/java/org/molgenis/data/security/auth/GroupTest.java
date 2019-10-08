package org.molgenis.data.security.auth;

import org.junit.jupiter.api.Test;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.data.security.config.SecurityTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {SecurityTestConfig.class})
class GroupTest extends AbstractSystemEntityTest {

  @Autowired GroupMetadata metadata;
  @Autowired GroupFactory factory;

  @SuppressWarnings("squid:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, Group.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
