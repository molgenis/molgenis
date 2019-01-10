package org.molgenis.amazon.bucket.meta;

import org.molgenis.amazon.bucket.config.AmazonBucketTestConfig;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      AmazonBucketJobExecutionMetadata.class,
      AmazonBucketJobExecutionFactory.class,
      JobPackage.class,
      AmazonBucketTestConfig.class
    })
public class AmazonBucketJobExecutionFactoryTest extends AbstractEntityFactoryTest {

  @Autowired AmazonBucketJobExecutionFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, AmazonBucketJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, AmazonBucketJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, AmazonBucketJobExecution.class);
  }
}
