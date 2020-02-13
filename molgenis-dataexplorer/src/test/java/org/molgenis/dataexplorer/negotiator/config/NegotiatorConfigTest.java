package org.molgenis.dataexplorer.negotiator.config;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      NegotiatorConfigMetadata.class,
      NegotiatorConfigFactory.class,
      NegotiatorPackage.class
    })
public class NegotiatorConfigTest extends AbstractSystemEntityTest {

  @Autowired NegotiatorConfigMetadata metadata;
  @Autowired NegotiatorConfigFactory factory;

  @SuppressWarnings("java:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, NegotiatorConfig.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
