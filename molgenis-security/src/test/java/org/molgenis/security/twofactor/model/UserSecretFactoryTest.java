package org.molgenis.security.twofactor.model;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.data.security.auth.SecurityPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      UserSecretMetadata.class,
      UserSecretFactory.class,
      SecurityPackage.class
    })
public class UserSecretFactoryTest extends AbstractEntityFactoryTest {

  @Autowired UserSecretFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, UserSecret.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, UserSecret.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, UserSecret.class);
  }
}
