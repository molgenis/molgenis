package org.molgenis.data.rest.copy.impl;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.populate.IdGenerator;
import org.springframework.stereotype.Component;

@Component
public class ResourceCopierFactory {

  private final DataService dataService;
  private final IdGenerator idGenerator;
  private final PackageMetadata packageMetadata;
  private final EntityTypeDependencyResolver entityTypeDependencyResolver;
  private final AttributeFactory attributeFactory;

  public ResourceCopierFactory(
      DataService dataService,
      IdGenerator idGenerator,
      PackageMetadata packageMetadata,
      EntityTypeDependencyResolver entityTypeDependencyResolver,
      AttributeFactory attributeFactory) {
    this.dataService = requireNonNull(dataService);
    this.idGenerator = requireNonNull(idGenerator);
    this.packageMetadata = requireNonNull(packageMetadata);
    this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
    this.attributeFactory = requireNonNull(attributeFactory);
  }

  public ResourceCopier newInstance(
      List<Package> packages, List<EntityType> entityTypes, Package targetLocation) {
    return new ResourceCopier(
        packages,
        entityTypes,
        targetLocation,
        dataService,
        idGenerator,
        packageMetadata,
        entityTypeDependencyResolver,
        attributeFactory);
  }
}
