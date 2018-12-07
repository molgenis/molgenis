package org.molgenis.navigator.delete;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.molgenis.jobs.Job;
import org.molgenis.jobs.JobFactory;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
public class ResourceDeleteJobConfig {

  private final ResourceDeleteService resourceDeleteService;

  public ResourceDeleteJobConfig(ResourceDeleteService resourceDeleteService) {
    this.resourceDeleteService = requireNonNull(resourceDeleteService);
  }

  @Bean
  public JobFactory<ResourceDeleteJobExecution> copyJobFactory() {
    return new JobFactory<ResourceDeleteJobExecution>() {
      @Override
      public Job<Void> createJob(ResourceDeleteJobExecution jobExecution) {
        final List<ResourceIdentifier> resources = jobExecution.getResources();
        return progress -> resourceDeleteService.delete(resources, progress);
      }
    };
  }
}
