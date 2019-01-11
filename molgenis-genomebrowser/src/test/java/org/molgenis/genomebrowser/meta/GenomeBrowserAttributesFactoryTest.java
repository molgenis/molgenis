package org.molgenis.genomebrowser.meta;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
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
public class GenomeBrowserAttributesFactoryTest extends AbstractEntityFactoryTest {

  @Autowired GenomeBrowserAttributesFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, GenomeBrowserAttributes.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, GenomeBrowserAttributes.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, GenomeBrowserAttributes.class);
  }
}
