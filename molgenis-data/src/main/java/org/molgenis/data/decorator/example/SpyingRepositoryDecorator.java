package org.molgenis.data.decorator.example;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

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
		String username = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
		LOG.info("{} changed entity [{}] in entity type [{}]", username, id, entityTypeId);
	}
}