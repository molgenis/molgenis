package org.molgenis.api.metadata.v3;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

import java.util.List;
import org.molgenis.api.data.QueryMapper;
import org.molgenis.api.data.SortMapper;
import org.molgenis.api.metadata.v3.exception.ZeroResultsException;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecution;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Sort;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.UnknownRepositoryException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.support.QueryImpl;
import org.springframework.stereotype.Service;

@Service
public class MetadataApiServiceImpl implements MetadataApiService {

  private final MetaDataService metadataService;
  private final QueryMapper queryMapper;
  private final SortMapper sortMapper;
  private final MetadataApiJobService metadataApiJobService;

  MetadataApiServiceImpl(
      MetaDataService metadataService,
      QueryMapper queryMapperV3,
      SortMapper sortMapper,
      MetadataApiJobService metadataApiJobService) {
    this.metadataService = requireNonNull(metadataService);
    this.queryMapper = requireNonNull(queryMapperV3);
    this.sortMapper = requireNonNull(sortMapper);
    this.metadataApiJobService = requireNonNull(metadataApiJobService);
  }

  public EntityTypes findEntityTypes(Query query, Sort sort, int size, int number) {
    Repository<EntityType> repository =
        metadataService
            .getRepository(ENTITY_TYPE_META_DATA, EntityType.class)
            .orElseThrow(() -> new UnknownRepositoryException(ENTITY_TYPE_META_DATA));

    org.molgenis.data.Query<EntityType> molgenisQuery =
        query != null ? queryMapper.map(query, repository) : new QueryImpl<>(repository);

    // get entities
    org.molgenis.data.Query<EntityType> findQuery = new QueryImpl<>(molgenisQuery);
    findQuery.offset(number * size);
    findQuery.pageSize(size);
    findQuery.sort(sortMapper.map(sort));
    List<EntityType> entityTypes = repository.findAll(findQuery).collect(toList());

    org.molgenis.data.Query<EntityType> countQuery = new QueryImpl<>(molgenisQuery);
    countQuery.offset(0);
    countQuery.pageSize(Integer.MAX_VALUE);
    int count = Math.toIntExact(repository.count(countQuery));

    return EntityTypes.builder().setEntityTypes(entityTypes).setTotal(count).build();
  }

  @Override
  public EntityType findEntityType(String identifier) {
    return metadataService
        .getEntityType(identifier)
        .orElseThrow(() -> new UnknownEntityTypeException(identifier));
  }

  @Override
  public Attributes findAttributes(
      String entityTypeId, Query query, Sort sort, int size, int number) {
    Repository<Attribute> repository =
        metadataService
            .getRepository(AttributeMetadata.ATTRIBUTE_META_DATA, Attribute.class)
            .orElseThrow(
                () -> new UnknownRepositoryException(AttributeMetadata.ATTRIBUTE_META_DATA));

    org.molgenis.data.Query<Attribute> molgenisQuery =
        query != null ? queryMapper.map(query, repository) : new QueryImpl<>(repository);

    boolean nest = !molgenisQuery.getRules().isEmpty();
    if (nest) {
      molgenisQuery.and();
      molgenisQuery.nest();
    }
    molgenisQuery.eq(AttributeMetadata.ENTITY, entityTypeId);
    if (nest) {
      molgenisQuery.unnest();
    }
    // get entities
    org.molgenis.data.Query<Attribute> findQuery = new QueryImpl<>(molgenisQuery);
    findQuery.offset(number * size);
    findQuery.pageSize(size);
    findQuery.sort(sortMapper.map(sort));
    List<Attribute> attributes = repository.findAll(findQuery).collect(toList());

    org.molgenis.data.Query<Attribute> countQuery = new QueryImpl<>(molgenisQuery);
    countQuery.offset(0);
    countQuery.pageSize(Integer.MAX_VALUE);
    int count = Math.toIntExact(repository.count(countQuery));

    return Attributes.builder().setAttributes(attributes).setTotal(count).build();
  }

  @Override
  public Attribute findAttribute(String entityTypeId, String attributeId) {
    EntityType entityType = findEntityType(entityTypeId);
    return entityType.getOwnAttributeById(attributeId);
  }

  @Override
  public MetadataUpsertJobExecution deleteAttributeAsync(String entityTypeId, String attributeId) {
    EntityType entityType = findEntityType(entityTypeId);
    Attribute attribute = entityType.getOwnAttributeById(attributeId);
    entityType.removeAttribute(attribute);
    return metadataApiJobService.scheduleUpdate(entityType);
  }

  @Override
  public MetadataUpsertJobExecution deleteAttributesAsync(String entityTypeId, Query query) {
    EntityType entityType = findEntityType(entityTypeId);
    findAttributes(entityTypeId, query).forEach(entityType::removeAttribute);
    return metadataApiJobService.scheduleUpdate(entityType);
  }

  @Override
  public void createEntityType(EntityType entityType) {
    metadataService.addEntityType(entityType);
  }

  @Override
  public MetadataUpsertJobExecution updateEntityTypeAsync(EntityType entityType) {
    return metadataApiJobService.scheduleUpdate(entityType);
  }

  @Override
  public MetadataDeleteJobExecution deleteEntityTypeAsync(String entityTypeId) {
    EntityType entityType = findEntityType(entityTypeId);
    return metadataApiJobService.scheduleDelete(entityType);
  }

  @Override
  public MetadataDeleteJobExecution deleteEntityTypesAsync(Query query) {
    return metadataApiJobService.scheduleDelete(getEntityTypes(query));
  }

  private List<EntityType> getEntityTypes(Query q) {
    Repository<EntityType> entityTypeRepository =
        metadataService
            .getRepository(ENTITY_TYPE_META_DATA, EntityType.class)
            .orElseThrow(() -> new UnknownRepositoryException(ENTITY_TYPE_META_DATA));
    org.molgenis.data.Query<EntityType> dataServiceQuery = queryMapper.map(q, entityTypeRepository);
    dataServiceQuery.setFetch(new Fetch().field(EntityTypeMetadata.ID));
    List<EntityType> entityTypes = dataServiceQuery.findAll().collect(toList());

    if (entityTypes.isEmpty()) {
      throw new ZeroResultsException(q);
    }
    return entityTypes;
  }

  private List<Attribute> findAttributes(String entityTypeId, Query q) {
    Repository<Attribute> attributeRepository =
        metadataService
            .getRepository(ATTRIBUTE_META_DATA, Attribute.class)
            .orElseThrow(() -> new UnknownRepositoryException(ENTITY_TYPE_META_DATA));
    org.molgenis.data.Query<Attribute> dataServiceQuery =
        queryMapper.map(q, attributeRepository).and().eq(AttributeMetadata.ENTITY, entityTypeId);
    dataServiceQuery.setFetch(new Fetch().field(AttributeMetadata.ID));
    List<Attribute> attributes = dataServiceQuery.findAll().collect(toList());
    if (attributes.isEmpty()) {
      throw new ZeroResultsException(q);
    }
    return attributes;
  }
}
