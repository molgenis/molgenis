package org.molgenis.data.plugin.model;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {EntityBaseTestConfig.class, PluginMetadata.class, PluginFactory.class})
public class PluginFactoryTest extends AbstractEntityFactoryTest {

  @Autowired PluginFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, Plugin.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, Plugin.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, Plugin.class);
  }
}
