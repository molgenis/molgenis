package org.molgenis.api.metadata.v3.model;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecutionFactory;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecutionMetadata;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecutionMetadata.DeleteType;
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
      MetadataDeleteJobExecutionMetadata.class,
      MetadataDeleteJobExecutionFactory.class,
      JobExecutionMetaData.class,
      JobPackage.class
    })
public class MetadataDeleteJobExecutionTest extends AbstractSystemEntityTest {

  @Autowired MetadataDeleteJobExecutionMetadata metadata;
  @Autowired MetadataDeleteJobExecutionFactory factory;

  @Override
  protected Map<String, Pair<Class, Object>> getOverriddenReturnTypes() {
    Map<String, Pair<Class, Object>> map = new HashMap<>();

    Pair<Class, Object> entityTypeIds = new Pair<>();
    entityTypeIds.setA(List.class);
    entityTypeIds.setB(asList("id1", "id2"));
    map.put(MetadataDeleteJobExecutionMetadata.IDS, entityTypeIds);

    Pair<Class, Object> type = new Pair<>();
    type.setA(DeleteType.class);
    type.setB(DeleteType.ATTRIBUTE);
    map.put(MetadataDeleteJobExecutionMetadata.DELETE_TYPE, type);

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
        MetadataDeleteJobExecution.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
