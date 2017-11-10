package org.molgenis.data.decorator.example;

import org.molgenis.data.Repository;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorFactory;
import org.springframework.stereotype.Component;

@Component
public class TimestampRepositoryDecoratorFactory implements DynamicRepositoryDecoratorFactory
{
	private static final String NAME = "timestamp";

	@Override
	@SuppressWarnings("unchecked")
	public Repository createDecoratedRepository(Repository repository)
	{
		return new TimestampRepositoryDecorator(repository);
	}

	@Override
	public String getName()
	{
		return NAME;
	}
}
