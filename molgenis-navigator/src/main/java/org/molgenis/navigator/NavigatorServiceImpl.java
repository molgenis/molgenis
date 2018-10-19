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

import com.google.common.collect.Lists;
import com.google.common.graph.Traverser;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.validation.constraints.NotEmpty;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.jobs.JobExecutor;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.navigator.Resource.Type;
import org.molgenis.oneclickimporter.job.OneClickImportJobExecution;
import org.molgenis.oneclickimporter.job.OneClickImportJobExecutionFactory;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NavigatorServiceImpl implements NavigatorService {

  private final DataService dataService;
  private final OneClickImportJobExecutionFactory
      oneClickImportJobExecutionFactory; // TODO dummy, must be removed
  private final JobExecutor jobExecutor;

  NavigatorServiceImpl(
      DataService dataService,
      OneClickImportJobExecutionFactory oneClickImportJobExecutionFactory,
      JobExecutor jobExecutor) {
    this.dataService = requireNonNull(dataService);
    this.oneClickImportJobExecutionFactory = oneClickImportJobExecutionFactory;
    this.jobExecutor = jobExecutor;
  }

  @Transactional
  @Override
  public void moveResources(List<Resource> resources, String targetFolderId) {
    Package targetPackage;
    if (targetFolderId != null) {
      targetPackage = dataService.findOneById(PACKAGE, targetFolderId, Package.class);
      if (targetPackage == null) {
        throw new UnknownEntityException(PACKAGE, targetFolderId);
      }
    } else {
      targetPackage = null;
    }

    Map<Type, List<Resource>> resourceMap =
        resources.stream().collect(groupingBy(Resource::getType));
    resourceMap.forEach(
        (type, typeResources) -> {
          switch (type) {
            case PACKAGE:
              movePackages(typeResources, targetPackage);
              break;
            case ENTITY_TYPE:
              moveEntityTypes(typeResources, targetPackage);
              break;
            default:
              throw new UnexpectedEnumException(type);
          }
        });
  }

  @Override
  public JobExecution copyResources(List<Resource> resources, String targetFolderId) {
    OneClickImportJobExecution jobExecution = oneClickImportJobExecutionFactory.create();
    jobExecution.setUser(getCurrentUsername());
    jobExecution.setFile("dummy.csv");
    jobExecutor.submit(jobExecution);
    return jobExecution;
  }

  @Transactional
  @Override
  public void deleteItems(List<String> packageIds, List<String> entityTypeIds) {
    if (packageIds.isEmpty() && entityTypeIds.isEmpty()) {
      return;
    }

    if (packageIds.isEmpty()) {
      deleteEntityTypes(entityTypeIds);
    } else {
      List<Package> deletablePackages = getDeletablePackages(packageIds);
      deleteEntityTypes(deletablePackages, entityTypeIds);
      deletePackages(deletablePackages);
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

  private void movePackages(List<Resource> typeResources, Package targetPackage) {
    List<Package> packages =
        dataService
            .findAll(PACKAGE, typeResources.stream().map(Resource::getId), Package.class)
            .collect(toList());
    packages.forEach(aPackage -> aPackage.setParent(targetPackage));
    dataService.update(PACKAGE, packages.stream());
  }

  private void moveEntityTypes(List<Resource> typeResources, Package targetPackage) {
    List<EntityType> entityTypes =
        dataService
            .findAll(
                ENTITY_TYPE_META_DATA,
                typeResources.stream().map(Resource::getId),
                EntityType.class)
            .collect(toList());
    entityTypes.forEach(entityType -> entityType.setPackage(targetPackage));
    dataService.update(ENTITY_TYPE_META_DATA, entityTypes.stream());
  }
}
