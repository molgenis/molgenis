package org.molgenis.data.decorator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.decorator.meta.DecoratorConfiguration;
import org.molgenis.data.decorator.meta.DecoratorParameters;
import org.molgenis.data.event.BootstrappingEvent;
import org.molgenis.data.support.QueryImpl;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.ENTITY_TYPE_ID;
import static org.molgenis.data.event.BootstrappingEvent.BootstrappingStatus.FINISHED;

@Component
public class DynamicRepositoryDecoratorRegistryImpl implements DynamicRepositoryDecoratorRegistry
{
	private final Map<String, DynamicRepositoryDecoratorFactory> factories = new HashMap<>();
	private final DataService dataService;
	private final Gson gson;
	private boolean bootstrappingDone = false;

	private static final Type MAP_TOKEN = new TypeToken<Map<String, Object>>()
	{
	}.getType();

	public DynamicRepositoryDecoratorRegistryImpl(DataService dataService, Gson gson)
	{
		this.dataService = requireNonNull(dataService);
		this.gson = requireNonNull(gson);
	}

	@Override
	public synchronized void addFactory(DynamicRepositoryDecoratorFactory factory)
	{
		String factoryId = factory.getId();
		if (factories.containsKey(factoryId))
		{
			throw new IllegalStateException(String.format("Duplicate decorator id [%s]", factoryId));
		}
		factories.put(factoryId, factory);
	}

	@Override
	public Stream<String> getFactoryIds()
	{
		return factories.keySet().stream();
	}

	@Override
	public DynamicRepositoryDecoratorFactory getFactory(String id)
	{
		return factories.get(id);
	}

	@Override
	public synchronized Repository<Entity> decorate(Repository<Entity> repository)
	{
		String entityTypeId = repository.getEntityType().getId();

		if (!entityTypeId.equals(DECORATOR_CONFIGURATION) && bootstrappingDone)
		{
			Query query = new QueryImpl().eq(ENTITY_TYPE_ID, entityTypeId);
			@SuppressWarnings("unchecked")
			DecoratorConfiguration configuration = dataService.findOne(DECORATOR_CONFIGURATION, query,
					DecoratorConfiguration.class);

			if (configuration != null)
			{
				repository = decorateRepository(repository, configuration);
			}
		}

		return repository;
	}

	@SuppressWarnings("unchecked")
	private Repository<Entity> decorateRepository(Repository<Entity> repository, DecoratorConfiguration configuration)
	{
		List<DecoratorParameters> decoratorParameters = configuration.getDecoratorParameters().collect(toList());
		if (decoratorParameters.isEmpty())
		{
			return repository;
		}

		Map<String, Map<String, Object>> parameters = configuration.getDecoratorParameters()
																   .collect(toMap(param -> param.getDecorator().getId(),
																		   param -> toParameterMap(
																				   param.getParameters())));

		for (DecoratorParameters decoratorParam : decoratorParameters)
		{
			DynamicRepositoryDecoratorFactory factory = factories.get(decoratorParam.getDecorator().getId());
			if (factory != null)
			{
				repository = factory.createDecoratedRepository(repository, parameters.get(factory.getId()));
			}
		}
		return repository;
	}

	private Map<String, Object> toParameterMap(String parameterJson)
	{
		if (parameterJson != null)
		{
			return gson.fromJson(parameterJson, MAP_TOKEN);
		}
		else
		{
			return emptyMap();
		}
	}

	@EventListener
	public void onApplicationEvent(BootstrappingEvent bootstrappingEvent)
	{
		this.bootstrappingDone = bootstrappingEvent.getStatus() == FINISHED;
	}
}