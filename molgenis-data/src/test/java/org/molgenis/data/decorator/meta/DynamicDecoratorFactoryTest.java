package org.molgenis.data.decorator.meta;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      DynamicDecoratorMetadata.class,
      DynamicDecoratorFactory.class,
      DecoratorPackage.class
    })
public class DynamicDecoratorFactoryTest extends AbstractEntityFactoryTest {

  @Autowired DynamicDecoratorFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, DynamicDecorator.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, DynamicDecorator.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, DynamicDecorator.class);
  }
}
