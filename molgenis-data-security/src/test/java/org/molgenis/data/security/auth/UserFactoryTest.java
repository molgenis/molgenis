package org.molgenis.data.security.auth;

import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.data.security.config.SecurityTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {SecurityTestConfig.class})
public class UserFactoryTest extends AbstractEntityFactoryTest {

  @Autowired UserFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, User.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, User.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, User.class);
  }
}
