package org.molgenis.data.rest.copy;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Stream;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.resource.Resource;
import org.molgenis.data.rest.copy.impl.ResourceCopierFactory;
import org.molgenis.jobs.Progress;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@SuppressWarnings({"squid:S1854", "squid:S1481"}) // TODO REMOVE ME
public class CopyServiceImpl implements CopyService {

  private final MetaDataService metaDataService;
  private final PackageMetadata packageMetadata;
  private final ResourceCopierFactory resourceCopierFactory;

  CopyServiceImpl(
      MetaDataService metaDataService,
      PackageMetadata packageMetadata,
      ResourceCopierFactory resourceCopierFactory) {
    this.metaDataService = requireNonNull(metaDataService);
    this.packageMetadata = requireNonNull(packageMetadata);
    this.resourceCopierFactory = requireNonNull(resourceCopierFactory);
  }

  @Override
  @Transactional
  public String copy(Stream<Resource> resources, String targetPackageId, Progress progress) {
    ResourceCollector collector = new ResourceCollector(resources);
    List<Package> packages = collector.getPackages();
    List<EntityType> entityTypes = collector.getEntityTypes();
    Package targetLocation = getPackage(targetPackageId);

    resourceCopierFactory.newInstance(packages, entityTypes, targetLocation).copy();
    return "true";
  }

  public Package getPackage(String id) {
    return metaDataService
        .getPackage(id)
        .orElseThrow(() -> new UnknownEntityException(packageMetadata, id));
  }

  // TODO refactor and make available as utility
  private class ResourceCollector {

    private List<Package> packages;
    private List<EntityType> entityTypes;

    ResourceCollector(Stream<Resource> resources) {
      this.packages = newArrayList();
      this.entityTypes = newArrayList();
      collect(resources);
    }

    private void collect(Stream<Resource> resources) {
      resources.forEach(
          resource -> {
            switch (resource.getType()) {
              case PACKAGE:
                packages.add(getPackage(resource.getId()));
                break;
              case ENTITY_TYPE:
                entityTypes.add(getEntityType(resource.getId()));
                break;
              default:
                throw new UnexpectedEnumException(resource.getType());
            }
          });
    }

    EntityType getEntityType(String id) {
      return metaDataService
          .getEntityType(id)
          .orElseThrow(() -> new UnknownEntityTypeException(id));
    }

    public List<Package> getPackages() {
      return packages;
    }

    public List<EntityType> getEntityTypes() {
      return entityTypes;
    }
  }
}
