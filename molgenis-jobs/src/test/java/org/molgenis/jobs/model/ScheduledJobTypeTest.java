package org.molgenis.jobs.model;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
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
public class ScheduledJobTypeTest extends AbstractSystemEntityTest {

  @Autowired ScheduledJobTypeMetadata metadata;
  @Autowired ScheduledJobTypeFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, ScheduledJobType.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
