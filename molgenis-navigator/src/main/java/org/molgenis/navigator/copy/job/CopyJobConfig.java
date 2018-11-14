package org.molgenis.navigator.copy.job;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.molgenis.jobs.Job;
import org.molgenis.jobs.JobFactory;
import org.molgenis.navigator.copy.service.CopyService;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.model.util.ResourceIdentifierUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class CopyJobConfig {

  private final CopyService copyService;

  public CopyJobConfig(CopyService copyService) {
    this.copyService = requireNonNull(copyService);
  }

  @Bean
  public JobFactory<CopyJobExecution> copyJobFactory() {
    return new JobFactory<CopyJobExecution>() {
      @Override
      public Job<String> createJob(CopyJobExecution jobExecution) {
        final List<ResourceIdentifier> resources =
            ResourceIdentifierUtil.getResourcesFromJson(jobExecution.getResources());
        final String targetPackageId = jobExecution.getTargetPackage();
        return progress -> copyService.copy(resources, targetPackageId, progress);
      }
    };
  }
}
