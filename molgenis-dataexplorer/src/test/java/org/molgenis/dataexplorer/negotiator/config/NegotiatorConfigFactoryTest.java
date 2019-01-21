package org.molgenis.dataexplorer.negotiator.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
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
public class NegotiatorConfigFactoryTest extends AbstractEntityFactoryTest {

  @Autowired NegotiatorConfigFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, NegotiatorConfig.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, NegotiatorConfig.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, NegotiatorConfig.class);
  }
}
