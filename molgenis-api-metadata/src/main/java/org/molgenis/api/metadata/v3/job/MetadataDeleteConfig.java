package org.molgenis.api.metadata.v3.job;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.molgenis.api.metadata.v3.MetadataApiService;
import org.molgenis.jobs.Job;
import org.molgenis.jobs.JobFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetadataDeleteConfig {

  private final MetadataApiService metadataApiService;

  public MetadataDeleteConfig(MetadataApiService metadataApiService) {
    this.metadataApiService = requireNonNull(metadataApiService);
  }

  @Bean
  public JobFactory<MetadataDeleteJobExecution> metadataDeleteJobExecutionJobFactory() {
    return new JobFactory<>() {
      @Override
      public Job createJob(MetadataDeleteJobExecution metadataDeleteJobExecution) {
        List<String> entityTypeIds = metadataDeleteJobExecution.getEntityTypeIds();
        if (entityTypeIds.size() == 1) {
          return progress -> metadataApiService.deleteEntityType(entityTypeIds.get(0));
        } else {
          return progress ->
              metadataApiService.deleteEntityTypes(metadataDeleteJobExecution.getEntityTypeIds());
        }
      }
    };
  }
}
