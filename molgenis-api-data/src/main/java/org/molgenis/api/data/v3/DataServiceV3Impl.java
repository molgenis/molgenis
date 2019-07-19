package org.molgenis.api.data.v3;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.api.data.v3.SortV3Mapper.map;

import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Selection;
import org.molgenis.api.model.Sort;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownRepositoryException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DataServiceV3Impl implements DataServiceV3 {
  private final MetaDataService metaDataService;
  private final EntityManagerV3 entityManagerV3;
  private final QueryV3Mapper queryMapperV3;
  private final FetchMapper fetchMapper;

  DataServiceV3Impl(
      MetaDataService metaDataService,
      EntityManagerV3 entityManagerV3,
      QueryV3Mapper queryMapperV3,
      FetchMapper fetchMapper) {
    this.metaDataService = requireNonNull(metaDataService);
    this.entityManagerV3 = requireNonNull(entityManagerV3);
    this.queryMapperV3 = requireNonNull(queryMapperV3);
    this.fetchMapper = requireNonNull(fetchMapper);
  }

  @Transactional
  @Override
  public Entity create(String entityTypeId, Map<String, Object> requestValues) {
    Repository<Entity> repository = getRepository(entityTypeId);
    EntityType entityType = repository.getEntityType();

    Entity entity = entityManagerV3.create(entityType);
    entityManagerV3.populate(entityType, entity, requestValues);

    repository.add(entity);
    return entity;
  }

  @Transactional(readOnly = true)
  @Override
  public Entity find(String entityTypeId, String entityId, Selection filter, Selection expand) {
    Repository<Entity> repository = getRepository(entityTypeId);
    EntityType entityType = repository.getEntityType();
    Object typedEntityId = toTypedEntityId(entityType, entityId);

    Fetch fetch = fetchMapper.toFetch(entityType, filter, expand);

    Entity entity = repository.findOneById(typedEntityId, fetch);
    if (entity == null) {
      throw new UnknownEntityException(entityTypeId, entityId);
    }

    return entity;
  }

  @Override
  public Entities findAll(
      String entityTypeId,
      @Nullable @CheckForNull Query query,
      Selection filter,
      Selection expand,
      Sort sort,
      int size,
      int number) {
    Repository<Entity> repository = getRepository(entityTypeId);

    Fetch fetch = fetchMapper.toFetch(repository.getEntityType(), filter, expand);

    // get entities
    org.molgenis.data.Query<Entity> findQuery =
        query != null ? queryMapperV3.map(query, repository) : new QueryImpl<>(repository);
    findQuery.fetch(fetch);
    findQuery.offset(number * size);
    findQuery.pageSize(size);
    findQuery.sort(map(sort));
    List<Entity> entities = repository.findAll(findQuery).collect(toList());

    // get total entity count
    org.molgenis.data.Query<Entity> countQuery =
        query != null ? queryMapperV3.map(query, repository) : new QueryImpl<>(repository);
    countQuery.offset(0);
    countQuery.pageSize(Integer.MAX_VALUE);
    int count = Math.toIntExact(repository.count(countQuery));

    return Entities.builder().setEntities(entities).setTotal(count).build();
  }

  @Transactional
  @Override
  public void update(String entityTypeId, String entityId, Map<String, Object> requestValues) {
    Repository<Entity> repository = getRepository(entityTypeId);
    EntityType entityType = repository.getEntityType();
    Object typedEntityId = toTypedEntityId(entityType, entityId);

    Entity entity = entityManagerV3.create(entityType); // TODO check what happens with auto values
    entityManagerV3.populate(entityType, entity, requestValues);
    entity.setIdValue(typedEntityId);

    repository.update(entity);
  }

  @Transactional
  @Override
  public void updatePartial(
      String entityTypeId, String entityId, Map<String, Object> requestValues) {
    Repository<Entity> repository = getRepository(entityTypeId);
    EntityType entityType = repository.getEntityType();
    Object typedEntityId = toTypedEntityId(entityType, entityId);

    Entity entity = repository.findOneById(typedEntityId);
    if (entity == null) {
      throw new UnknownEntityException(entityTypeId, typedEntityId);
    }

    entityManagerV3.populate(entityType, entity, requestValues);
    entity.setIdValue(typedEntityId);

    repository.update(entity);
  }

  @Transactional
  @Override
  public void delete(String entityTypeId, String entityId) {
    Repository<Entity> repository = getRepository(entityTypeId);
    EntityType entityType = repository.getEntityType();
    Object typedEntityId = toTypedEntityId(entityType, entityId);

    // repository.deleteById succeeds if entity doesn't exist, so check for existence first
    Fetch idFetch = new Fetch().field(entityType.getIdAttribute().getName());
    Entity entity = repository.findOneById(typedEntityId, idFetch);
    if (entity == null) {
      throw new UnknownEntityException(entityTypeId, entityId);
    }

    repository.deleteById(typedEntityId);
  }

  @Transactional
  @Override
  public void deleteAll(String entityTypeId, @Nullable @CheckForNull Query query) {
    Repository<Entity> repo = getRepository(entityTypeId);
    org.molgenis.data.Query<Entity> molgenisQuery =
        query != null ? queryMapperV3.map(query, repo) : new QueryImpl<>(repo);
    repo.delete(molgenisQuery.findAll());
  }

  private Repository<Entity> getRepository(String entityTypeId) {
    return metaDataService
        .getRepository(entityTypeId)
        .orElseThrow(() -> new UnknownRepositoryException(entityTypeId));
  }

  private Object toTypedEntityId(EntityType entityType, String entityId) {
    Attribute idAttribute = entityType.getIdAttribute();
    return EntityUtils.getTypedValue(entityId, idAttribute);
  }
}
