package org.molgenis.api.metadata.v3;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

import java.util.List;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Sort;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
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
            ? (org.molgenis.data.Query<EntityType>) queryMapper.map(query, repository)
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
  public Attribute findAttribute(String attributeId) {
    // TODO use MetaDataService instead of DataService
    Attribute attribute =
        dataService.findOneById(
            AttributeMetadata.ATTRIBUTE_META_DATA, attributeId, Attribute.class);
    if (attribute == null) {
      // TODO we can't throw an UnknownAttributeException here because it requires EntityType
      throw new UnknownEntityException(AttributeMetadata.ATTRIBUTE_META_DATA, attributeId);
    }
    return attribute;
  }

  public void createEntityType(EntityType entityType) {
    metadataService.addEntityType(entityType);
  }

  @Override
  public void deleteEntityType(String entityTypeId) {
    metadataService.deleteEntityType(entityTypeId);
  }

  @Override
  public void deleteEntityTypes(Query q) {
    Repository<EntityType> entityTypeRepository =
        dataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class);
    org.molgenis.data.Query<EntityType> dataServiceQuery = queryMapper.map(q, entityTypeRepository);
    dataServiceQuery.setFetch(new Fetch().field(EntityTypeMetadata.ID));
    List<EntityType> entityTypes = dataServiceQuery.findAll().collect(toList());
    metadataService.deleteEntityType(entityTypes);
  }
}
