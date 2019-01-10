package org.molgenis.data.security.auth;

import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.data.security.config.SecurityTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {SecurityTestConfig.class})
public class RoleTest extends AbstractSystemEntityTest {

  @Autowired RoleMetadata metadata;
  @Autowired RoleFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, Role.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
