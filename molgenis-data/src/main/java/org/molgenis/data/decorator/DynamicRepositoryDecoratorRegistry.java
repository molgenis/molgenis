package org.molgenis.data.decorator;

import java.util.stream.Stream;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;

public interface DynamicRepositoryDecoratorRegistry {
  void addFactory(DynamicRepositoryDecoratorFactory factory);

  Stream<String> getFactoryIds();

  DynamicRepositoryDecoratorFactory getFactory(String id);

  Repository<Entity> decorate(Repository<Entity> repository);
}
