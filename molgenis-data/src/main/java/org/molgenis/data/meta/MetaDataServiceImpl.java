package org.molgenis.data.meta;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.AttributeMetadata.REF_ENTITY_TYPE;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.EXTENDS;
import static org.molgenis.data.meta.model.EntityTypeMetadata.IS_ABSTRACT;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.meta.model.PackageMetadata.PARENT;
import static org.molgenis.data.meta.model.TagMetadata.TAG;
import static org.molgenis.data.util.EntityTypeUtils.getEntityTypeFetch;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryCollectionRegistry;
import org.molgenis.data.RepositoryCreationException;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.UnknownRepositoryCollectionException;
import org.molgenis.data.UnknownRepositoryException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.meta.persist.PackagePersister;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Meta data service for retrieving and editing meta data. */
@Component
public class MetaDataServiceImpl implements MetaDataService {
  private static final Logger LOG = LoggerFactory.getLogger(MetaDataServiceImpl.class);

  private final DataService dataService;
  private final RepositoryCollectionRegistry repoCollectionRegistry;
  private final SystemEntityTypeRegistry systemEntityTypeRegistry;
  private final EntityTypeDependencyResolver entityTypeDependencyResolver;
  private final PackagePersister packagePersister;

  MetaDataServiceImpl(
      DataService dataService,
      RepositoryCollectionRegistry repoCollectionRegistry,
      SystemEntityTypeRegistry systemEntityTypeRegistry,
      EntityTypeDependencyResolver entityTypeDependencyResolver,
      PackagePersister packagePersister) {
    this.dataService = requireNonNull(dataService);
    this.repoCollectionRegistry = requireNonNull(repoCollectionRegistry);
    this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
    this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
    this.packagePersister = requireNonNull(packagePersister);
  }

