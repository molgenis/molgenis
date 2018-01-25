package org.molgenis.data.decorator;

import org.molgenis.data.*;
import org.molgenis.data.decorator.meta.DecoratorConfiguration;
import org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata;
import org.molgenis.data.decorator.meta.DynamicDecorator;
import org.molgenis.data.support.QueryImpl;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.ENTITY_TYPE_ID;

@Component
public class DynamicRepositoryDecoratorRegistryImpl implements DynamicRepositoryDecoratorRegistry
{
	private final Map<String, DynamicRepositoryDecoratorFactory> factories = new HashMap<>();
	private final DataService dataService;
	private final RepositoryCollection repositoryCollection;
	private final DecoratorConfigurationMetadata decoratorConfigurationMetadata;

	public DynamicRepositoryDecoratorRegistryImpl(DataService dataService, RepositoryCollection repositoryCollection,
			DecoratorConfigurationMetadata decoratorConfigurationMetadata)
	{
		this.dataService = requireNonNull(dataService);
		this.repositoryCollection = requireNonNull(repositoryCollection);
		this.decoratorConfigurationMetadata = requireNonNull(decoratorConfigurationMetadata);
	}

	@Override
	public synchronized void addFactory(DynamicRepositoryDecoratorFactory factory)
	{
		String factoryId = factory.getId();
		factories.put(factoryId, factory);
	}

	@Override
	public Stream<String> getFactoryIds()
	{
		return factories.keySet().stream();
	}

	@Override
	public synchronized Repository<Entity> decorate(Repository<Entity> repository)
	{
		String entityTypeId = repository.getEntityType().getId();

		if (!entityTypeId.equals(DECORATOR_CONFIGURATION) && repositoryCollection.hasRepository(
				decoratorConfigurationMetadata))
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
			else
			{
				//FIXME: throw appropriate exception
			}
		}
		return repository;
	}
}