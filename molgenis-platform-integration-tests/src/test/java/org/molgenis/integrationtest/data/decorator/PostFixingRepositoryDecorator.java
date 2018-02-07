package org.molgenis.integrationtest.data.decorator;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Decorator specifically for DynamicDecoratorIT
public class PostFixingRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
	private static final Logger LOG = LoggerFactory.getLogger(PostFixingRepositoryDecorator.class);

	public PostFixingRepositoryDecorator(Repository<Entity> delegateRepository)
	{
		super(delegateRepository);
	}

	@Override
	public void update(Entity entity)
	{
		entity.set("string_attr", entity.getString("string_attr") + "_TEST");
		super.update(entity);
	}
}