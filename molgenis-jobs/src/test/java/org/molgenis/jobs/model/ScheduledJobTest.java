package org.molgenis.jobs.model;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

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

  @SuppressWarnings("java:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, ScheduledJob.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
