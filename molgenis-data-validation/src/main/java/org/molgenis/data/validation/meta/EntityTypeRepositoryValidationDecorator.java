package org.molgenis.data.validation.meta;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityMetaData;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Validates entity meta before adding or updating the delegated repository
 */
public class EntityTypeRepositoryValidationDecorator extends AbstractRepositoryDecorator<EntityMetaData>
{
	private final Repository<EntityMetaData> decoratedRepo;
	private final EntityMetaDataValidator entityMetaDataValidator;

	public EntityTypeRepositoryValidationDecorator(Repository<EntityMetaData> decoratedRepo,
			EntityMetaDataValidator entityMetaDataValidator)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.entityMetaDataValidator = requireNonNull(entityMetaDataValidator);
	}

	@Override
	public Repository<EntityMetaData> delegate()
	{
		return decoratedRepo;
	}

	@Override
	public void update(EntityMetaData entityMeta)
	{
		entityMetaDataValidator.validate(entityMeta);
		decoratedRepo.update(entityMeta);
	}

	@Override
	public void update(Stream<EntityMetaData> entities)
	{
		decoratedRepo.update(entities.filter(entityMeta ->
		{
			entityMetaDataValidator.validate(entityMeta);
			return true;
		}));
	}

	@Override
	public void add(EntityMetaData entityMeta)
	{
		entityMetaDataValidator.validate(entityMeta);
		decoratedRepo.add(entityMeta);
	}

	@Override
	public Integer add(Stream<EntityMetaData> entities)
	{
		return decoratedRepo.add(entities.filter(entityMeta ->
		{
			entityMetaDataValidator.validate(entityMeta);
			return true;
		}));
	}
}
