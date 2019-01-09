package org.molgenis.data.index.meta;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      IndexActionGroupMetadata.class,
      IndexActionGroupFactory.class,
      IndexPackage.class
    })
public class IndexActionGroupFactoryTest extends AbstractEntityFactoryTest {

  @Autowired IndexActionGroupFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, IndexActionGroup.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, IndexActionGroup.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, IndexActionGroup.class);
  }
}
