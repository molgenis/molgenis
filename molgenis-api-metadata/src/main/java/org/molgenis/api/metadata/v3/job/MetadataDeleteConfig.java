package org.molgenis.api.metadata.v3.job;

import java.util.List;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.jobs.Job;
import org.molgenis.jobs.JobFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetadataDeleteConfig {

  private final MetaDataService metadataService;

  public MetadataDeleteConfig(MetaDataService metaDataService) {
    this.metadataService = metaDataService;
  }

  @Bean
  public JobFactory<MetadataDeleteJobExecution> metadataDeleteJobExecutionJobFactory() {
    return new JobFactory<>() {
      @Override
      public Job createJob(MetadataDeleteJobExecution metadataDeleteJobExecution) {
        List<String> ids = metadataDeleteJobExecution.getIds();
        if (ids.size() == 1) {
          return progress -> deleteEntityType(ids.get(0));
        } else {
          return progress -> deleteEntityTypes(ids);
        }
      }
    };
  }

  private Void deleteEntityType(String entityTypeId) {
    metadataService.deleteEntityType(entityTypeId);
    return null;
  }

  private Void deleteEntityTypes(List<String> entityTypeIds) {
    metadataService.deleteEntityTypes(entityTypeIds);
    return null;
  }
}
