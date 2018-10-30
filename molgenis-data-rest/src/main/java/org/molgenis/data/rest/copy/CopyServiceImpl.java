package org.molgenis.data.rest.copy;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.resource.Resource;
import org.molgenis.data.resource.ResourceCollection;
import org.molgenis.data.resource.ResourceCollector;
import org.molgenis.data.rest.copy.impl.ResourceCopierFactory;
import org.molgenis.jobs.Progress;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
  @Transactional
  public String copy(List<Resource> resources, String targetPackageId, Progress progress) {
    ResourceCollection resourceCollection = resourceCollector.get(resources);
    Package targetLocation = resourceCollector.getPackage(targetPackageId);

    resourceCopierFactory.newInstance(resourceCollection, targetLocation).copy();
    return "true";
  }
}
