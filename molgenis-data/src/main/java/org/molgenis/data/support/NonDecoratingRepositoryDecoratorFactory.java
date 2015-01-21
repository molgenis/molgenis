package org.molgenis.data.support;

import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;

public class NonDecoratingRepositoryDecoratorFactory implements RepositoryDecoratorFactory
{
	@Override
	public Repository createDecoratedRepository(Repository repository)
	{
		return repository;
	}

}
