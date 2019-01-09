package org.molgenis.ontology.sorta.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.jobs.model.JobExecution.Status;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.molgenis.ontology.sorta.meta.SortaJobExecutionMetadata;
import org.molgenis.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      SortaJobExecutionMetadata.class,
      SortaJobExecutionFactory.class,
      JobExecutionMetaData.class,
      JobPackage.class
    })
public class SortaJobExecutionTest extends AbstractSystemEntityTest {

  @Autowired SortaJobExecutionMetadata metadata;
  @Autowired SortaJobExecutionFactory factory;

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
        metadata, SortaJobExecution.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
