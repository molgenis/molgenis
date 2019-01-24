package org.molgenis.navigator.copy.job;

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
      ResourceCopyJobExecutionMetadata.class,
      ResourceCopyJobExecutionFactory.class,
      JobPackage.class,
      JobTestConfig.class
    })
public class ResourceCopyJobExecutionFactoryTest extends AbstractEntityFactoryTest {

  @Autowired ResourceCopyJobExecutionFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, ResourceCopyJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, ResourceCopyJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, ResourceCopyJobExecution.class);
  }
}
