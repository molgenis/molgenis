package org.molgenis.semanticmapper.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
      MappingJobExecutionMetadata.class,
      MappingJobExecutionFactory.class,
      JobExecutionMetaData.class,
      JobPackage.class
    })
public class MappingJobExecutionTest extends AbstractSystemEntityTest {

  @Autowired MappingJobExecutionMetadata metadata;
  @Autowired MappingJobExecutionFactory factory;

  @Override
  protected Map<String, Pair<Class, Object>> getOverriddenReturnTypes() {
    Map<String, Pair<Class, Object>> map = new HashMap<>();

    Pair<Class, Object> status = new Pair<>();
    status.setA(Status.class);
    status.setB(Status.RUNNING);
    map.put(JobExecutionMetaData.STATUS, status);

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
        MappingJobExecution.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
