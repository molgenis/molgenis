package org.molgenis.navigator.delete.job;

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
      ResourceDeleteJobExecutionMetadata.class,
      ResourceDeleteJobExecutionFactory.class,
      JobPackage.class,
      JobTestConfig.class
    })
public class ResourceDeleteJobExecutionFactoryTest extends AbstractEntityFactoryTest {

  @Autowired ResourceDeleteJobExecutionFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, ResourceDeleteJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, ResourceDeleteJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, ResourceDeleteJobExecution.class);
  }
}
