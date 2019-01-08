package org.molgenis.data.security.auth;

import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.data.security.config.SecurityTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {SecurityTestConfig.class})
public class TokenTest extends AbstractSystemEntityTest {

  @Autowired TokenMetadata metadata;
  @Autowired TokenFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, Token.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
