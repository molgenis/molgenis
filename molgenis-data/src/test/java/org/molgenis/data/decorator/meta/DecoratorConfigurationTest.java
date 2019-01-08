package org.molgenis.data.decorator.meta;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
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
public class DecoratorConfigurationTest extends AbstractSystemEntityTest {

  @Autowired DecoratorConfigurationMetadata metadata;
  @Autowired DecoratorConfigurationFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata,
        DecoratorConfiguration.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
