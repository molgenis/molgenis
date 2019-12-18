package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.google.common.collect.Iterators;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownPackageException;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.util.PackageUtils;

public class GroupPackageRepositoryDecorator extends AbstractRepositoryDecorator<Package> {
  private static final int BATCH_SIZE = 1000;

  private final GroupPackageService groupPackageService;

  public GroupPackageRepositoryDecorator(
      Repository<Package> delegateRepository, GroupPackageService groupPackageService) {
    super(delegateRepository);
    this.groupPackageService = requireNonNull(groupPackageService);
  }

  @Override
  public void add(Package aPackage) {
    super.add(aPackage);
    if (isGroupPackage(aPackage)) {
      groupPackageService.createGroup(aPackage);
    }
  }

  @Override
  public Integer add(Stream<Package> packageStream) {
    AtomicInteger count = new AtomicInteger();
    Iterators.partition(packageStream.iterator(), BATCH_SIZE)
        .forEachRemaining(
            packageBatch -> {
              super.add(packageBatch.stream());
              count.addAndGet(packageBatch.size());

              List<Package> groupPackages =
                  packageBatch.stream().filter(this::isGroupPackage).collect(toList());
              if (!groupPackages.isEmpty()) {
                groupPackageService.createGroups(groupPackages);
              }
            });
    return count.get();
  }

  @Override
  public void update(Package aPackage) {
    Package currentPackage = findOneById(aPackage.getId());
    if (currentPackage == null) {
      throw new UnknownPackageException(aPackage.getId());
    }

    super.update(aPackage);

    if (!isGroupPackage(currentPackage) && isGroupPackage(aPackage)) {
      groupPackageService.createGroup(aPackage);
    }
  }

  @Override
  public void update(Stream<Package> packageStream) {
    AtomicInteger count = new AtomicInteger();
    Iterators.partition(packageStream.iterator(), BATCH_SIZE)
        .forEachRemaining(
            packageBatch -> {
              Map<String, Package> currentPackageMap =
                  findAll(packageBatch.stream().map(Package::getId))
                      .collect(toMap(Package::getId, e -> e));
              packageBatch.forEach(
                  aPackage -> {
                    Package currentPackage = currentPackageMap.get(aPackage.getId());
                    validateUpdate(currentPackage, aPackage);
                  });

              super.update(packageBatch.stream());
              count.addAndGet(packageBatch.size());

              packageBatch.forEach(
                  aPackage -> {
                    Package currentPackage = currentPackageMap.get(aPackage.getId());
                    if (!isGroupPackage(currentPackage) && isGroupPackage(aPackage)) {
                      groupPackageService.createGroup(aPackage);
                    }
                  });
            });
  }

  private void validateUpdate(Package currentPackage, Package updatedPackage) {
    if (isGroupPackage(currentPackage) && !isGroupPackage(updatedPackage)) {
      throw new RuntimeException(
          "cannot update group package to non-group package"); // TODO coded exception
    }
  }

  private boolean isGroupPackage(Package aPackage) {
    // TODO enable creation of group for system package (currently fails during bootstrapping
    return aPackage.getParent() == null && !PackageUtils.isSystemPackage(aPackage);
  }
}
