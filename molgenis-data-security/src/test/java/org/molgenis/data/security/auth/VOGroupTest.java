package org.molgenis.data.security.auth;

import org.junit.jupiter.api.Test;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.data.security.config.SecurityTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {SecurityTestConfig.class})
class VOGroupTest extends AbstractSystemEntityTest {

  @Autowired VOGroupMetadata metadata;
  @Autowired VOGroupFactory factory;

  @Test
  @SuppressWarnings({"java:S5786", "java:S2699"})
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, VOGroup.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
