package org.molgenis.api.metadata.v3;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

import java.util.List;
import org.molgenis.api.metadata.v3.job.EntityTypeSerializer;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecutionFactory;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecutionFactory;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecutionMetadata.Action;
import org.molgenis.api.model.Query;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.jobs.JobExecutor;
import org.springframework.stereotype.Service;

@Service
public class MetadataApiJobServiceImpl implements MetadataApiJobService {

  private final MetadataUpsertJobExecutionFactory metadataUpsertJobExecutionFactory;
  private final MetadataDeleteJobExecutionFactory metadataDeleteJobExecutionFactory;
  private final JobExecutor jobExecutor;
  private final EntityTypeSerializer entityTypeSerializer;
  private final DataService dataService;
  private final QueryMapper queryMapper;

  public MetadataApiJobServiceImpl(
      MetadataUpsertJobExecutionFactory metadataUpsertJobExecutionFactory,
      MetadataDeleteJobExecutionFactory metadataDeleteJobExecutionFactory,
      JobExecutor jobExecutor,
      EntityTypeSerializer entityTypeSerializer,
      DataService dataService,
      QueryMapper queryMapper) {
    this.metadataUpsertJobExecutionFactory = requireNonNull(metadataUpsertJobExecutionFactory);
    this.metadataDeleteJobExecutionFactory = requireNonNull(metadataDeleteJobExecutionFactory);
    this.jobExecutor = requireNonNull(jobExecutor);
    this.entityTypeSerializer = requireNonNull(entityTypeSerializer);
    this.dataService = requireNonNull(dataService);
    this.queryMapper = requireNonNull(queryMapper);
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
  public MetadataDeleteJobExecution scheduleDelete(String entityTypeId) {
    return scheduleDelete(singletonList(entityTypeId));
  }

  @Override
  public MetadataDeleteJobExecution scheduleDelete(Query query) {
    return scheduleDelete(getEntityTypeIds(query));
  }

  private MetadataUpsertJobExecution scheduleUpsert(Action action, EntityType entityType) {
    MetadataUpsertJobExecution jobExecution = metadataUpsertJobExecutionFactory.create();
    jobExecution.setAction(action);
    jobExecution.setEntityTypeData(entityTypeSerializer.serializeEntityType(entityType));
    jobExecutor.submit(jobExecution);
    return jobExecution;
  }

  private MetadataDeleteJobExecution scheduleDelete(List<String> entityTypeIds) {
    MetadataDeleteJobExecution jobExecution = metadataDeleteJobExecutionFactory.create();
    jobExecution.setEntityTypeIds(entityTypeIds);
    jobExecutor.submit(jobExecution);
    return jobExecution;
  }

  private List<String> getEntityTypeIds(Query q) {
    Repository<EntityType> entityTypeRepository =
        dataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class);
    org.molgenis.data.Query<EntityType> dataServiceQuery = queryMapper.map(q, entityTypeRepository);
    dataServiceQuery.setFetch(new Fetch().field(EntityTypeMetadata.ID));
    return dataServiceQuery.findAll().map(EntityType::getId).collect(toList());
  }
}
