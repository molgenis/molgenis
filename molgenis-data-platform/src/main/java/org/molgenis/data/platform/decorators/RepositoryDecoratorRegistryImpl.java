package org.molgenis.data.platform.decorators;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.RepositoryDecoratorRegistry;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RepositoryDecoratorRegistryImpl implements RepositoryDecoratorRegistry
{
	private final Map<String, RepositoryDecoratorFactory> factories = new HashMap<>();

	@Override
	public synchronized void addFactory(String entityName, RepositoryDecoratorFactory factory)
	{
		factories.put(entityName, factory);
	}

	@Override
	public synchronized Repository<Entity> decorate(Repository<Entity> repository)
	{
		RepositoryDecoratorFactory factory = factories.get(repository.getName());
		if (factory != null)
		{
			return factory.createDecoratedRepository(repository);
		}

		return repository;
	}
}
