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
      EntityTypeMetadata.class,
      EntityTypeFactory.class,
      MetadataTestConfig.class
    })
public class EntityTypeFactoryTest extends AbstractEntityFactoryTest {

  @Autowired EntityTypeFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, EntityType.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, EntityType.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, EntityType.class);
  }
}
