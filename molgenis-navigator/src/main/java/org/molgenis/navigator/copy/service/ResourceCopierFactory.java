package org.molgenis.navigator.copy.service;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.jobs.Progress;
import org.molgenis.navigator.model.util.ResourceCollection;
import org.springframework.stereotype.Component;

@Component
public class ResourceCopierFactory {

  private final DataService dataService;
  private final IdGenerator idGenerator;
  private final EntityTypeDependencyResolver entityTypeDependencyResolver;
  private final AttributeFactory attributeFactory;

  public ResourceCopierFactory(
      DataService dataService,
      IdGenerator idGenerator,
      EntityTypeDependencyResolver entityTypeDependencyResolver,
      AttributeFactory attributeFactory) {
    this.dataService = requireNonNull(dataService);
    this.idGenerator = requireNonNull(idGenerator);
    this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
    this.attributeFactory = requireNonNull(attributeFactory);
  }

  ResourceCopier newInstance(
      ResourceCollection resourceCollection, @Nullable Package targetPackage, Progress progress) {
    return new ResourceCopier(
        resourceCollection,
        targetPackage,
        progress,
        dataService,
        idGenerator,
        entityTypeDependencyResolver,
        attributeFactory);
  }
}
