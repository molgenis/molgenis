package org.molgenis.api.metadata.v3.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecutionMetadata.Action;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.jobs.model.JobExecution.Status;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.molgenis.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      MetadataUpsertJobExecutionMetadata.class,
      MetadataUpsertJobExecutionFactory.class,
      JobExecutionMetaData.class,
      JobPackage.class
    })
public class MetadataUpsertJobExecutionTest extends AbstractSystemEntityTest {

  @Autowired MetadataUpsertJobExecutionMetadata metadata;
  @Autowired MetadataUpsertJobExecutionFactory factory;

  @Override
  protected Map<String, Pair<Class, Object>> getOverriddenReturnTypes() {
    Map<String, Pair<Class, Object>> map = new HashMap<>();

    Pair<Class, Object> actions = new Pair<>();
    actions.setA(Action.class);
    actions.setB(Action.CREATE);
    map.put(MetadataUpsertJobExecutionMetadata.ACTION, actions);

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

  @SuppressWarnings("squid:S2699") // Tests should include assertions
  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata,
        MetadataUpsertJobExecution.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
