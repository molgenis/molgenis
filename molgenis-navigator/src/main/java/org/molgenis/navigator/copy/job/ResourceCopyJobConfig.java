package org.molgenis.navigator.copy.job;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.molgenis.jobs.Job;
import org.molgenis.jobs.JobFactory;
import org.molgenis.navigator.copy.service.CopyService;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("unused")
@Configuration
public class ResourceCopyJobConfig {

  private final CopyService copyService;

  public ResourceCopyJobConfig(CopyService copyService) {
    this.copyService = requireNonNull(copyService);
  }

  @Bean
  public JobFactory<ResourceCopyJobExecution> copyJobFactory() {
    return new JobFactory<ResourceCopyJobExecution>() {
      @Override
      public Job<Void> createJob(ResourceCopyJobExecution jobExecution) {
        final List<ResourceIdentifier> resources = jobExecution.getResources();
        final String targetPackageId = jobExecution.getTargetPackage();
        return progress -> copyService.copy(resources, targetPackageId, progress);
      }
    };
  }
}
