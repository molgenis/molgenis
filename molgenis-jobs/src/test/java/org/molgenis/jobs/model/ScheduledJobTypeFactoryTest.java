package org.molgenis.jobs.model;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      ScheduledJobTypeMetadata.class,
      ScheduledJobTypeFactory.class,
      JobPackage.class
    })
public class ScheduledJobTypeFactoryTest extends AbstractEntityFactoryTest {

  @Autowired ScheduledJobTypeFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, ScheduledJobType.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, ScheduledJobType.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, ScheduledJobType.class);
  }
}
