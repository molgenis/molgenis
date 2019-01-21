package org.molgenis.dataexplorer.negotiator.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

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

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, NegotiatorConfig.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
