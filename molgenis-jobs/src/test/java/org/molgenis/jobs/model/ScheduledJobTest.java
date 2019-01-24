package org.molgenis.jobs.model;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
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
public class ScheduledJobTest extends AbstractSystemEntityTest {

  @Autowired ScheduledJobMetadata metadata;
  @Autowired ScheduledJobFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, ScheduledJob.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
