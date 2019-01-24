package org.molgenis.data.security.auth;

import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.data.security.config.SecurityTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {SecurityTestConfig.class})
public class RoleMembershipFactoryTest extends AbstractEntityFactoryTest {

  @Autowired RoleMembershipFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, RoleMembership.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, RoleMembership.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, RoleMembership.class);
  }
}
