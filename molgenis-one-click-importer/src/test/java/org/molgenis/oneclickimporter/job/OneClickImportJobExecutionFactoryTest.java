package org.molgenis.oneclickimporter.job;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      OneClickImportJobExecutionMetadata.class,
      OneClickImportJobExecutionFactory.class,
      JobExecutionMetaData.class,
      JobPackage.class
    })
public class OneClickImportJobExecutionFactoryTest extends AbstractEntityFactoryTest {

  @Autowired OneClickImportJobExecutionFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, OneClickImportJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, OneClickImportJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, OneClickImportJobExecution.class);
  }
}
