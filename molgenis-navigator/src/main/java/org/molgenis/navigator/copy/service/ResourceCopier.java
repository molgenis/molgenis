package org.molgenis.navigator.copy.service;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Streams.stream;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.meta.model.EntityType.AttributeCopyMode.DEEP_COPY_ATTRS;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.molgenis.data.AbstractEntityDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaMetaData;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.resource.ResourceCollection;
import org.molgenis.data.util.EntityTypeUtils;
import org.molgenis.navigator.copy.exception.RecursiveCopyException;

// TODO document
public class ResourceCopier {

  private static final String POSTFIX = " (Copy)";

  private final List<Package> packages;
  private final List<EntityType> entityTypes;
  @Nullable private final Package targetLocation;

  private final DataService dataService;
  private final MetaDataService metaDataService;
  private final IdGenerator idGenerator;
  private final PackageMetadata packageMetadata;
  private final EntityTypeDependencyResolver entityTypeDependencyResolver;
  private final AttributeFactory attributeFactory;

  /** List of EntityTypes contained in Package(s) that are being copied. */
  private final List<EntityType> entityTypesInPackages;

  private final Map<String, Package> packageMap;
  private final Map<String, EntityType> entityTypeMap;

  ResourceCopier(
      ResourceCollection resourceCollection,
      @Nullable Package targetLocation,
      DataService dataService,
      IdGenerator idGenerator,
      PackageMetadata packageMetadata,
      EntityTypeDependencyResolver entityTypeDependencyResolver,
      AttributeFactory attributeFactory) {
    requireNonNull(resourceCollection);
    this.packages = resourceCollection.getPackages();
    this.entityTypes = resourceCollection.getEntityTypes();
    this.targetLocation = targetLocation;

    this.dataService = requireNonNull(dataService);
    this.idGenerator = requireNonNull(idGenerator);
    this.packageMetadata = requireNonNull(packageMetadata);
    this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
    this.attributeFactory = requireNonNull(attributeFactory);

    this.metaDataService = this.dataService.getMeta();

    this.entityTypesInPackages = newArrayList();
    this.packageMap = newHashMap();
    this.entityTypeMap = newHashMap();
  }

  public void copy() {
    packages.forEach(this::copyPackage);
    copyEntityTypes();
  }

  private void copyEntityTypes() {
    entityTypes.forEach(this::assignUniqueLabel);
    entityTypes.forEach(entityType -> entityType.setPackage(targetLocation));

    List<EntityType> allEntityTypes = newArrayList();
    allEntityTypes.addAll(entityTypes);
    allEntityTypes.addAll(entityTypesInPackages);

    List<EntityType> resolvedEntityTypes = entityTypeDependencyResolver.resolve(allEntityTypes);
    resolvedEntityTypes.forEach(this::copyEntityType);
  }

  private void copyEntityType(EntityType original) {
    EntityType copy = EntityType.newInstance(original, DEEP_COPY_ATTRS, attributeFactory);

    assignNewId(copy);
    updatePackage(copy);
    updateExtends(copy);
    updateReferences(copy);

    metaDataService.addEntityType(copy);
    if (!copy.isAbstract()) {

      Stream<Entity> entities = dataService.findAll(original.getId());
      entities = entities.map(PretendingEntity::new);
      dataService.add(copy.getId(), entities);
    }

    metaDataService.updateEntityType(copy);
  }

  private void updatePackage(EntityType entityType) {
    if (entityType.getPackage() != null) {
      String packageId = entityType.getPackage().getId();
      if (packageMap.containsKey(packageId)) {
        entityType.setPackage(packageMap.get(packageId));
      }
    }
  }

  private void updateExtends(EntityType entityType) {
    if (entityType.getExtends() != null) {
      String extendsId = entityType.getExtends().getId();
      if (entityTypeMap.containsKey(extendsId)) {
        entityType.setExtends(entityTypeMap.get(extendsId));
      }
    }
  }

  private void updateReferences(EntityType entityType) {
    stream(entityType.getAtomicAttributes())
        .filter(EntityTypeUtils::isReferenceType)
        .forEach(this::updateReference);
  }

  private void updateReference(Attribute attribute) {
    if (attribute.getRefEntity() != null) {
      String refId = attribute.getRefEntity().getId();
      if (entityTypeMap.containsKey(refId)) {
        attribute.setRefEntity(entityTypeMap.get(refId));
      }
    }
  }

  private void copyPackage(Package pack) {
    validateNotContainsItself(pack);
    assignUniqueLabel(pack);
    copyPackageRecursive(pack, targetLocation);
  }

