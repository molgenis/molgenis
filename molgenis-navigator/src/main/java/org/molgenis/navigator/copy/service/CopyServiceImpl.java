package org.molgenis.navigator.copy.service;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.molgenis.data.UnknownPackageException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Package;
import org.molgenis.i18n.CodedRuntimeException;
import org.molgenis.jobs.Progress;
import org.molgenis.navigator.copy.exception.CopyFailedException;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.model.util.ResourceCollection;
import org.molgenis.navigator.model.util.ResourceCollector;
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
  public String copy(
      List<ResourceIdentifier> resources, String targetPackageId, Progress progress) {
    ResourceCollection resourceCollection = resourceCollector.get(resources);

    Package targetPackage = getPackage(targetPackageId);

    try {
      resourceCopierFactory.newInstance(resourceCollection, targetPackage, progress).copy();
    } catch (CodedRuntimeException exception) {
      throw new CopyFailedException(exception);
    }

    return "true";
  }

  private Package getPackage(String targetPackageId) {
    return targetPackageId != null
        ? metaDataService
            .getPackage(targetPackageId)
            .orElseThrow(() -> new UnknownPackageException(targetPackageId))
        : null;
  }
}
