package org.molgenis.data.security.auth;

import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.data.security.config.SecurityTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {SecurityTestConfig.class})
public class MembershipInvitationFactoryTest extends AbstractEntityFactoryTest {

  @Autowired MembershipInvitationFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, MembershipInvitation.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, MembershipInvitation.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, MembershipInvitation.class);
  }
}
