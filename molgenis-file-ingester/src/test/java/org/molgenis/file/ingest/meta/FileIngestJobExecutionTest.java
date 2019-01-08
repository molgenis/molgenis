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

// NOTE: when the following exception occurs;
// java.lang.ArrayStoreException: sun.reflect.annotation.TypeNotPresentExceptionProxy,
// this means you are missing this dependency:
// <dependency>
//  <groupId>org.springframework.security</groupId>
//  <artifactId>spring-security-test</artifactId>
//  <scope>test</scope>
// </dependency>

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

  protected Map<String, Pair<Class, Object>> getOverriddenReturnTypes() {
    // Add attributes with 'smart' getters and setters that convert back and forth to correct values
    // for MOLGENIS datatypes
    // Provide the attribute name as key, and a pair of return type (Class) and a Object to be used
    // as test value
    Pair<Class, Object> statusPair = new Pair<>();
    statusPair.setA(Status.class);
    statusPair.setB(Status.SUCCESS);

    Map<String, Pair<Class, Object>> map = new HashMap<>();
    map.put(JobExecutionMetaData.STATUS, statusPair);
    return map;
  }

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
