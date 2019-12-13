package org.molgenis.api.metadata.v3.job;

import static java.util.Objects.requireNonNull;

import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecutionMetadata.Action;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.Job;
import org.molgenis.jobs.JobFactory;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetadataUpsertConfig {
  private final MetaDataService metadataService;
  private final EntityTypeSerializer entityTypeSerializer;

  public MetadataUpsertConfig(
      MetaDataService metadataService, EntityTypeSerializer entityTypeSerializer) {
    this.metadataService = requireNonNull(metadataService);
    this.entityTypeSerializer = requireNonNull(entityTypeSerializer);
  }

  @Bean
  public JobFactory<MetadataUpsertJobExecution> metadataUpsertJobExecutionJobFactory() {
    return new JobFactory<>() {
      @Override
      public Job createJob(MetadataUpsertJobExecution metadataUpsertJobExecution) {
        String entityTypeData = metadataUpsertJobExecution.getEntityTypeData();
        EntityType entityType = entityTypeSerializer.deserializeEntityType(entityTypeData);

        Action action = metadataUpsertJobExecution.getAction();
        switch (action) {
          case CREATE:
            throw new UnsupportedOperationException();
          case UPDATE:
            return progress -> updateEntityType(entityType);
          default:
            throw new UnexpectedEnumException(action);
        }
      }
    };
  }

  private Void updateEntityType(EntityType entityType) {
    metadataService.updateEntityType(entityType);
    return null;
  }
}
