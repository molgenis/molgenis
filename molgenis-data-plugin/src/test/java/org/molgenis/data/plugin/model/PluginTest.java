package org.molgenis.data.plugin.model;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {EntityBaseTestConfig.class, PluginMetadata.class, PluginFactory.class})
class PluginTest extends AbstractSystemEntityTest {

  @Autowired PluginMetadata metadata;
  @Autowired PluginFactory factory;

  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, Plugin.class, factory, getOverriddenReturnTypes(), getExcludedAttrs(), true);
  }
}
