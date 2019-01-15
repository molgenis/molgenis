package org.molgenis.data.index.job;

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
      IndexJobExecutionMetadata.class,
      IndexJobExecutionFactory.class,
      JobExecutionMetaData.class,
      JobPackage.class
    })
public class IndexJobExecutionFactoryTest extends AbstractEntityFactoryTest {

  @Autowired IndexJobExecutionFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, IndexJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, IndexJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, IndexJobExecution.class);
  }
}
