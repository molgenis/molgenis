package org.molgenis.dataexplorer.negotiator.config;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      NegotiatorEntityConfigMetadata.class,
      NegotiatorEntityConfigFactory.class,
      NegotiatorConfigMetadata.class,
      NegotiatorPackage.class
    })
public class NegotiatorEntityConfigTest extends AbstractSystemEntityTest {

  @Autowired NegotiatorEntityConfigMetadata metadata;
  @Autowired NegotiatorEntityConfigFactory factory;

  @SuppressWarnings("java:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata,
        NegotiatorEntityConfig.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
