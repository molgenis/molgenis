package org.molgenis.data.platform.decorators;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.SystemRepositoryDecoratorFactory;
import org.molgenis.data.SystemRepositoryDecoratorRegistry;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SystemRepositoryDecoratorRegistryImpl implements SystemRepositoryDecoratorRegistry
{
	private final Map<String, SystemRepositoryDecoratorFactory> factories = new HashMap<>();

	@Override
	public synchronized void addFactory(SystemRepositoryDecoratorFactory factory)
	{
		String factoryId = factory.getEntityType().getId();
		factories.put(factoryId, factory);
	}

	@Override
	public synchronized Repository<Entity> decorate(Repository<Entity> repository)
	{
		String factoryId = repository.getEntityType().getId();
		SystemRepositoryDecoratorFactory factory = factories.get(factoryId);
		if (factory != null)
		{
			return factory.createDecoratedRepository(repository);
		}
		return repository;
	}
}
