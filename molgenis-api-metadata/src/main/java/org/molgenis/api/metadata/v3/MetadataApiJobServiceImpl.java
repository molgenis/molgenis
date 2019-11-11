package org.molgenis.api.metadata.v3;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

import java.util.List;
import org.molgenis.api.metadata.v3.exception.ZeroResultsException;
import org.molgenis.api.metadata.v3.job.EntityTypeSerializer;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecutionFactory;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecutionMetadata.DeleteType;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecutionFactory;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecutionMetadata.Action;
import org.molgenis.api.model.Query;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
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
  private final MetaDataService metaDataService;
  private final QueryMapper queryMapper;

  MetadataApiJobServiceImpl(
      MetadataUpsertJobExecutionFactory metadataUpsertJobExecutionFactory,
      MetadataDeleteJobExecutionFactory metadataDeleteJobExecutionFactory,
      JobExecutor jobExecutor,
      EntityTypeSerializer entityTypeSerializer,
      DataService dataService,
      MetaDataService metaDataService,
      QueryMapper queryMapper) {
    this.metadataUpsertJobExecutionFactory = requireNonNull(metadataUpsertJobExecutionFactory);
    this.metadataDeleteJobExecutionFactory = requireNonNull(metadataDeleteJobExecutionFactory);
    this.jobExecutor = requireNonNull(jobExecutor);
    this.entityTypeSerializer = requireNonNull(entityTypeSerializer);
    this.dataService = requireNonNull(dataService);
    this.metaDataService = requireNonNull(metaDataService);
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
  public MetadataDeleteJobExecution scheduleDeleteEntityType(String entityTypeId) {
    validateEntityTypeExists(entityTypeId);
    return scheduleDeleteEntityType(DeleteType.ENTITY_TYPE, singletonList(entityTypeId));
  }

  @Override
  public MetadataDeleteJobExecution scheduleDeleteEntityType(Query query) {
    return scheduleDeleteEntityType(DeleteType.ENTITY_TYPE, getEntityTypeIds(query));
  }

  @Override
  public MetadataDeleteJobExecution scheduleDeleteAttribute(
      String entityTypeId, String attributeId) {
    validateAttributePartOfEntity(entityTypeId, attributeId);
    return scheduleDeleteAttribute(DeleteType.ATTRIBUTE, entityTypeId, singletonList(attributeId));
  }

  @Override
  public MetadataDeleteJobExecution scheduleDeleteAttribute(String entityTypeId, Query query) {
    validateEntityTypeExists(entityTypeId);
    return scheduleDeleteAttribute(
        DeleteType.ATTRIBUTE, entityTypeId, getAttributeIds(entityTypeId, query));
  }

  private MetadataUpsertJobExecution scheduleUpsert(Action action, EntityType entityType) {
    MetadataUpsertJobExecution jobExecution = metadataUpsertJobExecutionFactory.create();
    jobExecution.setAction(action);
    jobExecution.setEntityTypeData(entityTypeSerializer.serializeEntityType(entityType));
    jobExecutor.submit(jobExecution);
    return jobExecution;
  }

  private MetadataDeleteJobExecution scheduleDeleteAttribute(
      DeleteType deleteType, String entityTypeId, List<String> ids) {
    MetadataDeleteJobExecution jobExecution = metadataDeleteJobExecutionFactory.create();
    jobExecution.setDeleteType(deleteType);
    jobExecution.setIds(ids);
    jobExecution.setEntityTypeId(entityTypeId);
    jobExecutor.submit(jobExecution);
    return jobExecution;
  }

  private MetadataDeleteJobExecution scheduleDeleteEntityType(
      DeleteType deleteType, List<String> ids) {
    MetadataDeleteJobExecution jobExecution = metadataDeleteJobExecutionFactory.create();
    jobExecution.setDeleteType(deleteType);
    jobExecution.setIds(ids);
    jobExecutor.submit(jobExecution);
    return jobExecution;
  }

  private List<String> getEntityTypeIds(Query q) {
    Repository<EntityType> entityTypeRepository =
        dataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class);
    org.molgenis.data.Query<EntityType> dataServiceQuery = queryMapper.map(q, entityTypeRepository);
    dataServiceQuery.setFetch(new Fetch().field(EntityTypeMetadata.ID));
    List<String> entityTypeIds =
        dataServiceQuery.findAll().map(EntityType::getId).collect(toList());
    if (entityTypeIds.isEmpty()) {
      throw new ZeroResultsException(q);
    }
    return entityTypeIds;
  }

  private List<String> getAttributeIds(String entityTypeId, Query q) {
    Repository<Attribute> attributeRepository =
        dataService.getRepository(ATTRIBUTE_META_DATA, Attribute.class);
    org.molgenis.data.Query<Attribute> dataServiceQuery =
        queryMapper.map(q, attributeRepository).and().eq(AttributeMetadata.ENTITY, entityTypeId);
    dataServiceQuery.setFetch(new Fetch().field(AttributeMetadata.ID));
    List<String> attributeIds =
        dataServiceQuery.findAll().map(Attribute::getIdentifier).collect(toList());
    if (attributeIds.isEmpty()) {
      throw new ZeroResultsException(q);
    }
    return attributeIds;
  }

  private void validateEntityTypeExists(String entityTypeId) {
    if (!metaDataService.hasEntityType(entityTypeId)) {
      throw new UnknownEntityTypeException(entityTypeId);
    }
  }

  private void validateAttributePartOfEntity(String entityTypeId, String attributeId) {
    EntityType entityType =
        metaDataService
            .getEntityType(entityTypeId)
            .orElseThrow(() -> new UnknownEntityTypeException(entityTypeId));
    Attribute attribute =
        dataService.findOneById(ATTRIBUTE_META_DATA, attributeId, Attribute.class);

    if (attribute == null || !attribute.getEntity().getId().equals(entityTypeId)) {
      throw new UnknownAttributeException(entityType, attributeId);
    }
  }
}
