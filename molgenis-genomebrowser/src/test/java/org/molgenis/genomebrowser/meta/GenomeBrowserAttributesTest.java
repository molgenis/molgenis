package org.molgenis.genomebrowser.meta;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      GenomeBrowserAttributesMetadata.class,
      GenomeBrowserAttributesFactory.class,
      GenomeBrowserPackage.class
    })
public class GenomeBrowserAttributesTest extends AbstractSystemEntityTest {

  @Autowired GenomeBrowserAttributesMetadata metadata;
  @Autowired GenomeBrowserAttributesFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata,
        GenomeBrowserAttributes.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
