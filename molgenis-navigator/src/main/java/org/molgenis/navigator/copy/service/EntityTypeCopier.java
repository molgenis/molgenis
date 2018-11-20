package org.molgenis.navigator.copy.service;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.navigator.copy.service.RelationTransformer.transformExtends;
import static org.molgenis.navigator.copy.service.RelationTransformer.transformMappedBys;
import static org.molgenis.navigator.copy.service.RelationTransformer.transformPackage;
import static org.molgenis.navigator.copy.service.RelationTransformer.transformRefEntities;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.molgenis.data.AbstractEntityDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaMetaData;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.util.EntityTypeUtils;
import org.springframework.stereotype.Component;

@Component
class EntityTypeCopier {

  private static final int BATCH_SIZE = 1000;

  private final DataService dataService;
  private final IdGenerator idGenerator;
  private final EntityTypeDependencyResolver entityTypeDependencyResolver;
  private final EntityTypeMetadataCopier entityTypeMetadataCopier;

  EntityTypeCopier(
      DataService dataService,
      IdGenerator idGenerator,
      EntityTypeDependencyResolver entityTypeDependencyResolver,
      EntityTypeMetadataCopier entityTypeMetadataCopier) {
    this.dataService = requireNonNull(dataService);
    this.idGenerator = requireNonNull(idGenerator);
    this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
    this.entityTypeMetadataCopier = requireNonNull(entityTypeMetadataCopier);
  }

  public void copy(List<EntityType> entityTypes, CopyState state) {
    entityTypes.forEach(entityType -> assignUniqueLabel(entityType, state));
    List<EntityType> copiedEntityTypes = copyEntityTypes(entityTypes, state);
    persist(copiedEntityTypes, state);
  }

  private List<EntityType> copyEntityTypes(List<EntityType> entityTypes, CopyState state) {
    List<EntityType> copiedEntityTypes =
        concat(entityTypes.stream(), state.entityTypesInPackages().stream())
            .map(original -> copyEntityType(original, state))
            .collect(toList());

    copiedEntityTypes.forEach(entityType -> transformRelations(entityType, state));
    return copiedEntityTypes;
  }

  private void persist(List<EntityType> copiedEntityTypes, CopyState state) {
    entityTypeDependencyResolver
        .resolve(copiedEntityTypes)
        .stream()
        .map(copy -> cutDefaultValues(copy, state))
        .map(this::persistEntityType)
        .collect(toList())
        .stream()
        .map(copy -> copyEntities(copy, state))
        .map(copy -> pasteDefaultValues(copy, state))
        .forEach(e -> state.progress().increment(1));
  }

  /**
   * Checks if there's an EntityType in the target location with the same label. If so, keeps adding
   * a postfix until the label is unique.
   */
  private void assignUniqueLabel(EntityType entityType, CopyState state) {
    Set<String> existingLabels;
    if (state.targetPackage() != null) {
      //noinspection ConstantConditions
      existingLabels =
          stream(state.targetPackage().getEntityTypes()).map(EntityType::getLabel).collect(toSet());
    } else {
      existingLabels =
          dataService
              .query(ENTITY_TYPE_META_DATA, EntityType.class)
              .eq(EntityTypeMetadata.PACKAGE, null)
              .findAll()
              .map(EntityType::getLabel)
              .collect(toSet());
    }
    entityType.setLabel(LabelGenerator.generateUniqueLabel(entityType.getLabel(), existingLabels));
  }

  private EntityType copyEntityType(EntityType original, CopyState state) {
    EntityType copy = entityTypeMetadataCopier.copy(original, state);
    String newId = idGenerator.generateId();
    copy.setId(newId);

    state.copiedEntityTypes().put(original.getId(), copy);
    state.originalEntityTypeIds().put(newId, original.getId());

    return copy;
  }

  private void transformRelations(EntityType entityType, CopyState state) {
    transformPackage(entityType, state.copiedPackages());
    transformExtends(entityType, state.copiedEntityTypes());
    transformRefEntities(entityType, state.copiedEntityTypes());
    transformMappedBys(entityType, state.copiedAttributes());
  }

