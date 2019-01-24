package org.molgenis.data.plugin.model;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {EntityBaseTestConfig.class, PluginMetadata.class, PluginFactory.class})
public class PluginTest extends AbstractSystemEntityTest {

  @Autowired PluginMetadata metadata;
  @Autowired PluginFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, Plugin.class, factory, getOverriddenReturnTypes(), getExcludedAttrs(), true);
  }
}
