package org.molgenis.api.metadata.v3;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

import java.util.List;
import java.util.Optional;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Sort;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.UnknownRepositoryException;
import org.molgenis.data.meta.EntityTypeWithoutMappedByAttributes;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.springframework.stereotype.Service;

@Service
public class MetadataApiServiceImpl implements MetadataApiService {

  private final MetaDataService metadataService;
  private final QueryMapper queryMapper;
  private final SortMapper sortMapper;
  // TODO replace usage of DataService with new methods in MetaDataService
  private final DataService dataService;

  MetadataApiServiceImpl(
      MetaDataService metadataService,
      QueryMapper queryMapperV3,
      SortMapper sortMapper,
      DataService dataService) {
    this.metadataService = requireNonNull(metadataService);
    this.queryMapper = requireNonNull(queryMapperV3);
    this.sortMapper = requireNonNull(sortMapper);
    this.dataService = requireNonNull(dataService);
  }

  public EntityTypes findEntityTypes(Query query, Sort sort, int size, int number) {
    Repository<org.molgenis.data.meta.model.EntityType> repository =
        metadataService
            .getRepository(ENTITY_TYPE_META_DATA, org.molgenis.data.meta.model.EntityType.class)
            .orElseThrow(() -> new UnknownRepositoryException(ENTITY_TYPE_META_DATA));

    org.molgenis.data.Query<org.molgenis.data.meta.model.EntityType> molgenisQuery =
        query != null
            ? queryMapper.map(query, repository)
            : new QueryImpl<>(repository);

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
    Repository<EntityType> repository =
        metadataService
            .getRepository(ENTITY_TYPE_META_DATA, EntityType.class)
            .orElseThrow(() -> new UnknownRepositoryException(ENTITY_TYPE_META_DATA));

    EntityType entityType = repository.findOneById(identifier);
    if (entityType == null) {
      throw new UnknownEntityTypeException(identifier);
    }

    return entityType;
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
  public Attribute findAttribute(String attributeId, String entityTypeId) {
    EntityType entityType = getEntityType(entityTypeId);

    // TODO use MetaDataService instead of DataService
    Attribute attribute =
        dataService.findOneById(
            AttributeMetadata.ATTRIBUTE_META_DATA, attributeId, Attribute.class);
    if (attribute == null) {
      throw new UnknownAttributeException(entityType, attributeId);
    }
    return attribute;
  }

  private EntityType getEntityType(String entityTypeId) {
    Optional<EntityType> entityType = metadataService.getEntityType(entityTypeId);
    if(!entityType.isPresent()){
      throw new UnknownEntityTypeException(entityTypeId);
    }
    return entityType.get();
  }

  @Override
  public Void deleteAttribute(String attributeId, String entityTypeId) {
    getEntityType(entityTypeId);
    metadataService.deleteAttributeById(attributeId);
    return null;
  }

  @Override
  public Void deleteAttributes(List<String> attributeIds, String entityTypeId) {
    getEntityType(entityTypeId);
    metadataService.deleteAttributesById(attributeIds);
    return null;
  }

  public void createEntityType(EntityType entityType) {
    addEntityTypeFirstPass(entityType);
    updateEntityTypeSecondPass(entityType);
  }

  @Override
  public Void updateEntityType(EntityType entityType) {
    return null; // FIXME
  }

  @Override
  public Void deleteEntityType(String entityTypeId) {
    metadataService.deleteEntityType(entityTypeId);
    return null;
  }

  @Override
  public Void deleteEntityTypes(List<String> entityTypeIds) {
    metadataService.deleteEntityTypes(entityTypeIds);
    return null;
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
}
