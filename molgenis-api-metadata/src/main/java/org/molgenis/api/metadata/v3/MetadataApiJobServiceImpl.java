package org.molgenis.api.metadata.v3;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.molgenis.api.metadata.v3.job.EntityTypeSerializer;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecutionFactory;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecutionFactory;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecutionMetadata.Action;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.JobExecutor;
import org.springframework.stereotype.Service;

@Service
public class MetadataApiJobServiceImpl implements MetadataApiJobService {

  private final MetadataUpsertJobExecutionFactory metadataUpsertJobExecutionFactory;
  private final MetadataDeleteJobExecutionFactory metadataDeleteJobExecutionFactory;
  private final JobExecutor jobExecutor;
  private final EntityTypeSerializer entityTypeSerializer;

  MetadataApiJobServiceImpl(
      MetadataUpsertJobExecutionFactory metadataUpsertJobExecutionFactory,
      MetadataDeleteJobExecutionFactory metadataDeleteJobExecutionFactory,
      JobExecutor jobExecutor,
      EntityTypeSerializer entityTypeSerializer) {
    this.metadataUpsertJobExecutionFactory = requireNonNull(metadataUpsertJobExecutionFactory);
    this.metadataDeleteJobExecutionFactory = requireNonNull(metadataDeleteJobExecutionFactory);
    this.jobExecutor = requireNonNull(jobExecutor);
    this.entityTypeSerializer = requireNonNull(entityTypeSerializer);
  }

  @Override
  public MetadataUpsertJobExecution scheduleCreate(EntityType entityType) {
    return scheduleUpsert(Action.CREATE, entityType);
  }

  @Override
  public MetadataUpsertJobExecution scheduleUpdate(EntityType entityType) {
    return scheduleUpsert(Action.UPDATE, entityType);
  }

  @Override
  public MetadataDeleteJobExecution scheduleDelete(List<String> entityTypeIds) {
    MetadataDeleteJobExecution jobExecution = metadataDeleteJobExecutionFactory.create();
    jobExecution.setIds(entityTypeIds);
    jobExecutor.submit(jobExecution);
    return jobExecution;
  }

  private MetadataUpsertJobExecution scheduleUpsert(Action action, EntityType entityType) {
    MetadataUpsertJobExecution jobExecution = metadataUpsertJobExecutionFactory.create();
    jobExecution.setAction(action);
    jobExecution.setEntityTypeData(entityTypeSerializer.serializeEntityType(entityType));
    jobExecutor.submit(jobExecution);
    return jobExecution;
  }
}
