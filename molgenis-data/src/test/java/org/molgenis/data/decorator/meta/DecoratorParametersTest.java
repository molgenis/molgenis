package org.molgenis.data.decorator.meta;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
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
public class DecoratorParametersTest extends AbstractSystemEntityTest {

  @Autowired DecoratorParametersMetadata metadata;
  @Autowired DecoratorParametersFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata,
        DecoratorParameters.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
