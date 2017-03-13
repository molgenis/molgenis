package org.molgenis.data.platform.decorators;

import org.molgenis.data.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RepositoryDecoratorRegistryImpl implements RepositoryDecoratorRegistry
{
	private final Map<String, StaticEntityRepositoryDecoratorFactory> factories = new HashMap<>();

	@Override
	public synchronized void addFactory(StaticEntityRepositoryDecoratorFactory factory)
	{
		String factoryId = factory.getEntityType().getId();
		factories.put(factoryId, factory);
	}

	@Override
	public synchronized Repository<Entity> decorate(Repository<Entity> repository)
	{
		String factoryId = repository.getEntityType().getId();
		EntityTypeRepositoryDecoratorFactory factory = factories.get(factoryId);
		if (factory != null)
		{
			return factory.createDecoratedRepository(repository);
		}
		return repository;
	}
}
