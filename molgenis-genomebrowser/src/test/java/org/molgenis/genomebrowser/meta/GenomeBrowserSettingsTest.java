package org.molgenis.genomebrowser.meta;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

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

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata,
        GenomeBrowserSettings.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
