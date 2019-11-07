package org.molgenis.api.metadata.v3.model;

import org.junit.jupiter.api.Test;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecutionFactory;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecutionMetadata;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      MetadataDeleteJobExecutionMetadata.class,
      MetadataDeleteJobExecutionFactory.class,
      JobExecutionMetaData.class,
      JobPackage.class
    })
public class MetadataDeleteJobExecutionFactoryTest extends AbstractEntityFactoryTest {

  @Autowired MetadataDeleteJobExecutionFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, MetadataDeleteJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, MetadataDeleteJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, MetadataDeleteJobExecution.class);
  }
}
