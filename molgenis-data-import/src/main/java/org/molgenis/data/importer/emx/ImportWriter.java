package org.molgenis.data.importer.emx;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.security.EntityTypePermission.READ_METADATA;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.molgenis.data.DataAction;
import org.molgenis.data.EntityManager;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.importer.*;
import org.molgenis.data.importer.DataPersister.DataMode;
import org.molgenis.data.importer.DataPersister.MetadataMode;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.util.UnexpectedEnumException;
import org.molgenis.validation.ConstraintViolation;
import org.springframework.transaction.annotation.Transactional;

/** Writes the imported metadata and data to target {@link RepositoryCollection}. */
public class ImportWriter {
  private final MetaDataService metaDataService;
  private final PermissionSystemService permissionSystemService;
  private final UserPermissionEvaluator permissionService;
  private final EntityManager entityManager;
  private final DataPersister dataPersister;

  ImportWriter(
      MetaDataService metaDataService,
      PermissionSystemService permissionSystemService,
      UserPermissionEvaluator permissionService,
      EntityManager entityManager,
      DataPersister dataPersister) {
    this.metaDataService = requireNonNull(metaDataService);
    this.permissionSystemService = requireNonNull(permissionSystemService);
    this.permissionService = requireNonNull(permissionService);
    this.entityManager = requireNonNull(entityManager);
    this.dataPersister = requireNonNull(dataPersister);
  }

  @Transactional
  public EntityImportReport doImport(EmxImportJob job) {
    if (job.getMetadataAction() != MetadataAction.IGNORE) {
      importTags(job.parsedMetaData);
      importPackages(job.parsedMetaData);
    }

    GroupedEntityTypes groupedEntityTypes = groupEntityTypes(job.getParsedMetaData().getEntities());

    validateEntityTypePermissions(groupedEntityTypes.getUpdatedEntityTypes());

    PersistResult persistResult =
        dataPersister.persist(
            new EmxDataProvider(job, entityManager),
            toMetadataMode(job.getMetadataAction()),
            toDataMode(job.getDataAction()));
    permissionSystemService.giveUserWriteMetaPermissions(groupedEntityTypes.getNewEntityTypes());

    persistResult
        .getNrPersistedEntitiesMap()
        .forEach(
            (key, value) ->
                job.getEntityImportReport().addEntityCount(key, Math.toIntExact(value)));
    groupedEntityTypes
        .getNewEntityTypes()
        .stream()
        .map(EntityType::getId)
        .forEach(job.getEntityImportReport()::addNewEntity);
    return job.report;
  }

  private void importTags(ParsedMetaData parsedMetaData) {
    ImmutableCollection<Tag> tags = parsedMetaData.getTags().values();
    metaDataService.upsertTags(tags);
  }

  private void importPackages(ParsedMetaData parsedMetaData) {
    ImmutableCollection<Package> packages = parsedMetaData.getPackages().values();
    metaDataService.upsertPackages(packages.stream().filter(Objects::nonNull));
  }

  private MetadataMode toMetadataMode(MetadataAction metadataAction) {
    MetadataMode metadataMode;
    switch (metadataAction) {
      case ADD:
        metadataMode = MetadataMode.ADD;
        break;
      case UPDATE:
        metadataMode = MetadataMode.UPDATE;
        break;
      case UPSERT:
        metadataMode = MetadataMode.UPSERT;
        break;
      case IGNORE:
        metadataMode = MetadataMode.NONE;
        break;
      default:
        throw new UnexpectedEnumException(metadataAction);
    }
    return metadataMode;
  }

  private DataMode toDataMode(DataAction databaseAction) {
    DataMode dataMode;
    switch (databaseAction) {
      case ADD:
        dataMode = DataMode.ADD;
        break;
      case ADD_UPDATE_EXISTING:
        dataMode = DataMode.UPSERT;
        break;
      case UPDATE:
        dataMode = DataMode.UPDATE;
        break;
      case ADD_IGNORE_EXISTING:
        throw new IllegalArgumentException(
            "Only ADD, ADD_UPDATE_EXISTING and UPDATE are supported");
      default:
        throw new UnexpectedEnumException(databaseAction);
    }
    return dataMode;
  }

  private void validateEntityTypePermissions(ImmutableCollection<EntityType> entityTypes) {
    entityTypes.forEach(this::validateEntityTypePermission);
  }

  private void validateEntityTypePermission(EntityType entityType) {
    String entityTypeName = entityType.getId();
    if (!permissionService.hasPermission(new EntityTypeIdentity(entityTypeName), READ_METADATA)) {
      throw new MolgenisValidationException(
          new ConstraintViolation(
              format("Permission denied on existing entity type [%s]", entityTypeName)));
    }
  }

  private GroupedEntityTypes groupEntityTypes(ImmutableCollection<EntityType> entities) {
    return runAsSystem(
        () -> {
          Map<String, EntityType> existingEntityTypeMap = new HashMap<>();
          for (EntityType entityType : entities) {
            EntityType existing = metaDataService.getEntityType(entityType.getId());
            if (existing != null) {
              existingEntityTypeMap.put(entityType.getId(), entityType);
            }
          }

          ImmutableCollection<EntityType> newEntityTypes =
              entities
                  .stream()
                  .filter(entityType -> !existingEntityTypeMap.containsKey(entityType.getId()))
                  .collect(collectingAndThen(toList(), ImmutableList::copyOf));

          ImmutableCollection<EntityType> existingEntityTypes =
              entities
                  .stream()
                  .filter(entityType -> existingEntityTypeMap.containsKey(entityType.getId()))
                  .collect(collectingAndThen(toList(), ImmutableList::copyOf));

          return new GroupedEntityTypes(newEntityTypes, existingEntityTypes);
        });
  }

  private static class GroupedEntityTypes {
    private final ImmutableCollection<EntityType> newEntityTypes;
    private final ImmutableCollection<EntityType> updatedEntityTypes;

    GroupedEntityTypes(
        ImmutableCollection<EntityType> newEntityTypes,
        ImmutableCollection<EntityType> updatedEntityTypes) {
      this.newEntityTypes = requireNonNull(newEntityTypes);
      this.updatedEntityTypes = requireNonNull(updatedEntityTypes);
    }

    ImmutableCollection<EntityType> getNewEntityTypes() {
      return newEntityTypes;
    }

    ImmutableCollection<EntityType> getUpdatedEntityTypes() {
      return updatedEntityTypes;
    }
  }
}
