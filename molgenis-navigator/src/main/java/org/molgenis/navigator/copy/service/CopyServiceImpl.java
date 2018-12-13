package org.molgenis.navigator.copy.service;

import static com.google.common.collect.Iterables.size;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.CheckForNull;
import org.molgenis.data.UnknownPackageException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.util.PackageUtils.PackageTreeTraverser;
import org.molgenis.i18n.ContextMessageSource;
import org.molgenis.i18n.ErrorCoded;
import org.molgenis.jobs.Progress;
import org.molgenis.navigator.copy.exception.UnknownCopyFailedException;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.util.ResourceCollection;
import org.molgenis.navigator.util.ResourceCollector;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("unused")
@Component
public class CopyServiceImpl implements CopyService {

  private final ResourceCollector resourceCollector;
  private final MetaDataService metadataService;
  private final PackageCopier packageCopier;
  private final EntityTypeCopier entityTypeCopier;
  private final ContextMessageSource contextMessageSource;

  CopyServiceImpl(
      ResourceCollector resourceCollector,
      MetaDataService metadataService,
      PackageCopier packageCopier,
      EntityTypeCopier entityTypeCopier,
      ContextMessageSource contextMessageSource) {
    this.resourceCollector = requireNonNull(resourceCollector);
    this.metadataService = requireNonNull(metadataService);
    this.packageCopier = requireNonNull(packageCopier);
    this.entityTypeCopier = requireNonNull(entityTypeCopier);
    this.contextMessageSource = requireNonNull(contextMessageSource);
  }

  @Override
  @SuppressWarnings(
      "squid:S1193") // Exception types should not be tested using "instanceof" in catch blocks
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public Void copy(
      List<ResourceIdentifier> resources, @CheckForNull String targetPackageId, Progress progress) {
    try {
      ResourceCollection resourceCollection = resourceCollector.get(resources);
      Package targetPackage = getPackage(targetPackageId);
      CopyState state = CopyState.create(targetPackage, progress);

      copyResources(resourceCollection, state);
    } catch (RuntimeException e) {
      if (e instanceof ErrorCoded) {
        throw e;
      } else {
        throw new UnknownCopyFailedException(e);
      }
    }
    return null;
  }

  private void copyResources(ResourceCollection resourceCollection, CopyState state) {
    Progress progress = state.progress();
    progress.setProgressMax(calculateMaxProgress(resourceCollection));
    progress.progress(0, contextMessageSource.getMessage("progress-copy-started"));

    copyPackages(resourceCollection.getPackages(), state);
    copyEntityTypes(resourceCollection.getEntityTypes(), state);

    progress.status(contextMessageSource.getMessage("progress-copy-success"));
  }

  private void copyPackages(List<Package> packages, CopyState state) {
    if (!packages.isEmpty()) {
      state.progress().status(contextMessageSource.getMessage("progress-copy-packages"));
      packageCopier.copy(packages, state);
    }
  }

  private void copyEntityTypes(List<EntityType> entityTypes, CopyState state) {
    if (!entityTypes.isEmpty() || !state.entityTypesInPackages().isEmpty()) {
      state.progress().status(contextMessageSource.getMessage("progress-copy-entity-types"));
      entityTypeCopier.copy(entityTypes, state);
    }
  }

  private Package getPackage(@CheckForNull String targetPackageId) {
    return targetPackageId != null
        ? metadataService
            .getPackage(targetPackageId)
            .orElseThrow(() -> new UnknownPackageException(targetPackageId))
        : null;
  }

  private int calculateMaxProgress(ResourceCollection collection) {
    AtomicInteger maxProgress = new AtomicInteger();
    maxProgress.addAndGet(collection.getEntityTypes().size());
    maxProgress.addAndGet(collection.getPackages().size());

    collection
        .getPackages()
        .forEach(
            packToCopy ->
                new PackageTreeTraverser()
                    .postOrderTraversal(packToCopy)
                    .forEach(
                        pack -> {
                          maxProgress.addAndGet(size(pack.getChildren()));
                          maxProgress.addAndGet(size(pack.getEntityTypes()));
                        }));

    return maxProgress.get();
  }
}
