package org.molgenis.navigator.download.job;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.jobs.config.JobTestConfig;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      ResourceDownloadJobExecutionMetadata.class,
      ResourceDownloadJobExecutionFactory.class,
      JobPackage.class,
      JobTestConfig.class
    })
public class ResourceDownloadJobExecutionFactoryTest extends AbstractEntityFactoryTest {

  @Autowired ResourceDownloadJobExecutionFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, ResourceDownloadJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, ResourceDownloadJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, ResourceDownloadJobExecution.class);
  }
}
