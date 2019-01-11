package org.molgenis.settings.mail;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      JavaMailPropertyType.class,
      JavaMailPropertyFactory.class,
      MailPackage.class,
      MailTestConfig.class
    })
public class JavaMailPropertyFactoryTest extends AbstractEntityFactoryTest {

  @Autowired JavaMailPropertyFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, JavaMailProperty.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, JavaMailProperty.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, JavaMailProperty.class);
  }
}
