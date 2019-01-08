package org.molgenis.data.security.auth;

import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.data.security.config.SecurityTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {SecurityTestConfig.class})
public class TokenFactoryTest extends AbstractEntityFactoryTest {

  @Autowired TokenFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, Token.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, Token.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, Token.class);
  }
}
