package org.molgenis.navigator.delete;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.i18n.ContextMessageSource;
import org.molgenis.jobs.Progress;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Service;

@Service
public class ResourceDeleteServiceImpl implements ResourceDeleteService {
  private final DataService dataService;
  private final ContextMessageSource contextMessageSource;

  ResourceDeleteServiceImpl(DataService dataService, ContextMessageSource contextMessageSource) {
    this.dataService = requireNonNull(dataService);
    this.contextMessageSource = requireNonNull(contextMessageSource);
  }

  @Override
  public Void delete(List<ResourceIdentifier> resources, Progress progress) {
    progress.status(contextMessageSource.getMessage("progress-delete-started"));

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

    progress.status(contextMessageSource.getMessage("progress-delete-success"));
    return null;
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
    return packages.stream()
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
}
