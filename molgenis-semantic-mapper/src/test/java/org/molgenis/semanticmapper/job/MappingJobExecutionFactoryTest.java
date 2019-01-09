package org.molgenis.semanticmapper.job;

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
      MappingJobExecutionMetadata.class,
      MappingJobExecutionFactory.class,
      JobExecutionMetaData.class,
      JobPackage.class
    })
public class MappingJobExecutionFactoryTest extends AbstractEntityFactoryTest {

  @Autowired MappingJobExecutionFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, MappingJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, MappingJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, MappingJobExecution.class);
  }
}
