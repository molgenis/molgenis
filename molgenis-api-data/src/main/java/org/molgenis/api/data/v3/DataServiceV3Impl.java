package org.molgenis.api.data.v3;

import static java.util.Objects.requireNonNull;
import static org.molgenis.api.data.v3.SortV3Mapper.map;
import static org.molgenis.api.model.Selection.EMPTY_SELECTION;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Selection;
import org.molgenis.api.model.Sort;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.UnknownRepositoryException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.util.EntityTypeUtils;
import org.molgenis.data.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DataServiceV3Impl implements DataServiceV3 {
  private final MetaDataService metaDataService;
  private final EntityManagerV3 entityManagerV3;
  private final QueryV3Mapper queryMapperV3;

  DataServiceV3Impl(
      MetaDataService metaDataService,
      EntityManagerV3 entityManagerV3,
      QueryV3Mapper queryMapperV3) {
    this.metaDataService = requireNonNull(metaDataService);
    this.entityManagerV3 = requireNonNull(entityManagerV3);
    this.queryMapperV3 = requireNonNull(queryMapperV3);
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

    Fetch fetch = toFetch(entityType, filter, expand);

    Entity entity = repository.findOneById(typedEntityId, fetch);
    if (entity == null) {
      throw new UnknownEntityException(entityTypeId, entityId);
    }

    return entity;
  }

  @Transactional
  @Override
  public void update(String entityTypeId, String entityId, Map<String, Object> requestValues) {
    Repository<Entity> repository = getRepository(entityTypeId);
    EntityType entityType = repository.getEntityType();
    Object typedEntityId = toTypedEntityId(entityType, entityId);

    Entity entity = entityManagerV3.create(entityType); // what happens with auto values?
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
  public void delete(String entityTypeId, Query query) {
    Repository<Entity> repo =
        metaDataService
            .getRepository(entityTypeId)
            .orElseThrow(() -> new UnknownEntityTypeException(entityTypeId));
    org.molgenis.data.Query molgenisQuery = queryMapperV3.map(query, repo);
    repo.delete(molgenisQuery.findAll());
  }

  @Override
  public List<Entity> find(
      String entityTypeId,
      Query q,
      Sort sort,
      Selection filter,
      Selection expand,
      int size,
      int number) {
    Repository<Entity> repo =
        metaDataService
            .getRepository(entityTypeId)
            .orElseThrow(() -> new UnknownEntityTypeException(entityTypeId));

    Fetch fetch = toFetch(repo.getEntityType(), filter, expand);

    org.molgenis.data.Query<Entity> query = queryMapperV3.map(q, repo);
    query.fetch(fetch);
    query.offset(number * size);
    query.pageSize(size);

    query.sort(map(sort));

    return query.findAll().collect(Collectors.toList());
  }

  @Override
  public int count(String entityTypeId, Query q) {
    QueryV3Mapper queryV3Mapper = new QueryV3Mapper();
    Repository<Entity> repo =
        metaDataService
            .getRepository(entityTypeId)
            .orElseThrow(() -> new UnknownEntityTypeException(entityTypeId));
    org.molgenis.data.Query<Entity> query = queryV3Mapper.map(q, repo);

    query.offset(0);
    query.pageSize(Integer.MAX_VALUE);

    return Math.toIntExact(query.count());
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

  private @CheckForNull @Nullable Fetch toFetch(
      EntityType entityType, Selection filter, Selection expand) {
    if (!filter.hasItems()) {
      return null;
    }

    Fetch fetch = new Fetch();

    Iterable<Attribute> attributes = entityType.getAtomicAttributes();
    attributes.forEach(
        attribute -> {
          String attributeName = attribute.getName();

          if (filter.hasItem(attributeName)) {
            Fetch subFetch;
            if (expand.hasItem(attributeName) && EntityTypeUtils.isReferenceType(attribute)) {
              Selection subFilter = filter.getSelection(attributeName).orElse(EMPTY_SELECTION);
              Selection subExpand = expand.getSelection(attributeName).orElse(EMPTY_SELECTION);
              subFetch = toFetch(attribute.getRefEntity(), subFilter, subExpand);
            } else {
              subFetch = null;
            }

            fetch.field(attributeName, subFetch);
          }
        });

    return fetch;
  }
}
