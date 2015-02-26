package org.molgenis.data.support;

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
	public Repository createDecoratedRepository(Repository repository)
	{
		return repository;
	}
}
