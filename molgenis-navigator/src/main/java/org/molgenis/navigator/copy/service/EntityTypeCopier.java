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
import java.util.Set;
import java.util.stream.Stream;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.util.EntityTypeUtils;
import org.springframework.stereotype.Component;

@Component
public class EntityTypeCopier {

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
    List<EntityType> preparedEntityTypes = prepareEntityTypes(entityTypes, state);
    List<EntityType> copiedEntityTypes = copyEntityTypes(preparedEntityTypes, state);
    persist(copiedEntityTypes, state);
  }

  private List<EntityType> prepareEntityTypes(List<EntityType> entityTypes, CopyState state) {
    return removeDoubles(entityTypes, state)
        .map(entityType -> assignUniqueLabel(entityType, state))
        .map(entityType -> setTargetPackage(entityType, state))
        .collect(toList());
  }

  private Stream<EntityType> removeDoubles(List<EntityType> entityTypes, CopyState state) {
    Set<String> idsInPackages =
        state.entityTypesInPackages().stream().map(EntityType::getId).collect(toSet());
    return entityTypes.stream().filter(e -> !idsInPackages.contains(e.getId()));
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
    List<EntityType> persistedEntityTypes =
        entityTypeDependencyResolver
            .resolve(copiedEntityTypes)
            .stream()
            .map(copy -> cutDefaultValues(copy, state))
            .map(this::persistEntityType)
            .collect(toList());

    persistedEntityTypes
        .stream()
        .map(copy -> copyEntities(copy, state))
        .map(copy -> pasteDefaultValues(copy, state))
        .forEach(e -> state.progress().increment(1));
  }

  private EntityType setTargetPackage(EntityType entityType, CopyState state) {
    entityType.setPackage(state.targetPackage());
    return entityType;
  }

  /**
   * Checks if there's an EntityType in the target location with the same label. If so, keeps adding
   * a postfix until the label is unique.
   */
  private EntityType assignUniqueLabel(EntityType entityType, CopyState state) {
    Set<String> existingLabels;
    Package targetPackage = state.targetPackage();
    if (targetPackage != null) {
      existingLabels =
          stream(targetPackage.getEntityTypes()).map(EntityType::getLabel).collect(toSet());
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
    return entityType;
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
        .filter(Attribute::hasDefaultValue)
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
}