  private void copyPackageRecursive(Package pack, Package parent) {
    entityTypesInPackages.addAll(newArrayList(pack.getEntityTypes()));
    assignNewId(pack);
    pack.setParent(parent);
    dataService.add(PACKAGE, pack);
    pack.getChildren().forEach(child -> copyPackageRecursive(getPackage(child.getId()), pack));
  }

  private void assignNewId(EntityType entityType) {
    String newId = idGenerator.generateId();
    entityTypeMap.put(entityType.getId(), entityType);
    entityType.setId(newId);
  }

  private void assignNewId(Package pack) {
    String newId = idGenerator.generateId();
    packageMap.put(pack.getId(), pack);
    pack.setId(newId);
  }

  /**
   * Checks if there's a Package in the target location with the same label. If so, keeps adding a
   * postfix until the label is unique.
   */
  private void assignUniqueLabel(Package pack) {
    Set<String> existingLabels = emptySet();
    if (targetLocation != null) {
      existingLabels = stream(targetLocation.getChildren()).map(Package::getLabel).collect(toSet());
    }
    pack.setLabel(generateUniqueLabel(existingLabels, pack.getLabel()));
  }

  /**
   * Checks if there's an EntityType in the target location with the same label. If so, keeps adding
   * a postfix until the label is unique.
   */
  private void assignUniqueLabel(EntityType entityType) {
    Set<String> existingLabels = emptySet();
    if (targetLocation != null) {
      existingLabels =
          stream(targetLocation.getEntityTypes()).map(EntityType::getLabel).collect(toSet());
    }
    entityType.setLabel(generateUniqueLabel(existingLabels, entityType.getLabel()));
  }

  private String generateUniqueLabel(Set<String> existingLabels, String label) {
    StringBuilder newLabel = new StringBuilder(label);
    while (existingLabels.contains(newLabel.toString())) {
      newLabel.append(POSTFIX);
    }
    return newLabel.toString();
  }

  /** Checks that the target location isn't contained in the packages that will be copied. */
  private void validateNotContainsItself(Package pack) {
    if (pack.equals(targetLocation)) {
      throw new RecursiveCopyException();
    }
    pack.getChildren().forEach(this::validateNotContainsItself);
  }

  private Package getPackage(String id) {
    return metaDataService
        .getPackage(id)
        .orElseThrow(() -> new UnknownEntityException(packageMetadata, id));
  }

  /**
   * When copying rows from one repository to another, the metadata of these entities will not fit
   * the copied repository and its references because the metadatas will have different IDs. The
   * PretendingEntity acts like it's the newly copied entity by returning the metadata of the copied
   * repository instead of the original.
   */
  private class PretendingEntity extends AbstractEntityDecorator {

    PretendingEntity(Entity entity) {
      super(entity);
    }

    @Override
    public EntityType getEntityType() {
      if (delegate().getEntityType() != null) {
        String id = delegate().getEntityType().getId();
        if (entityTypeMap.containsKey(id)) {
          return entityTypeMap.get(id);
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
      return entity != null ? new PretendingEntity(entity) : null;
    }

    /**
     * Because the File datatype has a reference to {@link FileMetaMetaData} it can happen that a
     * typed FileMeta Entity is requested.
     */
    @Override
    public <E extends Entity> E getEntity(String attributeName, Class<E> clazz) {
      Entity entity = delegate().getEntity(attributeName);
      if (clazz.equals(FileMeta.class)) {
        //noinspection unchecked
        return entity != null ? (E) new FileMeta(new PretendingEntity(entity)) : null;
      } else {
        throw new UnsupportedOperationException("Can't return typed pretending entities");
      }
    }

    @Override
    public Iterable<Entity> getEntities(String attributeName) {
      return stream(delegate().getEntities(attributeName))
          .map(PretendingEntity::new)
          .collect(toList());
    }

    /**
     * Because the File datatype has a reference to {@link FileMetaMetaData} it can happen that a
     * typed FileMeta Entity is requested.
     */
    @Override
    public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz) {
      Iterable<E> entities = delegate().getEntities(attributeName, clazz);
      if (clazz.equals(FileMeta.class)) {
        //noinspection unchecked
        return stream(entities)
            .filter(Objects::nonNull)
            .map(PretendingEntity::new)
            .map(e -> (E) new FileMeta(e))
            .collect(toList());
      } else {
        throw new UnsupportedOperationException("Can't return typed pretending entities");
      }
    }
  }
}
