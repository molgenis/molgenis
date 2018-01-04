package org.molgenis.data.decorator.example;

import org.molgenis.data.Repository;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorFactory;
import org.springframework.stereotype.Component;

@Component
public class SpyingRepositoryDecoratorFactory implements DynamicRepositoryDecoratorFactory
{
	private static final String NAME = "spy";

	@Override
	@SuppressWarnings("unchecked")
	public Repository createDecoratedRepository(Repository repository)
	{
		return new SpyingRepositoryDecorator(repository);
	}

	@Override
	public String getName()
	{
		return NAME;
	}
}
