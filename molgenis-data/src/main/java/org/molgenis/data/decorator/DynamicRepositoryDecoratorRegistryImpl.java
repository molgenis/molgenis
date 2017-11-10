package org.molgenis.data.decorator;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DynamicRepositoryDecoratorRegistryImpl implements DynamicRepositoryDecoratorRegistry
{
	private final Map<String, DynamicRepositoryDecoratorFactory> factories = new HashMap<>();

	@Override
	public synchronized void addFactory(DynamicRepositoryDecoratorFactory factory)
	{
		String factoryId = factory.getEntityType().getId();
		factories.put(factoryId, factory);
	}

	@Override
	public synchronized Repository<Entity> decorate(Repository<Entity> repository)
	{
		String factoryId = repository.getEntityType().getId();
		DynamicRepositoryDecoratorFactory factory = factories.get(factoryId);
		if (factory != null)
		{
			return factory.createDecoratedRepository(repository);
		}
		return repository;
	}
}