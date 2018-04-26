package org.molgenis.data.listeners;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class EntityListenerRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
	private final EntityListenersService entityListenersService;

	public EntityListenerRepositoryDecorator(Repository<Entity> delegateRepository,
			EntityListenersService entityListenersService)
	{
		super(delegateRepository);
		requireNonNull(entityListenersService).register(delegate().getName());
		this.entityListenersService = entityListenersService;
	}

	@Override
	public void update(Entity entity)
	{
		entityListenersService.updateEntity(delegate().getName(), entity);
		delegate().update(entity);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		entities = entityListenersService.updateEntities(delegate().getName(), entities);
		delegate().update(entities);
	}
}
