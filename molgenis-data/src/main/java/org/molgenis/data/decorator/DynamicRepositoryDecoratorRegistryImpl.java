package org.molgenis.data.decorator;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.ENTITY_TYPE_ID;
import static org.molgenis.data.event.BootstrappingEvent.BootstrappingStatus.FINISHED;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.decorator.meta.DecoratorConfiguration;
import org.molgenis.data.decorator.meta.DecoratorParameters;
import org.molgenis.data.decorator.meta.DynamicDecorator;
import org.molgenis.data.event.BootstrappingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DynamicRepositoryDecoratorRegistryImpl implements DynamicRepositoryDecoratorRegistry {
  private final Map<String, DynamicRepositoryDecoratorFactory> factories = new HashMap<>();
  private final DataService dataService;
  private final Gson gson;
  private boolean bootstrappingDone = false;

  private static final Type MAP_TOKEN = new TypeToken<Map<String, Object>>() {}.getType();

  DynamicRepositoryDecoratorRegistryImpl(DataService dataService, Gson gson) {
    this.dataService = requireNonNull(dataService);
    this.gson = requireNonNull(gson);
  }

  @Override
  public synchronized void addFactory(DynamicRepositoryDecoratorFactory factory) {
    String factoryId = factory.getId();
    if (factories.containsKey(factoryId)) {
      throw new IllegalArgumentException(format("Duplicate decorator id [%s]", factoryId));
    }
    factories.put(factoryId, factory);
  }

  @Override
  public Stream<String> getFactoryIds() {
    return factories.keySet().stream();
  }

  @Override
  public DynamicRepositoryDecoratorFactory getFactory(String id) {
    if (!factories.containsKey(id)) {
      throw new IllegalArgumentException(format("Decorator [%s] does not exist", id));
    }
    return factories.get(id);
  }

  /**
   * Decorates a {@link Repository} if there is a {@link DecoratorConfiguration} specified for this
   * repository.
   */
  @Override
  public synchronized Repository<Entity> decorate(Repository<Entity> repository) {
    String entityTypeId = repository.getEntityType().getId();

    if (!entityTypeId.equals(DECORATOR_CONFIGURATION) && bootstrappingDone) {
      DecoratorConfiguration config =
          dataService
              .query(DECORATOR_CONFIGURATION, DecoratorConfiguration.class)
              .eq(ENTITY_TYPE_ID, entityTypeId)
              .findOne();

      if (config != null) {
        repository = decorateRepository(repository, config);
      }
    }

    return repository;
  }

  /**
   * Decorates a {@link Repository} with one or more {@link DynamicDecorator}s., based on the {@link
   * DecoratorConfiguration} entity.
   */
  @SuppressWarnings("unchecked")
  private Repository<Entity> decorateRepository(
      Repository<Entity> repository, DecoratorConfiguration configuration) {
    Map<String, Map<String, Object>> parameterMap = getParameterMap(configuration);
    List<DecoratorParameters> decoratorParameters =
        configuration.getDecoratorParameters().collect(toList());
    for (DecoratorParameters decoratorParam : decoratorParameters) {
      DynamicRepositoryDecoratorFactory factory =
          factories.get(decoratorParam.getDecorator().getId());
      if (factory != null) {
        repository =
            factory.createDecoratedRepository(repository, parameterMap.get(factory.getId()));
      }
    }
    return repository;
  }

  /**
   * Collects the JSON parameters of one or more DecoratorParameters entities in a map, with the
   * decorator's ID as the key and the JSON object as another key/value Map.
   */
  Map<String, Map<String, Object>> getParameterMap(DecoratorConfiguration configuration) {
    return configuration
        .getDecoratorParameters()
        .collect(
            toMap(
                param -> param.getDecorator().getId(), param -> jsonToMap(param.getParameters())));
  }

  private Map<String, Object> jsonToMap(String parameterJson) {
    if (parameterJson != null) {
      return gson.fromJson(parameterJson, MAP_TOKEN);
    } else {
      return emptyMap();
    }
  }

  @EventListener
  public void onApplicationEvent(BootstrappingEvent bootstrappingEvent) {
    this.bootstrappingDone = bootstrappingEvent.getStatus() == FINISHED;
  }
}
