package org.molgenis.api.metadata.v3;

import static java.util.Objects.requireNonNull;

import org.molgenis.api.metadata.v3.job.EntityTypeSerializer;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecutionFactory;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecutionMetadata.Action;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.JobExecutor;
import org.springframework.stereotype.Service;

@Service
public class MetadataApiJobServiceImpl implements MetadataApiJobService {
  private final MetadataUpsertJobExecutionFactory metadataUpsertJobExecutionFactory;
  private final JobExecutor jobExecutor;
  private final EntityTypeSerializer entityTypeSerializer;

  public MetadataApiJobServiceImpl(
      MetadataUpsertJobExecutionFactory metadataUpsertJobExecutionFactory,
      JobExecutor jobExecutor,
      EntityTypeSerializer entityTypeSerializer) {
    this.metadataUpsertJobExecutionFactory = requireNonNull(metadataUpsertJobExecutionFactory);
    this.jobExecutor = requireNonNull(jobExecutor);
    this.entityTypeSerializer = requireNonNull(entityTypeSerializer);
  }

  @Override
  public MetadataUpsertJobExecution scheduleCreate(EntityType entityType) {
    return schedule(Action.CREATE, entityType);
  }

  @Override
  public MetadataUpsertJobExecution scheduleUpdate(EntityType entityType) {
    return schedule(Action.UPDATE, entityType);
  }

  private MetadataUpsertJobExecution schedule(Action action, EntityType entityType) {
    MetadataUpsertJobExecution jobExecution = metadataUpsertJobExecutionFactory.create();
    jobExecution.setAction(action);
    jobExecution.setEntityTypeData(entityTypeSerializer.serializeEntityType(entityType));
    jobExecutor.submit(jobExecution);
    return jobExecution;
  }
}
