package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;

/**
 * RepositoryDecoratorFactory that just returns the repository.
 * 
 * Useful for testing
 */
public class NonDecoratingRepositoryDecoratorFactory implements RepositoryDecoratorFactory
{
	@Override
	public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
	{
		return repository;
	}

	@Override
	public <E extends Entity> Repository<E> createDecoratedRepository(Repository<E> repository, Class<E> clazz)
	{
		return repository;
	}
}
