package org.molgenis.data.decorator.meta;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

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

  @SuppressWarnings("squid:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata,
        DecoratorParameters.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
