package org.molgenis.jobs.model;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      ScheduledJobMetadata.class,
      ScheduledJobFactory.class,
      ScheduledJobTypeMetadata.class,
      JobPackage.class
    })
public class ScheduledJobFactoryTest extends AbstractEntityFactoryTest {

  @Autowired ScheduledJobFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, ScheduledJob.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, ScheduledJob.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, ScheduledJob.class);
  }
}
