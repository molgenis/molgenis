package org.molgenis.navigator;

import static com.google.common.collect.Streams.concat;
import static com.google.common.collect.Streams.stream;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.util.MetaUtils;
import org.molgenis.jobs.JobExecutor;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.navigator.copy.job.CopyJobExecution;
import org.molgenis.navigator.copy.job.CopyJobExecutionFactory;
import org.molgenis.navigator.download.job.DownloadJobExecution;
import org.molgenis.navigator.download.job.DownloadJobExecutionFactory;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NavigatorServiceImpl implements NavigatorService {

  private final DataService dataService;
  private final JobExecutor jobExecutor;
  private final DownloadJobExecutionFactory downloadJobExecutionFactory;
  private final CopyJobExecutionFactory copyJobExecutionFactory;

  NavigatorServiceImpl(
      DataService dataService,
      JobExecutor jobExecutor,
      DownloadJobExecutionFactory downloadJobExecutionFactory,
      CopyJobExecutionFactory copyJobExecutionFactory) {
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
            .query(PACKAGE, Package.class)
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
            .query(PACKAGE, Package.class)
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

    CopyJobExecution jobExecution = copyJobExecutionFactory.create();
    jobExecution.setResources(new Gson().toJson(resources));
    jobExecution.setTargetPackage(aPackage != null ? aPackage.getId() : null);
    jobExecution.setUser(getCurrentUsername());
    jobExecutor.submit(jobExecution);
    return jobExecution;
  }

  @Override
  public JobExecution downloadResources(List<ResourceIdentifier> resources) {
    if (resources.isEmpty()) {
      throw new IllegalArgumentException("resources can't be empty");
    }

    DownloadJobExecution jobExecution = downloadJobExecutionFactory.create();
    jobExecution.setResources(new Gson().toJson(resources));
    jobExecution.setUser(getCurrentUsername());
    jobExecutor.submit(jobExecution);
    return jobExecution;
  }

  @Transactional
  @Override
  public void deleteResources(List<ResourceIdentifier> resources) {
    if (resources.isEmpty()) {
      return;
    }

    List<String> packageIds =
        resources
            .stream()
            .filter(resource -> resource.getType() == ResourceType.PACKAGE)
            .map(ResourceIdentifier::getId)
            .collect(toList());
    List<String> entityTypeIds =
        resources
            .stream()
            .filter(resource -> resource.getType() == ResourceType.ENTITY_TYPE)
            .map(ResourceIdentifier::getId)
            .collect(toList());

    if (packageIds.isEmpty()) {
      deleteEntityTypes(entityTypeIds);
    } else {
      List<Package> deletablePackages = getDeletablePackages(packageIds);
      deleteEntityTypes(deletablePackages, entityTypeIds);
      deletePackages(deletablePackages);
    }
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
    Package aPackage = getPackage(resource.getId());
    if (aPackage == null) {
      throw new NullPointerException("package can't be null");
    }

    if (!Objects.equal(aPackage.getLabel(), resource.getLabel())
        || !Objects.equal(aPackage.getDescription(), resource.getDescription())) {
      aPackage.setLabel(resource.getLabel());
      aPackage.setDescription(resource.getDescription());
      dataService.update(PACKAGE, aPackage);
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

  private void deleteEntityTypes(@NotEmpty List<String> entityTypeIds) {
    deleteEntityTypes(emptyList(), entityTypeIds);
  }

  @SuppressWarnings("unchecked")
  private void deleteEntityTypes(List<Package> packages, List<String> entityTypeIds) {
    List<Object> allEntityTypeIds;
    if (packages.isEmpty()) {
      allEntityTypeIds = (List<Object>) (List) entityTypeIds;
    } else {
      Stream<Object> allPackageEntityTypeIds =
          packages
              .stream()
              .flatMap(aPackage -> stream(aPackage.getEntityTypes()))
              .map(EntityType::getId);
      if (entityTypeIds.isEmpty()) {
        allEntityTypeIds = allPackageEntityTypeIds.collect(toList());
      } else {
        allEntityTypeIds =
            concat(entityTypeIds.stream(), allPackageEntityTypeIds).collect(toList());
      }
    }
    if (!allEntityTypeIds.isEmpty()) {
      dataService.deleteAll(ENTITY_TYPE_META_DATA, allEntityTypeIds.stream());
    }
  }

  private void deletePackages(@NotEmpty List<Package> packages) {
    // the package entity types have been deleted, so delete by id instead of entity
    dataService.deleteAll(PACKAGE, packages.stream().map(Package::getId));
  }

  private List<Package> getDeletablePackages(List<String> packageIds) {
    @SuppressWarnings("unchecked")
    List<Object> untypedPackageIds = (List) packageIds;
    List<Package> packages =
        !packageIds.isEmpty()
            ? dataService
                .findAll(PACKAGE, untypedPackageIds.stream(), Package.class)
                .collect(toList())
            : emptyList();

    Iterable<Package> packageIterable =
        Traverser.forTree(Package::getChildren).breadthFirst(packages);
    return Lists.newArrayList(packageIterable);
  }

  private void movePackages(
      List<ResourceIdentifier> typeResources, @Nullable Package targetPackage) {
    List<Package> packages =
        dataService
            .findAll(PACKAGE, typeResources.stream().map(ResourceIdentifier::getId), Package.class)
            .filter(aPackage -> isDifferentPackage(aPackage.getParent(), targetPackage))
            .collect(toList());
    if (!packages.isEmpty()) {
      packages.forEach(aPackage -> aPackage.setParent(targetPackage));
      dataService.update(PACKAGE, packages.stream());
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
    boolean isSystemPackage = MetaUtils.isSystemPackage(aPackage);
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
    ResourceType type =
        entityType.isAbstract() ? ResourceType.ENTITY_TYPE_ABSTRACT : ResourceType.ENTITY_TYPE;
    boolean isSystemEntityType = MetaUtils.isSystemPackage(entityType.getPackage());
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

    Package aPackage = dataService.findOneById(PACKAGE, folderId, Package.class);
    if (aPackage == null) {
      throw new UnknownEntityException(PACKAGE, folderId);
    }
    return aPackage;
  }
}
