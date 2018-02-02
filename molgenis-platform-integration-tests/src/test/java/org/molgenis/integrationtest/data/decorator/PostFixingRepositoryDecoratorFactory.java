package org.molgenis.integrationtest.data.decorator;

import org.molgenis.data.Repository;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorFactory;
import org.springframework.stereotype.Component;

@Component
public class PostFixingRepositoryDecoratorFactory implements DynamicRepositoryDecoratorFactory
{
	private static final String ID = "postfix";

	@Override
	@SuppressWarnings("unchecked")
	public Repository createDecoratedRepository(Repository repository)
	{
		return new PostFixingRepositoryDecorator(repository);
	}

	@Override
	public String getId()
	{
		return ID;
	}

	@Override
	public String getLabel()
	{
		return "postfix";
	}

	@Override
	public String getDescription()
	{
		return "This is a test decorator";
	}

}
