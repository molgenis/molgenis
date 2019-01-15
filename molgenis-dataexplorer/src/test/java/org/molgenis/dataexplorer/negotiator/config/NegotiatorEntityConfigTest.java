package org.molgenis.dataexplorer.negotiator.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

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

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata,
        NegotiatorEntityConfig.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
