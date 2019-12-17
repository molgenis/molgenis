package org.molgenis.api.metadata.v3;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.data.QueryMapper;
import org.molgenis.api.data.SortMapper;
import org.molgenis.api.metadata.v3.exception.ZeroResultsException;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecution;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Sort;
import org.molgenis.data.Fetch;
import org.molgenis.data.QueryRule;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
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

  public EntityTypes findEntityTypes(
      @Nullable @CheckForNull Query query, Sort sort, int size, int number) {
    Repository<EntityType> repository = getEntityTypeRepository();

    org.molgenis.data.Query<EntityType> repositoryQuery =
        query != null ? queryMapper.map(query, repository) : new QueryImpl<>(repository);

    // get entities
    org.molgenis.data.Query<EntityType> findQuery = new QueryImpl<>(repositoryQuery);
    findQuery.offset(number * size);
    findQuery.pageSize(size);
    findQuery.sort(sortMapper.map(sort, repository.getEntityType()));
    List<EntityType> entityTypes = repository.findAll(findQuery).collect(toList());

    org.molgenis.data.Query<EntityType> countQuery = new QueryImpl<>(repositoryQuery);
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
      String entityTypeId, @Nullable @CheckForNull Query query, Sort sort, int size, int number) {
    if (!metadataService.hasEntityType(entityTypeId)) {
      throw new UnknownEntityTypeException(entityTypeId);
    }

    Repository<Attribute> repository = getAttributeRepository();
    org.molgenis.data.Query<Attribute> repositoryQuery =
        toAttributeRepositoryQuery(entityTypeId, query, repository);

    // get entities
    org.molgenis.data.Query<Attribute> findQuery = new QueryImpl<>(repositoryQuery);
    findQuery.offset(number * size);
    findQuery.pageSize(size);
    findQuery.sort(sortMapper.map(sort, repository.getEntityType()));
    List<Attribute> attributes = repository.findAll(findQuery).collect(toList());

    org.molgenis.data.Query<Attribute> countQuery = new QueryImpl<>(repositoryQuery);
    countQuery.offset(0);
    countQuery.pageSize(Integer.MAX_VALUE);
    int count = Math.toIntExact(repository.count(countQuery));

    return Attributes.builder().setAttributes(attributes).setTotal(count).build();
  }

  @Override
  public Attribute findAttribute(String entityTypeId, String attributeId) {
    EntityType entityType = findEntityType(entityTypeId);
    Attribute attribute = entityType.getOwnAttributeById(attributeId);
    if (attribute == null) {
      throw new UnknownAttributeException(entityType, attributeId);
    }
    return attribute;
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
    Repository<EntityType> entityTypeRepository = getEntityTypeRepository();

    org.molgenis.data.Query<EntityType> dataServiceQuery = queryMapper.map(q, entityTypeRepository);
    dataServiceQuery.setFetch(new Fetch().field(EntityTypeMetadata.ID));
    List<EntityType> entityTypes = dataServiceQuery.findAll().collect(toList());

    if (entityTypes.isEmpty()) {
      throw new ZeroResultsException(q);
    }
    return entityTypes;
  }

  private org.molgenis.data.Query<Attribute> toAttributeRepositoryQuery(
      String entityTypeId,
      @Nullable @CheckForNull Query query,
      Repository<Attribute> attributeRepository) {
    org.molgenis.data.Query<Attribute> repositoryQuery =
        query != null
            ? queryMapper.map(query, attributeRepository)
            : new QueryImpl<>(attributeRepository);

    boolean nest = repositoryQuery.getRules().size() > 1;
    if (nest) {
      // workaround for missing org.molgenis.data.Query.setRules method
      QueryRule nestedQueryRule = new QueryRule(new ArrayList<>(repositoryQuery.getRules()));
      QueryImpl<Attribute> nestedRepositoryQuery = new QueryImpl<>(nestedQueryRule);
      nestedRepositoryQuery.setPageSize(repositoryQuery.getPageSize());
      nestedRepositoryQuery.setOffset(repositoryQuery.getOffset());
      nestedRepositoryQuery.setSort(repositoryQuery.getSort());
      nestedRepositoryQuery.setFetch(repositoryQuery.getFetch());
      repositoryQuery = nestedRepositoryQuery;
    }
    if (!repositoryQuery.getRules().isEmpty()) {
      repositoryQuery.and();
    }
    repositoryQuery.eq(AttributeMetadata.ENTITY, entityTypeId);
    return repositoryQuery;
  }

  private List<Attribute> findAttributes(String entityTypeId, Query q) {
    org.molgenis.data.Query<Attribute> repositoryQuery =
        toAttributeRepositoryQuery(entityTypeId, q, getAttributeRepository());
    repositoryQuery.setFetch(new Fetch().field(AttributeMetadata.ID));
    List<Attribute> attributes = repositoryQuery.findAll().collect(toList());
    if (attributes.isEmpty()) {
      throw new ZeroResultsException(q);
    }
    return attributes;
  }

  private Repository<EntityType> getEntityTypeRepository() {
    return metadataService
        .getRepository(ENTITY_TYPE_META_DATA, EntityType.class)
        .orElseThrow(() -> new UnknownRepositoryException(ENTITY_TYPE_META_DATA));
  }

  private Repository<Attribute> getAttributeRepository() {
    return metadataService
        .getRepository(ATTRIBUTE_META_DATA, Attribute.class)
        .orElseThrow(() -> new UnknownRepositoryException(ATTRIBUTE_META_DATA));
  }
}
