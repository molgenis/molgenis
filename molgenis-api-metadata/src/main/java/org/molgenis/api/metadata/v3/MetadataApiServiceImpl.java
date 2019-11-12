package org.molgenis.api.metadata.v3;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

import java.util.List;
import java.util.Optional;
import org.molgenis.api.metadata.v3.exception.ZeroResultsException;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecution;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Sort;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.UnknownRepositoryException;
import org.molgenis.data.meta.EntityTypeWithoutMappedByAttributes;
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
  // TODO replace usage of DataService with new methods in MetaDataService
  private final DataService dataService;
  private final MetadataApiJobService metadataApiJobService;

  MetadataApiServiceImpl(
      MetaDataService metadataService,
      QueryMapper queryMapperV3,
      SortMapper sortMapper,
      DataService dataService,
      MetadataApiJobService metadataApiJobService) {
    this.metadataService = requireNonNull(metadataService);
    this.queryMapper = requireNonNull(queryMapperV3);
    this.sortMapper = requireNonNull(sortMapper);
    this.dataService = requireNonNull(dataService);
    this.metadataApiJobService = requireNonNull(metadataApiJobService);
  }

  public EntityTypes findEntityTypes(Query query, Sort sort, int size, int number) {
    Repository<org.molgenis.data.meta.model.EntityType> repository =
        metadataService
            .getRepository(ENTITY_TYPE_META_DATA, org.molgenis.data.meta.model.EntityType.class)
            .orElseThrow(() -> new UnknownRepositoryException(ENTITY_TYPE_META_DATA));

    org.molgenis.data.Query<org.molgenis.data.meta.model.EntityType> molgenisQuery =
        query != null ? queryMapper.map(query, repository) : new QueryImpl<>(repository);

    // get entities
    org.molgenis.data.Query<org.molgenis.data.meta.model.EntityType> findQuery =
        new QueryImpl<>(molgenisQuery);
    findQuery.offset(number * size);
    findQuery.pageSize(size);
    findQuery.sort(sortMapper.map(sort));
    List<org.molgenis.data.meta.model.EntityType> entityTypes =
        repository.findAll(findQuery).collect(toList());

    org.molgenis.data.Query<org.molgenis.data.meta.model.EntityType> countQuery =
        new QueryImpl<>(molgenisQuery);
    countQuery.offset(0);
    countQuery.pageSize(Integer.MAX_VALUE);
    int count = Math.toIntExact(repository.count(countQuery));

    return EntityTypes.builder().setEntityTypes(entityTypes).setTotal(count).build();
  }

  public EntityType findEntityType(String identifier) {
    Optional<EntityType> entityType = metadataService.getEntityType(identifier);
    if (!entityType.isPresent()) {
      throw new UnknownEntityTypeException(identifier);
    }
    return entityType.get();
  }

  public Attributes findAttributes(
      String entityTypeId, Query query, Sort sort, int size, int number) {
    Repository<org.molgenis.data.meta.model.Attribute> repository =
        metadataService
            .getRepository(
                AttributeMetadata.ATTRIBUTE_META_DATA, org.molgenis.data.meta.model.Attribute.class)
            .orElseThrow(
                () -> new UnknownRepositoryException(AttributeMetadata.ATTRIBUTE_META_DATA));

    org.molgenis.data.Query<org.molgenis.data.meta.model.Attribute> molgenisQuery =
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
    List<org.molgenis.data.meta.model.Attribute> attributes =
        repository.findAll(findQuery).collect(toList());

    org.molgenis.data.Query<org.molgenis.data.meta.model.Attribute> countQuery =
        new QueryImpl<>(molgenisQuery);
    countQuery.offset(0);
    countQuery.pageSize(Integer.MAX_VALUE);
    int count = Math.toIntExact(repository.count(countQuery));

    return Attributes.builder().setAttributes(attributes).setTotal(count).build();
  }

  @Override
  public Attribute findAttribute(String entityTypeId, String attributeId) {
    EntityType entityType = findEntityType(entityTypeId);

    // TODO use MetaDataService instead of DataService
    Attribute attribute =
        dataService.findOneById(
            AttributeMetadata.ATTRIBUTE_META_DATA, attributeId, Attribute.class);
    if (attribute == null) {
      throw new UnknownAttributeException(entityType, attributeId);
    }
    return attribute;
  }

  @Override
  public MetadataUpsertJobExecution deleteAttribute(String entityTypeId, String attributeId) {
    findEntityType(entityTypeId);
    EntityType entityType = findEntityType(entityTypeId);
    Attribute attribute = validateAttributePartOfEntity(entityType, attributeId);
    entityType.removeAttribute(attribute);
    return metadataApiJobService.scheduleUpdate(entityType);
  }

  @Override
  public MetadataUpsertJobExecution deleteAttributes(String entityTypeId, Query query) {
    EntityType entityType = findEntityType(entityTypeId);
    findAttributes(entityTypeId, query).forEach(entityType::removeAttribute);
    return metadataApiJobService.scheduleUpdate(entityType);
  }

  public void createEntityType(EntityType entityType) {
    addEntityTypeFirstPass(entityType);
    updateEntityTypeSecondPass(entityType);
  }

  @Override
  public MetadataUpsertJobExecution updateEntityType(EntityType entityType) {
    return metadataApiJobService.scheduleUpdate(entityType);
  }

  @Override
  public MetadataDeleteJobExecution deleteEntityType(String entityTypeId) {
    validateEntityTypeExists(entityTypeId);
    return metadataApiJobService.scheduleDelete(singletonList(entityTypeId));
  }

  @Override
  public MetadataDeleteJobExecution deleteEntityTypes(Query query) {
    return metadataApiJobService.scheduleDelete(getEntityTypeIds(query));
  }

  // TODO remove code duplication with molgenis-data-import DataPersisterImpl
  private void addEntityTypeFirstPass(EntityType entityType) {
    EntityType persistableEntityType;
    if (entityType.hasMappedByAttributes()) {
      persistableEntityType = new EntityTypeWithoutMappedByAttributes(entityType);
    } else {
      persistableEntityType = entityType;
    }
    metadataService.addEntityType(persistableEntityType);
  }

  // TODO remove code duplication with molgenis-data-import DataPersisterImpl
  private void updateEntityTypeSecondPass(EntityType entityType) {
    if (entityType.hasMappedByAttributes()) {
      metadataService.updateEntityType(entityType);
    }
  }

  private void validateEntityTypeExists(String entityTypeId) {
    if (!metadataService.hasEntityType(entityTypeId)) {
      throw new UnknownEntityTypeException(entityTypeId);
    }
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

  private List<Attribute> findAttributes(String entityTypeId, Query q) {
    Repository<Attribute> attributeRepository =
        dataService.getRepository(ATTRIBUTE_META_DATA, Attribute.class);
    org.molgenis.data.Query<Attribute> dataServiceQuery =
        queryMapper.map(q, attributeRepository).and().eq(AttributeMetadata.ENTITY, entityTypeId);
    dataServiceQuery.setFetch(new Fetch().field(AttributeMetadata.ID));
    List<Attribute> attributes = dataServiceQuery.findAll().collect(toList());
    if (attributes.isEmpty()) {
      throw new ZeroResultsException(q);
    }
    return attributes;
  }

  private Attribute validateAttributePartOfEntity(EntityType entityType, String attributeId) {
    Attribute attribute =
        dataService.findOneById(ATTRIBUTE_META_DATA, attributeId, Attribute.class);

    if (attribute == null || !attribute.getEntity().getId().equals(entityType.getId())) {
      throw new UnknownAttributeException(entityType, attributeId);
    }
    return attribute;
  }
}
