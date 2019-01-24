package org.molgenis.data.meta.model;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.config.MetadataTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      TagMetadata.class,
      TagFactory.class,
      MetadataTestConfig.class
    })
public class TagFactoryTest extends AbstractEntityFactoryTest {

  @Autowired TagFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, Tag.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, Tag.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, Tag.class);
  }
}
