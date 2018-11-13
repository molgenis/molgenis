package org.molgenis.navigator;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;

import java.util.List;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;

/**
 * Takes {@link Resource}s (which are just IDs) and gets the actual underlying resources: ({@link
 * Package}s and {@link EntityType}s).
 */
@Component
public class ResourceCollector {

  private final MetaDataService metaDataService;

  public ResourceCollector(MetaDataService metaDataService) {
    this.metaDataService = requireNonNull(metaDataService);
  }

  private Package getPackage(String id) {
    return metaDataService
        .getPackage(id)
        .orElseThrow(() -> new UnknownEntityException(PackageMetadata.PACKAGE, id));
  }

  private EntityType getEntityType(String id) {
    return metaDataService.getEntityType(id).orElseThrow(() -> new UnknownEntityTypeException(id));
  }

  public ResourceCollection get(List<Resource> resources) {
    List<Package> packages = newArrayList();
    List<EntityType> entityTypes = newArrayList();

    resources.forEach(
        resource -> {
          switch (resource.getType()) {
            case PACKAGE:
              packages.add(getPackage(resource.getId()));
              break;
            case ENTITY_TYPE:
            case ENTITY_TYPE_ABSTRACT:
              entityTypes.add(getEntityType(resource.getId()));
              break;
            default:
              throw new UnexpectedEnumException(resource.getType());
          }
        });
    return ResourceCollection.of(packages, entityTypes);
  }
}
