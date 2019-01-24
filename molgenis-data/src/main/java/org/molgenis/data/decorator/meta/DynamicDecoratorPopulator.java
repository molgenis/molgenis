package org.molgenis.data.decorator.meta;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.PARAMETERS;
import static org.molgenis.data.decorator.meta.DecoratorParametersMetadata.DECORATOR_PARAMETERS;
import static org.molgenis.data.decorator.meta.DynamicDecoratorMetadata.DYNAMIC_DECORATOR;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorFactory;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorRegistry;
import org.springframework.stereotype.Component;

@Component
public class DynamicDecoratorPopulator {
  private final DataService dataService;
  private final DynamicRepositoryDecoratorRegistry registry;
  private final DynamicDecoratorFactory dynamicDecoratorFactory;

  DynamicDecoratorPopulator(
      DataService dataService,
      DynamicRepositoryDecoratorRegistry dynamicRepositoryDecoratorRegistry,
      DynamicDecoratorFactory dynamicDecoratorFactory) {
    this.dataService = requireNonNull(dataService);
    this.registry = requireNonNull(dynamicRepositoryDecoratorRegistry);
    this.dynamicDecoratorFactory = requireNonNull(dynamicDecoratorFactory);
  }

  public void populate() {
    addNewDecorators();
    removeNonExistingDecorators();
  }

  private void addNewDecorators() {
    dataService.add(
        DYNAMIC_DECORATOR,
        registry.getFactoryIds().filter(this::notPersisted).map(this::createDecorator));
  }

  @SuppressWarnings("unchecked")
  private void removeNonExistingDecorators() {
    Set<String> nonExistingDecorators = getNonExistingDecorators();
    updateReferringEntities(nonExistingDecorators);
    dataService.deleteAll(DYNAMIC_DECORATOR, nonExistingDecorators.stream().map(id -> (Object) id));
  }

  private Set<String> getNonExistingDecorators() {
    Set<String> decorators =
        dataService
            .findAll(DYNAMIC_DECORATOR, DynamicDecorator.class)
            .map(DynamicDecorator::getId)
            .collect(toSet());

    decorators.removeAll(registry.getFactoryIds().collect(toSet()));
    return decorators;
  }

  private void updateReferringEntities(Set<String> nonExistingDecorators) {
    List<Object> paramsToDelete = getDecoratorParametersToDelete(nonExistingDecorators);
    Stream<DecoratorConfiguration> updatedConfigs =
        removeParametersFromConfigurations(paramsToDelete);
    dataService.update(DECORATOR_CONFIGURATION, updatedConfigs);
    dataService.deleteAll(DECORATOR_PARAMETERS, paramsToDelete.stream());
  }

  private List<Object> getDecoratorParametersToDelete(Set<String> nonExistingDecorators) {
    return dataService
        .findAll(DECORATOR_PARAMETERS, DecoratorParameters.class)
        .filter(
            decoratorParameters ->
                nonExistingDecorators.contains(decoratorParameters.getDecorator().getId()))
        .map(Entity::getIdValue)
        .collect(toList());
  }

  private Stream<DecoratorConfiguration> removeParametersFromConfigurations(
      List<Object> paramsToDelete) {
    return dataService
        .findAll(DECORATOR_CONFIGURATION, DecoratorConfiguration.class)
        .map(config -> removeReferencesOrDeleteIfEmpty(paramsToDelete, config))
        .filter(Objects::nonNull);
  }

  /**
   * Removes references to DecoratorParameters that will be deleted. If this results in a
   * DecoratorConfiguration without any parameters, then the row is deleted.
   *
   * @return DecoratorConfiguration without references to DecoratorParameters that will be deleted,
   *     null if the row was deleted
   */
  DecoratorConfiguration removeReferencesOrDeleteIfEmpty(
      List<Object> decoratorParametersToRemove, DecoratorConfiguration configuration) {
    List<DecoratorParameters> decoratorParameters =
        stream(
                configuration.getEntities(PARAMETERS, DecoratorParameters.class).spliterator(),
                false)
            .filter(parameters -> !decoratorParametersToRemove.contains(parameters.getId()))
            .collect(toList());

    if (decoratorParameters.isEmpty()) {
      dataService.deleteById(DECORATOR_CONFIGURATION, configuration.getIdValue());
      return null;
    } else {
      configuration.setDecoratorParameters(decoratorParameters.stream());
      return configuration;
    }
  }

  private boolean notPersisted(String id) {
    return dataService.findOneById(DYNAMIC_DECORATOR, id, DynamicDecorator.class) == null;
  }

  private DynamicDecorator createDecorator(String id) {
    DynamicRepositoryDecoratorFactory factory = registry.getFactory(id);
    return dynamicDecoratorFactory
        .create(id)
        .setLabel(factory.getLabel())
        .setDescription(factory.getDescription())
        .setSchema(factory.getSchema());
  }
}
