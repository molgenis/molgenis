package org.molgenis.genomebrowser.meta;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      GenomeBrowserSettingsMetadata.class,
      GenomeBrowserSettingsFactory.class,
      GenomeBrowserPackage.class,
      GenomeBrowserAttributesMetadata.class
    })
public class GenomeBrowserSettingsTest extends AbstractSystemEntityTest {

  @Autowired GenomeBrowserSettingsMetadata metadata;
  @Autowired GenomeBrowserSettingsFactory factory;

  @SuppressWarnings("java:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata,
        GenomeBrowserSettings.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
