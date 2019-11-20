package org.molgenis.api.metadata.v3.job;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      MetadataUpsertJobExecutionMetadata.class,
      MetadataUpsertJobExecutionFactory.class,
      JobExecutionMetaData.class,
      JobPackage.class
    })
public class MetadataUpsertJobExecutionFactoryTest extends AbstractEntityFactoryTest {

  @Autowired MetadataUpsertJobExecutionFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, MetadataUpsertJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, MetadataUpsertJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, MetadataUpsertJobExecution.class);
  }
}
