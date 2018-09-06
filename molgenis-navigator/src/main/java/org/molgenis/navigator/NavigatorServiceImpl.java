package org.molgenis.navigator;

import static com.google.common.collect.Streams.concat;
import static com.google.common.collect.Streams.stream;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;

import com.google.common.collect.Lists;
import com.google.common.graph.Traverser;
import java.util.List;
import java.util.stream.Stream;
import javax.validation.constraints.NotEmpty;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NavigatorServiceImpl implements NavigatorService {

  private final DataService dataService;

  NavigatorServiceImpl(DataService dataService) {
    this.dataService = requireNonNull(dataService);
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
}
