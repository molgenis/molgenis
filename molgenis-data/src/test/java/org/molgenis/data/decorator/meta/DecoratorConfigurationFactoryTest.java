package org.molgenis.data.decorator.meta;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      DecoratorConfigurationMetadata.class,
      DecoratorConfigurationFactory.class,
      DecoratorParametersMetadata.class,
      DynamicDecoratorMetadata.class,
      DecoratorPackage.class
    })
public class DecoratorConfigurationFactoryTest extends AbstractEntityFactoryTest {

  @Autowired DecoratorConfigurationFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, DecoratorConfiguration.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, DecoratorConfiguration.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, DecoratorConfiguration.class);
  }
}
