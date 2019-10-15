package org.molgenis.data.decorator.meta;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

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

  @SuppressWarnings("squid:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata,
        DecoratorConfiguration.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
