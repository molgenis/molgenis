package org.molgenis.data.validation.meta;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Validates entity meta before adding or updating the delegated repository
 */
public class EntityTypeRepositoryValidationDecorator extends AbstractRepositoryDecorator<EntityType>
{
	private final Repository<EntityType> decoratedRepo;
	private final EntityTypeValidator entityTypeValidator;

	public EntityTypeRepositoryValidationDecorator(Repository<EntityType> decoratedRepo,
			EntityTypeValidator entityTypeValidator)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.entityTypeValidator = requireNonNull(entityTypeValidator);
	}

	@Override
	public Repository<EntityType> delegate()
	{
		return decoratedRepo;
	}

	@Override
	public void update(EntityType entityType)
	{
		entityTypeValidator.validate(entityType);
		decoratedRepo.update(entityType);
	}

	@Override
	public void update(Stream<EntityType> entities)
	{
		decoratedRepo.update(entities.filter(entityType ->
		{
			entityTypeValidator.validate(entityType);
			return true;
		}));
	}

	@Override
	public void add(EntityType entityType)
	{
		entityTypeValidator.validate(entityType);
		decoratedRepo.add(entityType);
	}

	@Override
	public Integer add(Stream<EntityType> entities)
	{
		return decoratedRepo.add(entities.filter(entityType ->
		{
			entityTypeValidator.validate(entityType);
			return true;
		}));
	}
}
