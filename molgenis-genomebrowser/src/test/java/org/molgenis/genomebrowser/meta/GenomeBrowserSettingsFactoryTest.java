package org.molgenis.genomebrowser.meta;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
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
public class GenomeBrowserSettingsFactoryTest extends AbstractEntityFactoryTest {

  @Autowired GenomeBrowserSettingsFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, GenomeBrowserSettings.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, GenomeBrowserSettings.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, GenomeBrowserSettings.class);
  }
}
