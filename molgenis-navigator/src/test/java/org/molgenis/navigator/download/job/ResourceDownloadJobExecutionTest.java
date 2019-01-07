package org.molgenis.navigator.download.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.jobs.config.JobTestConfig;
import org.molgenis.jobs.model.JobExecution.Status;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.model.ResourceType;
import org.molgenis.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      ResourceDownloadJobExecutionMetadata.class,
      ResourceDownloadJobExecutionFactory.class,
      JobPackage.class,
      JobTestConfig.class
    })
public class ResourceDownloadJobExecutionTest extends AbstractSystemEntityTest {

  @Autowired ResourceDownloadJobExecutionMetadata metadata;
  @Autowired ResourceDownloadJobExecutionFactory factory;

  private Map<String, Pair<Class, Object>> getOverriddenReturnTypes() {
    // Add attributes with 'smart' getters and setters that convert back and forth to correct values
    // for MOLGENIS datatypes
    // Provide the attribute name as key, and a pair of return type (Class) and a Object to be used
    // as test value
    Map<String, Pair<Class, Object>> map = new HashMap<>();

    Pair<Class, Object> resourcesPair = new Pair<>();
    resourcesPair.setA(List.class);
    ResourceIdentifier resource =
        ResourceIdentifier.create(ResourceType.ENTITY_TYPE, "testResource");
    resourcesPair.setB(Collections.singletonList(resource));

    Pair<Class, Object> statusPair = new Pair<>();
    statusPair.setA(Status.class);
    statusPair.setB(Status.SUCCESS);

    map.put(ResourceDownloadJobExecutionMetadata.RESOURCES, resourcesPair);
    map.put(JobExecutionMetaData.STATUS, statusPair);
    return map;
  }

  private List<String> getExcludedAttrs() {
    List<String> attrs = new ArrayList<>();
    attrs.add(JobExecutionMetaData.FAILURE_EMAIL);
    attrs.add(JobExecutionMetaData.SUCCESS_EMAIL);
    return attrs;
  }

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata,
        ResourceDownloadJobExecution.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
