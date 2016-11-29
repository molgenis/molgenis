package org.molgenis.data.listeners;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class EntityListenerRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
	private final Repository<Entity> decoratedRepository;
	private final EntityListenersService entityListenersService;

	public EntityListenerRepositoryDecorator(Repository<Entity> decoratedRepository,
			EntityListenersService entityListenersService)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
		requireNonNull(entityListenersService).register(decoratedRepository.getName());
		this.entityListenersService = entityListenersService;
	}

	@Override
	protected Repository<Entity> delegate()
	{
		return decoratedRepository;
	}

	@Override
	public void update(Entity entity)
	{
		entityListenersService.updateEntity(decoratedRepository.getName(), entity);
		decoratedRepository.update(entity);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		entities = entityListenersService.updateEntities(decoratedRepository.getName(), entities);
		decoratedRepository.update(entities);
	}
}
