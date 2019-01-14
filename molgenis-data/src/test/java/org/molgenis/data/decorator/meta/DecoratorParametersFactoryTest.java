package org.molgenis.data.decorator.meta;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      DecoratorParametersMetadata.class,
      DecoratorParametersFactory.class,
      DynamicDecoratorMetadata.class,
      DecoratorPackage.class
    })
public class DecoratorParametersFactoryTest extends AbstractEntityFactoryTest {

  @Autowired DecoratorParametersFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, DecoratorParameters.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, DecoratorParameters.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, DecoratorParameters.class);
  }
}
