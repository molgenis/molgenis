package org.molgenis.data.decorator;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.decorator.meta.DecoratorConfiguration;
import org.molgenis.data.decorator.meta.DynamicDecorator;
import org.molgenis.data.event.BootstrappingEvent;
import org.molgenis.data.support.QueryImpl;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.ENTITY_TYPE_ID;
import static org.molgenis.data.event.BootstrappingEvent.BootstrappingStatus.FINISHED;

@Component
public class DynamicRepositoryDecoratorRegistryImpl implements DynamicRepositoryDecoratorRegistry
{
	private final Map<String, DynamicRepositoryDecoratorFactory> factories = new HashMap<>();
	private final DataService dataService;
	private boolean bootstrappingDone = false;

	public DynamicRepositoryDecoratorRegistryImpl(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
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
		List<DynamicDecorator> decorators = configuration.getDecorators().collect(toList());
		if (decorators.isEmpty())
		{
			return repository;
		}

		for (DynamicDecorator decorator : decorators)
		{
			DynamicRepositoryDecoratorFactory factory = factories.get(decorator.getId());
			if (factory != null)
			{
				repository = factory.createDecoratedRepository(repository);
			}
		}
		return repository;
	}

	@EventListener
	public void onApplicationEvent(BootstrappingEvent bootstrappingEvent)
	{
		this.bootstrappingDone = bootstrappingEvent.getStatus() == FINISHED;
	}
}