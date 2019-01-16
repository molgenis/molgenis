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
      AttributeMetadata.class,
      AttributeFactory.class,
      MetadataTestConfig.class
    })
public class AttributeFactoryTest extends AbstractEntityFactoryTest {

  @Autowired AttributeFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, Attribute.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, Attribute.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, Attribute.class);
  }
}
