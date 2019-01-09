package org.molgenis.data.index.meta;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      IndexActionMetadata.class,
      IndexActionFactory.class,
      IndexActionGroupMetadata.class,
      IndexPackage.class
    })
public class IndexActionFactoryTest extends AbstractEntityFactoryTest {

  @Autowired IndexActionFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, IndexAction.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, IndexAction.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, IndexAction.class);
  }
}
