package org.molgenis.data.file.model;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {EntityBaseTestConfig.class, FileMetaMetadata.class, FileMetaFactory.class})
public class FileMetaFactoryTest extends AbstractEntityFactoryTest {

  @Autowired FileMetaFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, FileMeta.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, FileMeta.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, FileMeta.class);
  }
}
