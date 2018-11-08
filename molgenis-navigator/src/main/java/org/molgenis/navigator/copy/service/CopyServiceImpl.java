package org.molgenis.navigator.copy.service;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.resource.Resource;
import org.molgenis.data.resource.ResourceCollection;
import org.molgenis.data.resource.ResourceCollector;
import org.molgenis.jobs.Progress;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("unused")
@Component
public class CopyServiceImpl implements CopyService {

  private final ResourceCopierFactory resourceCopierFactory;
  private final ResourceCollector resourceCollector;

  CopyServiceImpl(
      ResourceCopierFactory resourceCopierFactory, ResourceCollector resourceCollector) {
    this.resourceCopierFactory = requireNonNull(resourceCopierFactory);
    this.resourceCollector = requireNonNull(resourceCollector);
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public String copy(List<Resource> resources, String targetPackageId, Progress progress) {
    ResourceCollection resourceCollection = resourceCollector.get(resources);

    Package targetPackage =
        targetPackageId != null ? resourceCollector.getPackage(targetPackageId) : null;

    resourceCopierFactory.newInstance(resourceCollection, targetPackage, progress).copy();
    return "true";
  }
}
