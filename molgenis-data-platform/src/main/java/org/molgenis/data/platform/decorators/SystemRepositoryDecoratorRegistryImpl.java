package org.molgenis.data.platform.decorators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.SystemRepositoryDecoratorFactory;
import org.molgenis.data.SystemRepositoryDecoratorRegistry;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.stereotype.Component;

@Component
public class SystemRepositoryDecoratorRegistryImpl implements SystemRepositoryDecoratorRegistry {
  private final Map<String, SystemRepositoryDecoratorFactory> factories = new HashMap<>();

  @Override
  public synchronized void addFactory(SystemRepositoryDecoratorFactory factory) {
    String factoryId = factory.getEntityType().getId();
    factories.put(factoryId, factory);
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized Repository<Entity> decorate(Repository<Entity> repository) {
    Repository<Entity> decoratedRepository = repository;
    for (String factoryId : getFactoryIds(repository)) {
      SystemRepositoryDecoratorFactory factory = factories.get(factoryId);
      if (factory != null) {
        decoratedRepository = factory.createDecoratedRepository(decoratedRepository);
      }
    }
    return decoratedRepository;
  }

  private List<String> getFactoryIds(Repository<Entity> repository) {
    List<String> factoryIds = new ArrayList<>();
    EntityType entityType = repository.getEntityType();
    do {
      factoryIds.add(entityType.getId());
      entityType = entityType.getExtends();
    } while (entityType != null);
    Collections.reverse(factoryIds);
    return factoryIds;
  }
}
