package org.molgenis.amazon.bucket.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.molgenis.amazon.bucket.config.AmazonBucketTestConfig;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.jobs.model.JobExecution.Status;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.molgenis.util.Pair;
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
public class AmazonBucketJobExecutionTest extends AbstractSystemEntityTest {

  @Autowired AmazonBucketJobExecutionMetadata metadata;
  @Autowired AmazonBucketJobExecutionFactory factory;

  @Override
  protected Map<String, Pair<Class, Object>> getOverriddenReturnTypes() {
    Pair<Class, Object> statusPair = new Pair<>();
    statusPair.setA(Status.class);
    statusPair.setB(Status.SUCCESS);

    Map<String, Pair<Class, Object>> map = new HashMap<>();
    map.put(JobExecutionMetaData.STATUS, statusPair);
    return map;
  }

  @Override
  protected List<String> getExcludedAttrs() {
    List<String> attrs = new ArrayList<>();
    attrs.add(JobExecutionMetaData.FAILURE_EMAIL);
    attrs.add(JobExecutionMetaData.SUCCESS_EMAIL);
    return attrs;
  }

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata,
        AmazonBucketJobExecution.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
