package org.molgenis.api.metadata.v3;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Sort;
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
public class MetadataServiceImpl {

  private final MetaDataService metadataService;
  private final QueryMapper queryMapper;
  private final SortMapper sortMapper;

  MetadataServiceImpl(
      MetaDataService metadataService,
      QueryMapper queryMapperV3,
      SortMapper sortMapper,
      MetadataV3Mapper entityTypeMapper) {
    this.metadataService = requireNonNull(metadataService);
    this.queryMapper = requireNonNull(queryMapperV3);
    this.sortMapper = requireNonNull(sortMapper);
  }

  public EntityTypes findEntityTypes(Query query, Sort sort, int size, int number) {
    Repository<org.molgenis.data.meta.model.EntityType> repository =
        metadataService
            .getRepository(
                EntityTypeMetadata.ENTITY_TYPE_META_DATA,
                org.molgenis.data.meta.model.EntityType.class)
            .orElseThrow(
                () -> new UnknownRepositoryException(EntityTypeMetadata.ENTITY_TYPE_META_DATA));

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
            .getRepository(EntityTypeMetadata.ENTITY_TYPE_META_DATA, EntityType.class)
            .orElseThrow(
                () -> new UnknownRepositoryException(EntityTypeMetadata.ENTITY_TYPE_META_DATA));

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
        query != null
            ? (org.molgenis.data.Query<Attribute>) queryMapper.map(query, repository)
            : new QueryImpl<>(repository);

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

  public Attribute findAttribute(String entityTypeId, String attributeName) {
    Optional<EntityType> entityType = metadataService.getEntityType(entityTypeId);
    if (entityType.isPresent()) {
      // FIXME: get by id or name
      return entityType.get().getAttribute(attributeName);
    }
    return null;
  }

  public void createEntityType(EntityType entityType) {
    metadataService.addEntityType(entityType);
  }
}
