package org.molgenis.data.decorator.example;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpyingRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
	private static final Logger LOG = LoggerFactory.getLogger(SpyingRepositoryDecorator.class);

	public SpyingRepositoryDecorator(Repository<Entity> delegateRepository)
	{
		super(delegateRepository);
	}

	@Override
	public void update(Entity entity)
	{
		Object id = entity.getIdValue();
		super.update(entity);
		catchPerpetrator(entity.getEntityType().getId(), id);
	}

	private void catchPerpetrator(String entityTypeId, Object id)
	{
		LOG.info("TEST");
	}
}