  private EntityType cutDefaultValues(EntityType copy, CopyState state) {
    stream(copy.getAtomicAttributes())
        .filter(EntityTypeUtils::isReferenceType)
        .filter(attr -> attr.getDefaultValue() != null)
        .forEach(
            attr -> {
              state.referenceDefaultValues().put(attr.getIdentifier(), attr.getDefaultValue());
              attr.setDefaultValue(null);
            });
    return copy;
  }

  private EntityType pasteDefaultValues(EntityType copy, CopyState state) {
    stream(copy.getAtomicAttributes())
        .filter(EntityTypeUtils::isReferenceType)
        .forEach(
            attr ->
                attr.setDefaultValue(
                    state.referenceDefaultValues().getOrDefault(attr.getIdentifier(), null)));

    dataService.getMeta().updateEntityType(copy);
    return copy;
  }

  private EntityType persistEntityType(EntityType copy) {
    dataService.getMeta().addEntityType(copy);
    return copy;
  }

  private EntityType copyEntities(EntityType copy, CopyState state) {
    String originalEntityTypeId = state.originalEntityTypeIds().get(copy.getId());
    if (!copy.isAbstract()) {
      dataService
          .getRepository(originalEntityTypeId)
          .forEachBatched(batch -> addEntityBatch(copy, state, batch), BATCH_SIZE);
    }
    return copy;
  }

  private void addEntityBatch(EntityType copy, CopyState state, List<Entity> batch) {
    dataService.add(
        copy.getId(),
        batch.stream().map(entity -> new PretendingEntity(entity, state.copiedEntityTypes())));
  }

  /**
   * When copying rows from one repository to another, the metadata of these entities will not fit
   * the copied repository and its references because the metadatas will have different IDs. The
   * PretendingEntity acts like it's the newly copied entity by returning the metadata of the copied
   * repository instead of the original.
   */
  private class PretendingEntity extends AbstractEntityDecorator {

    private final Map<String, EntityType> copiedEntityTypes;

    PretendingEntity(Entity entity, Map<String, EntityType> copiedEntityTypes) {
      super(entity);
      this.copiedEntityTypes = copiedEntityTypes;
    }

    @Override
    public EntityType getEntityType() {
      if (delegate().getEntityType() != null) {
        String id = delegate().getEntityType().getId();
        if (copiedEntityTypes.containsKey(id)) {
          return copiedEntityTypes.get(id);
        } else {
          return delegate().getEntityType();
        }
      } else {
        return null;
      }
    }

    @Override
    public Entity getEntity(String attributeName) {
      Entity entity = delegate().getEntity(attributeName);
      return entity != null ? newPretendingEntity(entity) : null;
    }

    /**
     * Because the File datatype has a reference to {@link FileMetaMetaData} it can happen that a
     * typed FileMeta Entity is requested.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <E extends Entity> E getEntity(String attributeName, Class<E> clazz) {
      Entity entity = delegate().getEntity(attributeName);
      if (clazz.equals(FileMeta.class)) {
        return entity != null ? (E) new FileMeta(newPretendingEntity(entity)) : null;
      } else {
        throw new UnsupportedOperationException("Can't return typed pretending entities");
      }
    }

    @Override
    public Iterable<Entity> getEntities(String attributeName) {
      return stream(delegate().getEntities(attributeName))
          .map(this::newPretendingEntity)
          .collect(toList());
    }

    /**
     * Because the File datatype has a reference to {@link FileMetaMetaData} it can happen that a
     * typed FileMeta Entity is requested.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz) {
      Iterable<E> entities = delegate().getEntities(attributeName, clazz);
      if (clazz.equals(FileMeta.class)) {
        return stream(entities)
            .filter(Objects::nonNull)
            .map(this::newPretendingEntity)
            .map(e -> (E) new FileMeta(e))
            .collect(toList());
      } else {
        throw new UnsupportedOperationException("Can't return typed pretending entities");
      }
    }

    private PretendingEntity newPretendingEntity(Entity entity) {
      return new PretendingEntity(entity, copiedEntityTypes);
    }
  }
}