  @Override
  public Optional<Repository<Entity>> getRepository(String entityTypeId) {
    EntityType entityType =
        getEntityType(entityTypeId).orElseThrow(() -> new UnknownEntityTypeException(entityTypeId));
    return !entityType.isAbstract() ? getRepository(entityType) : Optional.empty();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <E extends Entity> Optional<Repository<E>> getRepository(
      String entityTypeId, Class<E> entityClass) {
    return (Optional<Repository<E>>) (Optional<?>) getRepository(entityTypeId);
  }

  @Override
  public Optional<Repository<Entity>> getRepository(EntityType entityType) {
    if (!entityType.isAbstract()) {
      String backendName = entityType.getBackend();
      RepositoryCollection backend = getBackend(backendName);
      Repository<Entity> repository = backend.getRepository(entityType);
      return repository != null ? Optional.of(repository) : Optional.empty();
    } else {
      return Optional.empty();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <E extends Entity> Optional<Repository<E>> getRepository(
      EntityType entityType, Class<E> entityClass) {
    return (Optional<Repository<E>>) (Optional<?>) getRepository(entityType);
  }

  @Override
  public boolean hasRepository(String entityTypeId) {
    SystemEntityType systemEntityType = systemEntityTypeRegistry.getSystemEntityType(entityTypeId);
    if (systemEntityType != null) {
      return !systemEntityType.isAbstract();
    } else {
      return dataService
              .query(ENTITY_TYPE_META_DATA, EntityType.class)
              .eq(EntityTypeMetadata.ID, entityTypeId)
              .and()
              .eq(IS_ABSTRACT, false)
              .findOne()
          != null;
    }
  }

  @Transactional
  @Override
  public Repository<Entity> createRepository(EntityType entityType) {
    if (entityType.isAbstract()) {
      throw new RepositoryCreationException(entityType);
    }
    addEntityType(entityType);
    return getRepository(entityType)
        .orElseThrow(() -> new UnknownRepositoryException(entityType.getId()));
  }

  @Transactional
  @Override
  public <E extends Entity> Repository<E> createRepository(
      EntityType entityType, Class<E> entityClass) {
    if (entityType.isAbstract()) {
      throw new RepositoryCreationException(entityType);
    }
    addEntityType(entityType);
    return getRepository(entityType, entityClass)
        .orElseThrow(() -> new UnknownRepositoryException(entityType.getId()));
  }

  @Override
  public RepositoryCollection getDefaultBackend() {
    return repoCollectionRegistry.getDefaultRepoCollection();
  }

  @Override
  public RepositoryCollection getBackend(String backendName) {
    RepositoryCollection repositoryCollection =
        repoCollectionRegistry.getRepositoryCollection(backendName);
    if (repositoryCollection == null) {
      throw new UnknownRepositoryCollectionException(backendName);
    }
    return repositoryCollection;
  }

  @Transactional
  @Override
  public void deleteEntityType(String entityTypeId) {
    dataService.deleteById(ENTITY_TYPE_META_DATA, entityTypeId);

    LOG.info("Removed entity [{}]", entityTypeId);
  }

  @Transactional
  @Override
  public void deleteEntityType(Collection<EntityType> entityTypes) {
    if (entityTypes.isEmpty()) {
      return;
    }

    dataService.delete(ENTITY_TYPE_META_DATA, entityTypes.stream());

    if (LOG.isInfoEnabled()) {
      LOG.info(
          "Removed entities [{}]",
          entityTypes.stream().map(EntityType::getId).collect(joining(",")));
    }
  }

  @Transactional
  @Override
  public void deleteAttributeById(Object id) {
    Attribute attribute = dataService.findOneById(ATTRIBUTE_META_DATA, id, Attribute.class);
    if (attribute == null) {
      throw new UnknownEntityException(ATTRIBUTE_META_DATA, id);
    }
    EntityType entityType = attribute.getEntity();

    // Update repository state
    entityType.removeAttribute(attribute);

    // Update repository state
    dataService.update(ENTITY_TYPE_META_DATA, entityType);

    // Update administration
    dataService.delete(ATTRIBUTE_META_DATA, attribute);
  }

  @Override
  public RepositoryCollection getBackend(EntityType entityType) {
    String backendName =
        entityType.getBackend() == null ? getDefaultBackend().getName() : entityType.getBackend();
    RepositoryCollection backend = repoCollectionRegistry.getRepositoryCollection(backendName);
    if (backend == null) {
      throw new UnknownRepositoryCollectionException(backendName);
    }

    return backend;
  }

  @Transactional
  @Override
  public void addEntityType(EntityType entityType) {
    // create entity
    dataService.add(ENTITY_TYPE_META_DATA, entityType);

    // create attributes
    Stream<Attribute> attrs = stream(entityType.getOwnAllAttributes());
    dataService.add(ATTRIBUTE_META_DATA, attrs);
  }

  @Transactional
  @Override
  public void updateEntityType(EntityType entityType) {
    EntityType existingEntityType =
        dataService
            .query(ENTITY_TYPE_META_DATA, EntityType.class)
            .eq(EntityTypeMetadata.ID, entityType.getId())
            .fetch(getEntityTypeFetch())
            .findOne();
    if (existingEntityType == null) {
      throw new UnknownEntityTypeException(entityType.getId());
    }

    updateEntityType(entityType, existingEntityType);
  }

  /**
   * Returns true if entity meta contains mapped by attributes that do not exist in the existing
   * entity meta.
   *
   * @param entityType entity meta data
   * @param existingEntityType existing entity meta data
   * @return true if entity meta contains mapped by attributes that do not exist in the existing
   *     entity meta.
   */
  private static boolean hasNewMappedByAttrs(EntityType entityType, EntityType existingEntityType) {
    Set<String> mappedByAttrs =
        entityType.getOwnMappedByAttributes().map(Attribute::getName).collect(toSet());

    Set<String> existingMappedByAttrs =
        existingEntityType.getOwnMappedByAttributes().map(Attribute::getName).collect(toSet());
    return !mappedByAttrs.equals(existingMappedByAttrs);
  }

  @Transactional
  @Override
  public void upsertEntityTypes(Collection<EntityType> entityTypes) {
    if (entityTypes.isEmpty()) {
      return;
    }

    List<EntityType> resolvedEntityTypes = entityTypeDependencyResolver.resolve(entityTypes);

    Map<String, EntityType> existingEntityTypeMap = getExistingEntityTypeMap(entityTypes);
    upsertEntityTypesSkipMappedByAttributes(resolvedEntityTypes, existingEntityTypeMap);
    addMappedByAttributes(resolvedEntityTypes, existingEntityTypeMap);
  }

  private Map<String, EntityType> getExistingEntityTypeMap(Collection<EntityType> entityTypes) {
    Map<String, EntityType> existingEntityTypeMap = new HashMap<>();
    entityTypes.forEach(
        entityType -> {
          String entityId = entityType.getId();
          if (entityId != null) {
            EntityType existingEntityType =
                dataService.findOneById(ENTITY_TYPE_META_DATA, entityId, EntityType.class);

            if (existingEntityType != null) {
              existingEntityTypeMap.put(entityType.getId(), existingEntityType);
            }
          }
        });
    return existingEntityTypeMap;
  }

  private void addMappedByAttributes(
      List<EntityType> resolvedEntityTypes, Map<String, EntityType> existingEntityTypeMap) {
    // 2nd pass: create mappedBy attributes and update entity
    resolvedEntityTypes.forEach(
        entityType -> {
          EntityType existingEntityType = existingEntityTypeMap.get(entityType.getId());
          if (existingEntityType == null) {
            if (entityType.hasMappedByAttributes()) {
              updateEntityType(entityType, new EntityTypeWithoutMappedByAttributes(entityType));
            }
          } else {
            if (hasNewMappedByAttrs(entityType, existingEntityType)) {
              updateEntityType(entityType, existingEntityType);
            }
          }
        });
  }

  private void upsertEntityTypesSkipMappedByAttributes(
      List<EntityType> resolvedEntityType, Map<String, EntityType> existingEntityTypeMap) {
    // 1st pass: create entities and attributes except for mappedBy attributes
    resolvedEntityType.forEach(
        entityType -> {
          EntityType existingEntityType = existingEntityTypeMap.get(entityType.getId());
          if (existingEntityType == null) {
            if (entityType.hasMappedByAttributes()) {
              entityType = new EntityTypeWithoutMappedByAttributes(entityType);
            }

            addEntityType(entityType);
          } else {
            if (hasNewMappedByAttrs(entityType, existingEntityType)) {
              entityType = new EntityTypeWithoutMappedByAttributes(entityType, existingEntityType);
            }

            updateEntityType(entityType, existingEntityType);
          }
        });
  }

  private void updateEntityType(EntityType entityType, EntityType existingEntityType) {
    // update entity
    if (!EntityUtils.equals(entityType, existingEntityType)) {
      // note: leave it up to the data service to decided what to do with attributes removed from
      // entity meta data
      dataService.update(ENTITY_TYPE_META_DATA, entityType);
    }
    // add new attributes, update modified attributes
    upsertAttributes(entityType, existingEntityType);
  }

  @Transactional
  @Override
  public void addAttribute(Attribute attr) {
    EntityType entityType = dataService.getEntityType(attr.getEntity().getId());
    if (entityType == null) {
      throw new UnknownEntityTypeException(attr.getEntity().getId());
    }
    entityType.addAttribute(attr);

    // Update repository state
    dataService.update(ENTITY_TYPE_META_DATA, entityType);

    // Update administration
    dataService.add(ATTRIBUTE_META_DATA, attr);
  }

  @Override
  public boolean hasEntityType(String entityTypeId) {
    return systemEntityTypeRegistry.hasSystemEntityType(entityTypeId)
        || getEntityTypeBypassingRegistry(entityTypeId) != null;
  }

  @Override
  public Optional<EntityType> getEntityType(String entityTypeId) {
    EntityType entityType = systemEntityTypeRegistry.getSystemEntityType(entityTypeId);
    if (entityType != null) {
      return Optional.of(entityType);
    } else {
      entityType = getEntityTypeBypassingRegistry(entityTypeId);
      return entityType != null ? Optional.of(entityType) : Optional.empty();
    }
  }

  @Transactional
  @Override
  public void addPackage(Package aPackage) {
    dataService.add(PACKAGE, aPackage);
  }

  @Transactional
  @Override
  public void upsertPackages(Stream<Package> packages) {
    packagePersister.upsertPackages(packages);
  }

  @Override
  public Optional<Package> getPackage(String packageId) {
    Package aPackage = dataService.findOneById(PACKAGE, packageId, Package.class);
    return aPackage != null ? Optional.of(aPackage) : Optional.empty();
  }

  @Override
  public List<Package> getPackages() {
    return dataService.findAll(PACKAGE, Package.class).collect(toList());
  }

  @Override
  public List<Package> getRootPackages() {
    return dataService.query(PACKAGE, Package.class).eq(PARENT, null).findAll().collect(toList());
  }

  @Transactional
  @Override
  public void upsertTags(Collection<Tag> tags) {
    // TODO replace with dataService.upsert once available in Repository
    tags.forEach(
        tag -> {
          Tag existingTag = dataService.findOneById(TAG, tag.getId(), Tag.class);
          if (existingTag == null) {
            dataService.add(TAG, tag);
          } else {
            dataService.update(TAG, tag);
          }
        });
  }

  @Override
  public Stream<EntityType> getEntityTypes() {
    List<EntityType> entityTypeList = newArrayList();
    Fetch entityTypeFetch = getEntityTypeFetch();

    // Fetch the entitytypes page by page so that the results can be cached
    final int pageSize = 1000;
    for (int page = 0; entityTypeList.size() == page * pageSize; page++) {
      QueryImpl<EntityType> query = new QueryImpl<>();
      query.setFetch(entityTypeFetch);
      query.setPageSize(pageSize);
      query.setOffset(page * pageSize);
      dataService
          .findAll(ENTITY_TYPE_META_DATA, query, EntityType.class)
          .forEach(entityTypeList::add);
    }

    return entityTypeList.stream();
  }

  @Override
  public Stream<Repository<Entity>> getRepositories() {
    return dataService
        .query(ENTITY_TYPE_META_DATA, EntityType.class)
        .eq(IS_ABSTRACT, false)
        .fetch(getEntityTypeFetch())
        .findAll()
        .map(
            entityType ->
                this.getRepository(entityType)
                    .orElseThrow(() -> new UnknownRepositoryException(entityType.getId())));
  }

  /**
   * Add and update entity attributes
   *
   * @param entityType entity meta data
   * @param existingEntityType existing entity meta data
   */
  private void upsertAttributes(EntityType entityType, EntityType existingEntityType) {
    // analyze both compound and atomic attributes owned by the entity
    Map<String, Attribute> attrsMap =
        stream(entityType.getOwnAllAttributes())
            .collect(toMap(Attribute::getName, Function.identity()));
    Map<String, Attribute> existingAttrsMap =
        stream(existingEntityType.getOwnAllAttributes())
            .collect(toMap(Attribute::getName, Function.identity()));

    // determine attributes to add, update and delete
    Set<String> addedAttrNames = Sets.difference(attrsMap.keySet(), existingAttrsMap.keySet());
    Set<String> sharedAttrNames = Sets.intersection(attrsMap.keySet(), existingAttrsMap.keySet());
    Set<String> deletedAttrNames = Sets.difference(existingAttrsMap.keySet(), attrsMap.keySet());

    // add new attributes
    if (!addedAttrNames.isEmpty()) {
      dataService.add(ATTRIBUTE_META_DATA, addedAttrNames.stream().map(attrsMap::get));
    }

    // update changed attributes
    List<String> updatedAttrNames =
        sharedAttrNames
            .stream()
            .filter(
                attrName ->
                    !EntityUtils.equals(attrsMap.get(attrName), existingAttrsMap.get(attrName)))
            .collect(toList());
    if (!updatedAttrNames.isEmpty()) {
      dataService.update(ATTRIBUTE_META_DATA, updatedAttrNames.stream().map(attrsMap::get));
    }

    // delete removed attributes
    if (!deletedAttrNames.isEmpty()) {
      dataService.delete(ATTRIBUTE_META_DATA, deletedAttrNames.stream().map(existingAttrsMap::get));
    }
  }

  @Override
  public @Nonnull Iterator<RepositoryCollection> iterator() {
    return repoCollectionRegistry.getRepositoryCollections().iterator();
  }

  @Override
  public Map<String, Boolean> determineImportableEntities(
      RepositoryCollection repositoryCollection) {
    LinkedHashMap<String, Boolean> entitiesImportable = Maps.newLinkedHashMap();
    stream(repositoryCollection.getEntityTypeIds())
        .forEach(
            id ->
                entitiesImportable.put(
                    id,
                    this.isEntityTypeCompatible(
                        repositoryCollection.getRepository(id).getEntityType())));

    return entitiesImportable;
  }

  @Override
  public boolean isEntityTypeCompatible(EntityType newEntityType) {
    String newEntityTypeId = newEntityType.getId();
    if (dataService.hasRepository(newEntityTypeId)) {
      EntityType oldEntityType = dataService.getEntityType(newEntityTypeId);
      if (oldEntityType == null) {
        throw new UnknownEntityTypeException(newEntityTypeId);
      }
      List<Attribute> oldAtomicAttributes =
          stream(oldEntityType.getAtomicAttributes()).collect(toList());

      LinkedHashMap<String, Attribute> newAtomicAttributesMap = newLinkedHashMap();
      stream(newEntityType.getAtomicAttributes())
          .forEach(attribute -> newAtomicAttributesMap.put(attribute.getName(), attribute));

      for (Attribute oldAttribute : oldAtomicAttributes) {
        if (!newAtomicAttributesMap.keySet().contains(oldAttribute.getName())) return false;
        // FIXME This implies that an attribute can never be different when doing an update import?
        if (!EntityUtils.equals(
            oldAttribute, newAtomicAttributesMap.get(oldAttribute.getName()), false)) return false;
      }
    }
    return true;
  }

  @Override
  public boolean hasBackend(String backendName) {
    return repoCollectionRegistry.hasRepositoryCollection(backendName);
  }

  @Override
  public Stream<EntityType> getConcreteChildren(EntityType entityType) {
    if (!entityType.isAbstract()) {
      return Stream.of(entityType);
    }
    return dataService
        .query(ENTITY_TYPE_META_DATA, EntityType.class)
        .eq(EXTENDS, entityType)
        .findAll()
        .flatMap(this::getConcreteChildren);
  }

  /**
   * Retrieves EntityType, bypassing the {@link
   * org.molgenis.data.meta.system.SystemEntityTypeRegistry}
   *
   * <p>package-private for testability
   */
  EntityType getEntityTypeBypassingRegistry(String entityTypeId) {
    return entityTypeId != null
        ? dataService.findOneById(
            ENTITY_TYPE_META_DATA, entityTypeId, getEntityTypeFetch(), EntityType.class)
        : null;
  }

  @Override
  public Stream<Attribute> getReferringAttributes(String entityTypeId) {
    return dataService
        .query(ATTRIBUTE_META_DATA, Attribute.class)
        .eq(REF_ENTITY_TYPE, entityTypeId)
        .findAll();
  }
}
