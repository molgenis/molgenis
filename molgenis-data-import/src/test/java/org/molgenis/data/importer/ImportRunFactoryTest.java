package org.molgenis.data.importer;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.importer.config.ImportTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      ImportRunMetadata.class,
      ImportRunFactory.class,
      ImportTestConfig.class
    })
public class ImportRunFactoryTest extends AbstractEntityFactoryTest {

  @Autowired ImportRunFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, ImportRun.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, ImportRun.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, ImportRun.class);
  }
}
