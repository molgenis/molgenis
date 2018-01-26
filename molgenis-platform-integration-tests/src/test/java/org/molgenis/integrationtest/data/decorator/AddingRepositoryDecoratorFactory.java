package org.molgenis.integrationtest.data.decorator;

import org.molgenis.data.Repository;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorFactory;
import org.springframework.stereotype.Component;

@Component
public class AddingRepositoryDecoratorFactory implements DynamicRepositoryDecoratorFactory
{
	private static final String ID = "add";

	@Override
	@SuppressWarnings("unchecked")
	public Repository createDecoratedRepository(Repository repository)
	{
		return new AddingRepositoryDecorator(repository);
	}

	@Override
	public String getId()
	{
		return ID;
	}

	@Override
	public String getLabel()
	{
		return "add";
	}

	@Override
	public String getDescription()
	{
		return "This is a test decorator";
	}
}
