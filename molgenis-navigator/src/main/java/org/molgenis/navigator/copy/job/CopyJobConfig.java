package org.molgenis.navigator.copy.job;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import java.util.List;
import org.molgenis.jobs.Job;
import org.molgenis.jobs.JobFactory;
import org.molgenis.navigator.Resource;
import org.molgenis.navigator.ResourceType;
import org.molgenis.navigator.copy.service.CopyService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings({"squid:S1854", "squid:S1481", "unused"}) // TODO REMOVE ME
public class CopyJobConfig {

  private final CopyService copyService;
  private final Gson gson;

  public CopyJobConfig(CopyService copyService) {
    this.copyService = requireNonNull(copyService);
    this.gson = new Gson();
  }

  @Bean
  public JobFactory<CopyJobExecution> copyJobFactory() {
    return new JobFactory<CopyJobExecution>() {
      @Override
      public Job<String> createJob(CopyJobExecution jobExecution) {
        final List<Resource> resources = toResources(jobExecution.getResources());
        final String targetPackageId = jobExecution.getTargetPackage();
        return progress -> copyService.copy(resources, targetPackageId, progress);
      }
    };
  }

  @SuppressWarnings("unchecked")
  // TODO refactor and make available as utility
  private List<Resource> toResources(String resourceJson) {
    List<Resource> resources = newArrayList();
    List<LinkedTreeMap<String, String>> jsonList = gson.fromJson(resourceJson, List.class);
    for (LinkedTreeMap<String, String> jsonResource : jsonList) {
      resources.add(
          Resource.builder()
              .setType(ResourceType.valueOf(jsonResource.get("type")))
              .setId(jsonResource.get("id"))
              .build());
    }
    return resources;
  }
}
