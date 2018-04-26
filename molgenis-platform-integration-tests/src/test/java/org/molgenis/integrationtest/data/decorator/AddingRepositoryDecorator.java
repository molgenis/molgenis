package org.molgenis.integrationtest.data.decorator;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Decorator specifically for DynamicDecoratorIT
public class AddingRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
	private static final Logger LOG = LoggerFactory.getLogger(AddingRepositoryDecorator.class);

	public AddingRepositoryDecorator(Repository<Entity> delegateRepository)
	{
		super(delegateRepository);
	}

	@Override
	public void update(Entity entity)
	{
		entity.set("int_attr", entity.getInt("int_attr") + 1);
		super.update(entity);
	}
}