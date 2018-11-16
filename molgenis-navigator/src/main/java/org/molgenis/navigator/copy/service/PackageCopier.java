package org.molgenis.navigator.copy.service;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.navigator.copy.service.LabelGenerator.generateUniqueLabel;

import java.util.List;
import java.util.Set;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownPackageException;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.util.PackageUtils;
import org.molgenis.navigator.copy.exception.RecursiveCopyException;
import org.springframework.stereotype.Component;

@Component
public class PackageCopier {

  private final DataService dataService;
  private final IdGenerator idGenerator;

  public PackageCopier(DataService dataService, IdGenerator idGenerator) {
    this.dataService = requireNonNull(dataService);
    this.idGenerator = requireNonNull(idGenerator);
  }

  public void copy(List<Package> packages, CopyState state) {
    packages.forEach(pack -> copyPackage(pack, state));
  }

  private void copyPackage(Package pack, CopyState state) {
    validateNotContainsItself(pack, state.targetPackage());
    assignUniqueLabel(pack, state.targetPackage());
    copyPackageRecursive(pack, state.targetPackage(), state);
  }

  private void copyPackageRecursive(Package pack, Package parent, CopyState state) {
    state.entityTypesInPackages().addAll(newArrayList(pack.getEntityTypes()));
    assignNewId(pack, state);
    pack.setParent(parent);
    dataService.add(PACKAGE, pack);
    state.progress().increment(1);
    pack.getChildren()
        .forEach(child -> copyPackageRecursive(getPackage(child.getId()), pack, state));
  }

  private void assignNewId(Package pack, CopyState state) {
    String newId = idGenerator.generateId();
    state.copiedPackages().put(pack.getId(), pack);
    pack.setId(newId);
  }

  /**
   * Checks if there's a Package in the target location with the same label. If so, keeps adding a
   * postfix until the label is unique.
   */
  private void assignUniqueLabel(Package pack, Package targetPackage) {
    Set<String> existingLabels;
    if (targetPackage != null) {
      existingLabels = stream(targetPackage.getChildren()).map(Package::getLabel).collect(toSet());
    } else {
      existingLabels =
          dataService
              .query(PACKAGE, Package.class)
              .eq(PackageMetadata.PARENT, null)
              .findAll()
              .map(Package::getLabel)
              .collect(toSet());
    }
    pack.setLabel(generateUniqueLabel(pack.getLabel(), existingLabels));
  }

  private void validateNotContainsItself(Package pack, Package targetPackage) {
    if (PackageUtils.contains(pack, targetPackage)) {
      throw new RecursiveCopyException();
    }
  }

  private Package getPackage(String id) {
    return dataService.getMeta().getPackage(id).orElseThrow(() -> new UnknownPackageException(id));
  }
}
