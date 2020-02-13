package org.molgenis.data.decorator.meta;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      DynamicDecoratorMetadata.class,
      DynamicDecoratorFactory.class,
      DecoratorPackage.class
    })
public class DynamicDecoratorTest extends AbstractSystemEntityTest {

  @Autowired DynamicDecoratorMetadata metadata;
  @Autowired DynamicDecoratorFactory factory;

  @SuppressWarnings("java:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, DynamicDecorator.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
