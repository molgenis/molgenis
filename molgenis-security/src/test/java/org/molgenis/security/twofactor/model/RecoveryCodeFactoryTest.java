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
      RecoveryCodeMetadata.class,
      RecoveryCodeFactory.class,
      SecurityPackage.class
    })
public class RecoveryCodeFactoryTest extends AbstractEntityFactoryTest {

  @Autowired RecoveryCodeFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, RecoveryCode.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, RecoveryCode.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, RecoveryCode.class);
  }
}
