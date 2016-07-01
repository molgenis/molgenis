package org.molgenis.data.platform.decorators;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RepositoryDecoratorRegistry
{
	private final Map<String, RepositoryDecoratorFactory> factories = new HashMap<>();

	public synchronized void addFactory(String entityName, RepositoryDecoratorFactory factory)
	{
		factories.put(entityName, factory);
	}

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
