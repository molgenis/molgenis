package org.molgenis.navigator;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.navigator.model.ResourceType.ENTITY_TYPE;
import static org.molgenis.navigator.model.ResourceType.ENTITY_TYPE_ABSTRACT;

import com.google.common.base.Objects;
import com.google.common.collect.Streams;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.util.PackageUtils;
import org.molgenis.jobs.JobExecutor;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.navigator.copy.job.ResourceCopyJobExecution;
import org.molgenis.navigator.copy.job.ResourceCopyJobExecutionFactory;
import org.molgenis.navigator.download.job.ResourceDownloadJobExecution;
import org.molgenis.navigator.download.job.ResourceDownloadJobExecutionFactory;
import org.molgenis.navigator.model.Resource;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.model.ResourceType;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NavigatorServiceImpl implements NavigatorService {

  private final DataService dataService;
  private final JobExecutor jobExecutor;
  private final ResourceDownloadJobExecutionFactory downloadJobExecutionFactory;
  private final ResourceCopyJobExecutionFactory copyJobExecutionFactory;

  NavigatorServiceImpl(
      DataService dataService,
      JobExecutor jobExecutor,
      ResourceDownloadJobExecutionFactory downloadJobExecutionFactory,
      ResourceCopyJobExecutionFactory copyJobExecutionFactory) {
    this.dataService = requireNonNull(dataService);
    this.jobExecutor = requireNonNull(jobExecutor);
    this.downloadJobExecutionFactory = requireNonNull(downloadJobExecutionFactory);
    this.copyJobExecutionFactory = requireNonNull(copyJobExecutionFactory);
  }

  @Transactional(readOnly = true)
  @Override
  public @Nullable Folder getFolder(@Nullable String folderId) {
    Package aPackage = getPackage(folderId);
    return toFolder(aPackage);
  }

  @Transactional(readOnly = true)
  @Override
  public List<Resource> getResources(@Nullable String folderId) {
    try (Stream<Resource> packageResources =
        dataService
            .query(PackageMetadata.PACKAGE, Package.class)
            .eq(PackageMetadata.PARENT, folderId)
            .findAll()
            .map(this::toResource)) {
      Stream<Resource> entityTypeResources =
          dataService
              .query(ENTITY_TYPE_META_DATA, EntityType.class)
              .eq(EntityTypeMetadata.PACKAGE, folderId)
              .findAll()
              .map(this::toResource);
      return Streams.concat(packageResources, entityTypeResources).collect(toList());
    }
  }

  @Transactional(readOnly = true)
  @Override
  public List<Resource> findResources(String query) {
    Stream<Resource> packageResources =
        dataService
            .query(PackageMetadata.PACKAGE, Package.class)
            .search(PackageMetadata.LABEL, query)
            .or()
            .search(PackageMetadata.DESCRIPTION, query)
            .findAll()
            .map(this::toResource);
    Stream<Resource> entityTypeResources =
        dataService
            .query(ENTITY_TYPE_META_DATA, EntityType.class)
            .search(EntityTypeMetadata.LABEL, query)
            .or()
            .search(EntityTypeMetadata.DESCRIPTION, query)
            .findAll()
            .map(this::toResource);
    return Streams.concat(packageResources, entityTypeResources).collect(toList());
  }

  @Transactional
  @Override
  public void moveResources(List<ResourceIdentifier> resources, @Nullable String targetFolderId) {
    if (resources.isEmpty()) {
      return;
    }

    Package targetPackage = getPackage(targetFolderId);

    Map<ResourceType, List<ResourceIdentifier>> resourceMap =
        resources.stream().collect(groupingBy(ResourceIdentifier::getType));
    resourceMap.forEach(
        (type, typeResources) -> {
          switch (type) {
            case PACKAGE:
              movePackages(typeResources, targetPackage);
              break;
            case ENTITY_TYPE:
            case ENTITY_TYPE_ABSTRACT:
              moveEntityTypes(typeResources, targetPackage);
              break;
            default:
              throw new UnexpectedEnumException(type);
          }
        });
  }

  @Override
  public JobExecution copyResources(
      List<ResourceIdentifier> resources, @Nullable String targetFolderId) {
    if (resources.isEmpty()) {
      throw new IllegalArgumentException("resources can't be empty");
    }

    Package aPackage = getPackage(targetFolderId);

    ResourceCopyJobExecution jobExecution = copyJobExecutionFactory.create();
    jobExecution.setResources(resources);
    jobExecution.setTargetPackage(aPackage != null ? aPackage.getId() : null);
    jobExecutor.submit(jobExecution);
    return jobExecution;
  }

  @Override
  public JobExecution downloadResources(List<ResourceIdentifier> resources) {
    if (resources.isEmpty()) {
      throw new IllegalArgumentException("resources can't be empty");
    }

    ResourceDownloadJobExecution jobExecution = downloadJobExecutionFactory.create();
    jobExecution.setResources(resources);
    jobExecutor.submit(jobExecution);
    return jobExecution;
  }

  @Transactional
  @Override
  public void deleteResources(List<ResourceIdentifier> resources) {
    if (resources.isEmpty()) {
      return;
    }

    Set<Object> packageIds = new LinkedHashSet<>();
    Set<Object> entityTypeIds = new LinkedHashSet<>();
    resources.forEach(
        resource -> {
          switch (resource.getType()) {
            case PACKAGE:
              packageIds.add(resource.getId());
              break;
            case ENTITY_TYPE:
            case ENTITY_TYPE_ABSTRACT:
              entityTypeIds.add(resource.getId());
              break;
            default:
              throw new UnexpectedEnumException(resource.getType());
          }
        });

    if (!entityTypeIds.isEmpty()) {
      deleteEntityTypes(entityTypeIds);
    }
    if (!packageIds.isEmpty()) {
      deletePackages(packageIds);
    }
  }

  private void deleteEntityTypes(@NotEmpty Set<Object> entityTypeIds) {
    List<EntityType> entityTypes =
        dataService
            .findAll(ENTITY_TYPE_META_DATA, entityTypeIds.stream(), EntityType.class)
            .collect(toList());

    if (!entityTypes.isEmpty()) {
      dataService.delete(ENTITY_TYPE_META_DATA, entityTypes.stream());
    }
  }

  private void deletePackages(Set<Object> packageIds) {
    List<Package> packages =
        dataService
            .findAll(PackageMetadata.PACKAGE, packageIds.stream(), Package.class)
            .collect(toList());

    if (!packages.isEmpty()) {
      List<Package> deletablePackages = getDeletablePackages(packages, packageIds);
      if (!deletablePackages.isEmpty()) {
        dataService.delete(PackageMetadata.PACKAGE, deletablePackages.stream());
      }
    }
  }

  private List<Package> getDeletablePackages(List<Package> packages, Set<Object> packageIds) {
    return packages
        .stream()
        .filter(aPackage -> isDeletablePackage(aPackage, packageIds))
        .collect(toList());
  }

  private boolean isDeletablePackage(Package aPackage, Set<Object> packageIds) {
    Package parentPackage = aPackage.getParent();
    while (parentPackage != null) {
      if (packageIds.contains(parentPackage.getId())) {
        return false;
      }
      parentPackage = parentPackage.getParent();
    }
    return true;
  }

  @Transactional
  @Override
  public void updateResource(Resource resource) {
    ResourceType resourceType = resource.getType();
    switch (resourceType) {
      case PACKAGE:
        updatePackage(resource);
        break;
      case ENTITY_TYPE:
      case ENTITY_TYPE_ABSTRACT:
        updateEntityType(resource);
        break;
      default:
        throw new UnexpectedEnumException(resourceType);
    }
  }

  private void updatePackage(Resource resource) {
    Package aPackage =
        dataService.findOneById(PackageMetadata.PACKAGE, resource.getId(), Package.class);
    if (aPackage == null) {
      throw new UnknownEntityException(PackageMetadata.PACKAGE, resource.getId());
    }

    if (!Objects.equal(aPackage.getLabel(), resource.getLabel())
        || !Objects.equal(aPackage.getDescription(), resource.getDescription())) {
      aPackage.setLabel(resource.getLabel());
      aPackage.setDescription(resource.getDescription());
      dataService.update(PackageMetadata.PACKAGE, aPackage);
    }
  }

  private void updateEntityType(Resource resource) {
    EntityType entityType =
        dataService.findOneById(ENTITY_TYPE_META_DATA, resource.getId(), EntityType.class);
    if (entityType == null) {
      throw new UnknownEntityException(ENTITY_TYPE_META_DATA, resource.getId());
    }

    if (!Objects.equal(entityType.getLabel(), resource.getLabel())
        || !Objects.equal(entityType.getDescription(), resource.getDescription())) {
      entityType.setLabel(resource.getLabel());
      entityType.setDescription(resource.getDescription());
      dataService.update(ENTITY_TYPE_META_DATA, entityType);
    }
  }

  private void movePackages(
      List<ResourceIdentifier> typeResources, @Nullable Package targetPackage) {
    List<Package> packages =
        dataService
            .findAll(
                PackageMetadata.PACKAGE,
                typeResources.stream().map(ResourceIdentifier::getId),
                Package.class)
            .filter(aPackage -> isDifferentPackage(aPackage.getParent(), targetPackage))
            .collect(toList());
    if (!packages.isEmpty()) {
      packages.forEach(aPackage -> aPackage.setParent(targetPackage));
      dataService.update(PackageMetadata.PACKAGE, packages.stream());
    }
  }

  private void moveEntityTypes(
      List<ResourceIdentifier> typeResources, @Nullable Package targetPackage) {
    List<EntityType> entityTypes =
        dataService
            .findAll(
                ENTITY_TYPE_META_DATA,
                typeResources.stream().map(ResourceIdentifier::getId),
                EntityType.class)
            .filter(entityType -> isDifferentPackage(entityType.getPackage(), targetPackage))
            .collect(toList());
    if (!entityTypes.isEmpty()) {
      entityTypes.forEach(entityType -> entityType.setPackage(targetPackage));
      dataService.update(ENTITY_TYPE_META_DATA, entityTypes.stream());
    }
  }

  private boolean isDifferentPackage(@Nullable Package thisPackage, @Nullable Package thatPackage) {
    boolean isSame;
    if (thisPackage == null && thatPackage == null) {
      isSame = true;
    } else if (thisPackage != null && thatPackage != null) {
      isSame = thisPackage.getId().equals(thatPackage.getId());
    } else {
      isSame = false;
    }
    return !isSame;
  }

  private Resource toResource(Package aPackage) {
    boolean isSystemPackage = PackageUtils.isSystemPackage(aPackage);
    return Resource.builder()
        .setType(ResourceType.PACKAGE)
        .setId(aPackage.getId())
        .setLabel(aPackage.getLabel())
        .setDescription(aPackage.getDescription())
        .setHidden(isSystemPackage)
        .setReadonly(isSystemPackage)
        .build();
  }

  private Resource toResource(EntityType entityType) {
    ResourceType type = entityType.isAbstract() ? ENTITY_TYPE_ABSTRACT : ENTITY_TYPE;
    boolean isSystemEntityType = PackageUtils.isSystemPackage(entityType.getPackage());
    return Resource.builder()
        .setType(type)
        .setId(entityType.getId())
        .setLabel(entityType.getLabel())
        .setDescription(entityType.getDescription())
        .setHidden(isSystemEntityType)
        .setReadonly(isSystemEntityType)
        .build();
  }

  /**
   * @param aPackage <tt>null</tt> implies the root package
   * @return folder or <tt>null</tt> for the root folder
   */
  private @Nullable Folder toFolder(@Nullable Package aPackage) {
    if (aPackage == null) {
      return null;
    }

    Folder parentFolder = aPackage.getParent() != null ? toFolder(aPackage.getParent()) : null;
    return Folder.create(aPackage.getId(), aPackage.getLabel(), parentFolder);
  }

  /**
   * @param folderId <tt>null</tt> implies the root folder
   * @return package or <tt>null</tt> for the root package
   */
  private @Nullable Package getPackage(@Nullable String folderId) {
    if (folderId == null) {
      return null;
    }

    Package aPackage = dataService.findOneById(PackageMetadata.PACKAGE, folderId, Package.class);
    if (aPackage == null) {
      throw new UnknownEntityException(PackageMetadata.PACKAGE, folderId);
    }
    return aPackage;
  }
}
