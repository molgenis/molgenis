package org.molgenis.api.meta;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.List;
import org.molgenis.api.data.v3.FetchMapper;
import org.molgenis.api.data.v3.SortV3Mapper;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Selection;
import org.molgenis.api.model.Sort;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownRepositoryException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.support.QueryImpl;
import org.springframework.stereotype.Service;

@Service
public class MetadataServiceImpl {

  private final MetaDataService metadataService;
  private final QueryMapper queryMapper;
  private final SortV3Mapper sortMapperV3; // FIXME: write our own?
  private final FetchMapper fetchMapper;
  private final EntityTypeMetadata entityTypeMetadata;

  MetadataServiceImpl(
      MetaDataService metadataService,
      QueryMapper queryMapperV3,
      SortV3Mapper sortMapperV3,
      FetchMapper fetchMapper,
      EntityTypeMetadata entityTypeMetadata) {
    this.metadataService = requireNonNull(metadataService);
    this.queryMapper = requireNonNull(queryMapperV3);
    this.sortMapperV3 = requireNonNull(sortMapperV3);
    this.fetchMapper = requireNonNull(fetchMapper);
    this.entityTypeMetadata = requireNonNull(entityTypeMetadata);
  }

  public EntityTypes findEntityTypes(
      Query query, Selection filter, Selection expand, Sort sort, int size, int number) {
    Repository<org.molgenis.data.meta.model.EntityType> repository =
        metadataService
            .getRepository(
                EntityTypeMetadata.ENTITY_TYPE_META_DATA,
                org.molgenis.data.meta.model.EntityType.class)
            .orElseThrow(
                () -> new UnknownRepositoryException(EntityTypeMetadata.ENTITY_TYPE_META_DATA));
    Fetch fetch = fetchMapper.toFetch(repository.getEntityType(), filter, expand);

    org.molgenis.data.Query<org.molgenis.data.meta.model.EntityType> molgenisQuery =
        query != null ? queryMapper.map(query, repository) : new QueryImpl<>(repository);

    // get entities
    org.molgenis.data.Query<org.molgenis.data.meta.model.EntityType> findQuery =
        new QueryImpl<>(molgenisQuery);
    findQuery.fetch(fetch);
    findQuery.offset(number * size);
    findQuery.pageSize(size);
    findQuery.sort(sortMapperV3.map(sort));
    List<org.molgenis.data.meta.model.EntityType> entityTypes =
        repository.findAll(findQuery).collect(toList());

    org.molgenis.data.Query<org.molgenis.data.meta.model.EntityType> countQuery =
        new QueryImpl<>(molgenisQuery);
    countQuery.offset(0);
    countQuery.pageSize(Integer.MAX_VALUE);
    int count = Math.toIntExact(repository.count(countQuery));

    return EntityTypes.builder().setEntityTypes(entityTypes).setTotal(count).build();
  }

  public EntityType findEntityType(String identifier, Selection filter, Selection expand) {
    Repository<EntityType> repository =
        metadataService
            .getRepository(EntityTypeMetadata.ENTITY_TYPE_META_DATA, EntityType.class)
            .orElseThrow(
                () -> new UnknownRepositoryException(EntityTypeMetadata.ENTITY_TYPE_META_DATA));

    Fetch fetch = fetchMapper.toFetch(entityTypeMetadata, filter, expand);

    EntityType entityType = repository.findOneById(identifier, fetch);
    if (entityType == null) {
      throw new UnknownEntityException(EntityTypeMetadata.ENTITY_TYPE_META_DATA, identifier);
    }

    return entityType;
  }
}
