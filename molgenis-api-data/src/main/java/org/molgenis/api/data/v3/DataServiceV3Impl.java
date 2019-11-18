package org.molgenis.api.data.v3;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.data.QueryMapper;
import org.molgenis.api.data.SortMapper;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Selection;
import org.molgenis.api.model.Sort;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.QueryRule;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownRepositoryException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetadataAccessException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.util.EntityTypeUtils;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.data.validation.EntityErrors;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.data.validation.RepositoryConstraintViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DataServiceV3Impl implements DataServiceV3 {
  private final MetaDataService metaDataService;
  private final EntityManagerV3 entityManagerV3;
  private final QueryMapper queryMapper;
  private final SortMapper sortMapper;
  private final FetchMapper fetchMapper;
  private final EntityValidator entityValidator;

  private enum OperationType {
    READ,
    MODIFY
  }

  DataServiceV3Impl(
      MetaDataService metaDataService,
      EntityManagerV3 entityManagerV3,
      QueryMapper queryMapper,
      SortMapper sortMapper,
      FetchMapper fetchMapper,
      EntityValidator entityValidator) {
    this.metaDataService = requireNonNull(metaDataService);
    this.entityManagerV3 = requireNonNull(entityManagerV3);
    this.queryMapper = requireNonNull(queryMapper);
    this.sortMapper = requireNonNull(sortMapper);
    this.fetchMapper = requireNonNull(fetchMapper);
    this.entityValidator = requireNonNull(entityValidator);
  }

  @Transactional
  @Override
  public Entity create(String entityTypeId, Map<String, Object> requestValues) {
    Repository<Entity> repository = getRepository(entityTypeId, OperationType.MODIFY);
    EntityType entityType = repository.getEntityType();

    Entity entity = entityManagerV3.create(entityType);
    entityManagerV3.populate(entityType, entity, requestValues);

    validate(entity);
    repository.add(entity);
    return entity;
  }

  @Transactional(readOnly = true)
  @Override
  public Entity find(String entityTypeId, String entityId, Selection filter, Selection expand) {
    Repository<Entity> repository = getRepository(entityTypeId, OperationType.READ);
    EntityType entityType = repository.getEntityType();
    Object typedEntityId = toTypedEntityId(entityType, entityId);

    Fetch fetch = fetchMapper.toFetch(entityType, filter, expand);

    Entity entity = repository.findOneById(typedEntityId, fetch);
    if (entity == null) {
      throw new UnknownEntityException(entityTypeId, entityId);
    }

    return entity;
  }

  @Transactional(readOnly = true)
  @Override
  public Entities findSubresources(
      String entityTypeId,
      String entityId,
      String attributeName,
      @Nullable @CheckForNull Query query,
      Selection filter,
      Selection expand,
      Sort sort,
      int size,
      int number) {
    Repository<Entity> repository = getRepository(entityTypeId, OperationType.READ);
    EntityType entityType = repository.getEntityType();
    Object typedEntityId = toTypedEntityId(entityType, entityId);

    // Check if the attribute is suitable for this endpoint
    Attribute refAttribute = entityType.getAttribute(attributeName);
    if (refAttribute == null) {
      throw new UnknownAttributeException(entityType, attributeName);
    }
    if (!EntityTypeUtils.isMultipleReferenceType(refAttribute)) {
      throw new UnsupportedAttributeTypeException(refAttribute);
    }

    // get the mref ID's for this entity
    EntityType refEntityType = refAttribute.getRefEntity();
    Fetch refFetch = new Fetch().field(refEntityType.getIdAttribute().getName());
    Fetch fetch = new Fetch().field(refAttribute.getName(), refFetch);
    Entity entity = repository.findOneById(typedEntityId, fetch);
    if (entity == null) {
      throw new UnknownEntityException(refEntityType, typedEntityId);
    }
    List<Object> refEntityIds =
        StreamSupport.stream(entity.getEntities(attributeName).spliterator(), false)
            .map(Entity::getIdValue)
            .collect(toList());

    // Add 'in' query for the mref entity ID's
    Repository<Entity> refRepository = getRepository(refEntityType.getId(), OperationType.READ);
    org.molgenis.data.Query<Entity> findQuery =
        query != null ? queryMapper.map(query, refRepository) : new QueryImpl<>(refRepository);

    QueryImpl<Entity> q = new QueryImpl<>();
    if (!findQuery.getRules().isEmpty()) {
      q.nest();
      for (QueryRule rule : findQuery.getRules()) {
        q.addRule(rule);
      }
      q.unnest();
      q.and();
    }

    Entities result;
    if (!refEntityIds.isEmpty()) {
      q.in(refEntityType.getIdAttribute().getName(), refEntityIds);
      result = getEntities(filter, expand, sort, size, number, refRepository, q);
    } else {
      result = Entities.builder().setEntities(Collections.emptyList()).setTotal(0).build();
    }
    return result;
  }

  @Transactional(readOnly = true)
  @Override
  public Entities findAll(
      String entityTypeId,
      @Nullable @CheckForNull Query query,
      Selection filter,
      Selection expand,
      Sort sort,
      int size,
      int number) {
    Repository<Entity> repository = getRepository(entityTypeId, OperationType.READ);
    org.molgenis.data.Query<Entity> findQuery =
        query != null ? queryMapper.map(query, repository) : new QueryImpl<>(repository);

    return getEntities(filter, expand, sort, size, number, repository, findQuery);
  }

  private Entities getEntities(
      Selection filter,
      Selection expand,
      Sort sort,
      int size,
      int number,
      Repository<Entity> repository,
      org.molgenis.data.Query<Entity> query) {
    Fetch fetch = fetchMapper.toFetch(repository.getEntityType(), filter, expand);

    // get entities
    org.molgenis.data.Query<Entity> findQuery = new QueryImpl<>(query);
    findQuery.fetch(fetch);
    findQuery.offset(number * size);
    findQuery.pageSize(size);
    findQuery.sort(sortMapper.map(sort));
    List<Entity> entities = repository.findAll(findQuery).collect(toList());

    org.molgenis.data.Query<Entity> countQuery = new QueryImpl<>(query);
    countQuery.offset(0);
    countQuery.pageSize(Integer.MAX_VALUE);
    int count = Math.toIntExact(repository.count(countQuery));

    return Entities.builder().setEntities(entities).setTotal(count).build();
  }

  @Transactional
  @Override
  public void update(String entityTypeId, String entityId, Map<String, Object> requestValues) {
    Repository<Entity> repository = getRepository(entityTypeId, OperationType.MODIFY);
    EntityType entityType = repository.getEntityType();
    Object typedEntityId = toTypedEntityId(entityType, entityId);

    Entity entity = entityManagerV3.create(entityType);
    entityManagerV3.populate(entityType, entity, requestValues);
    entity.setIdValue(typedEntityId);

    validate(entity);

    repository.update(entity);
  }

  @Transactional
  @Override
  public void updatePartial(
      String entityTypeId, String entityId, Map<String, Object> requestValues) {
    Repository<Entity> repository = getRepository(entityTypeId, OperationType.MODIFY);
    EntityType entityType = repository.getEntityType();
    Object typedEntityId = toTypedEntityId(entityType, entityId);

    Entity entity = repository.findOneById(typedEntityId);
    if (entity == null) {
      throw new UnknownEntityException(entityTypeId, typedEntityId);
    }

    entityManagerV3.populate(entityType, entity, requestValues);
    entity.setIdValue(typedEntityId);

    validate(entity);

    repository.update(entity);
  }

  @Transactional
  @Override
  public void delete(String entityTypeId, String entityId) {
    Repository<Entity> repository = getRepository(entityTypeId, OperationType.MODIFY);
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
    Repository<Entity> repo = getRepository(entityTypeId, OperationType.MODIFY);
    org.molgenis.data.Query<Entity> molgenisQuery =
        query != null ? queryMapper.map(query, repo) : new QueryImpl<>(repo);
    repo.delete(molgenisQuery.findAll());
  }

  private Repository<Entity> getRepository(String entityTypeId, OperationType operation) {
    if (operation == OperationType.MODIFY
        && (entityTypeId.equals(ENTITY_TYPE_META_DATA)
            || entityTypeId.equals(ATTRIBUTE_META_DATA))) {
      throw new MetadataAccessException();
    }

    return metaDataService
        .getRepository(entityTypeId)
        .orElseThrow(() -> new UnknownRepositoryException(entityTypeId));
  }

  private Object toTypedEntityId(EntityType entityType, String entityId) {
    Attribute idAttribute = entityType.getIdAttribute();
    return EntityUtils.getTypedValue(entityId, idAttribute);
  }

  private void validate(Entity entity) {
    EntityErrors entityErrors = new EntityErrors(entity);
    entityValidator.validate(entity, entityErrors);
    if (entityErrors.hasErrors()) {
      throw new RepositoryConstraintViolationException(entityErrors);
    }
  }
}
