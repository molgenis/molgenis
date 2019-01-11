package org.molgenis.file.ingest.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.file.ingest.config.FileIngestTestConfig;
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
      FileIngestJobExecutionMetadata.class,
      FileIngestJobExecutionFactory.class,
      JobPackage.class,
      FileIngestTestConfig.class
    })
public class FileIngestJobExecutionTest extends AbstractSystemEntityTest {

  @Autowired FileIngestJobExecutionMetadata metadata;
  @Autowired FileIngestJobExecutionFactory factory;

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
        FileIngestJobExecution.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
