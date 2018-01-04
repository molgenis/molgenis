package org.molgenis.data.decorator.example;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;

import java.time.ZonedDateTime;

public class TimestampRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
	private static final String TIMESTAMP = "TIMESTAMP";

	public TimestampRepositoryDecorator(Repository<Entity> delegateRepository)
	{
		super(delegateRepository);
	}

	@Override
	public void update(Entity entity)
	{
		Attribute timestampAttribute = entity.getEntityType().getAttribute(TIMESTAMP);
		if (timestampAttribute != null)
		{
			entity.set(TIMESTAMP, ZonedDateTime.now().toInstant());
		}
		super.update(entity);
	}

	@Override
	public void add(Entity entity)
	{
		Attribute timestampAttribute = entity.getEntityType().getAttribute(TIMESTAMP);
		if (timestampAttribute != null)
		{
			entity.set(TIMESTAMP, ZonedDateTime.now().toInstant());
		}
		super.add(entity);
	}
}