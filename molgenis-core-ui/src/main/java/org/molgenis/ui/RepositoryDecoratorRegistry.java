package org.molgenis.ui;

import java.util.HashMap;
import java.util.Map;

import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;

public class RepositoryDecoratorRegistry
{
	private final Map<String, RepositoryDecoratorFactory> factories = new HashMap<>();

	public synchronized void addFactory(String entityName, RepositoryDecoratorFactory factory)
	{
		factories.put(entityName, factory);
	}

	public synchronized Repository decorate(Repository repository)
	{
		RepositoryDecoratorFactory factory = factories.get(repository.getName());
		if (factory != null)
		{
			return factory.createDecoratedRepository(repository);
		}

		return repository;
	}
}
