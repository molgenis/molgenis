package org.molgenis.data.decorator;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DynamicRepositoryDecoratorFactoryRegistrar {
  private final DynamicRepositoryDecoratorRegistry repositoryDecoratorRegistry;

  DynamicRepositoryDecoratorFactoryRegistrar(
      DynamicRepositoryDecoratorRegistry repositoryDecoratorRegistry) {
    this.repositoryDecoratorRegistry = requireNonNull(repositoryDecoratorRegistry);
  }

  public void register(ApplicationContext context) {
    Map<String, DynamicRepositoryDecoratorFactory> repositoryDecoratorFactoryMap =
        context.getBeansOfType(DynamicRepositoryDecoratorFactory.class);
    repositoryDecoratorFactoryMap.values().forEach(repositoryDecoratorRegistry::addFactory);
  }
}
