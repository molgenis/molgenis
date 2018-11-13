package org.molgenis.navigator.copy.service;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.jobs.Progress;
import org.molgenis.navigator.Resource;
import org.molgenis.navigator.ResourceCollection;
import org.molgenis.navigator.ResourceCollector;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("unused")
@Component
public class CopyServiceImpl implements CopyService {

  private final ResourceCopierFactory resourceCopierFactory;
  private final ResourceCollector resourceCollector;
  private final MetaDataService metaDataService;

  CopyServiceImpl(
      ResourceCopierFactory resourceCopierFactory,
      ResourceCollector resourceCollector,
      MetaDataService metaDataService) {
    this.resourceCopierFactory = requireNonNull(resourceCopierFactory);
    this.resourceCollector = requireNonNull(resourceCollector);
    this.metaDataService = requireNonNull(metaDataService);
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public String copy(List<Resource> resources, String targetPackageId, Progress progress) {
    ResourceCollection resourceCollection = resourceCollector.get(resources);

    Package targetPackage = getPackage(targetPackageId);

    resourceCopierFactory.newInstance(resourceCollection, targetPackage, progress).copy();
    return "true";
  }

  private Package getPackage(String targetPackageId) {
    return targetPackageId != null
        ? metaDataService
            .getPackage(targetPackageId)
            .orElseThrow(() -> new UnknownEntityException(PackageMetadata.PACKAGE, targetPackageId))
        : null;
  }
}
