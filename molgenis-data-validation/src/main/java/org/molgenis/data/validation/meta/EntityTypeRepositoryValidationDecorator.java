package org.molgenis.data.validation.meta;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.data.validation.constraint.EntityTypeValidationResult;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Validates entity meta before adding or updating the delegated repository
 */
public class EntityTypeRepositoryValidationDecorator extends AbstractRepositoryDecorator<EntityType>
{
	private final EntityTypeValidator entityTypeValidator;

	public EntityTypeRepositoryValidationDecorator(Repository<EntityType> delegateRepository,
			EntityTypeValidator entityTypeValidator)
	{
		super(delegateRepository);
		this.entityTypeValidator = requireNonNull(entityTypeValidator);
	}

	@Override
	public void update(EntityType entityType)
	{
		validate(entityType);
		delegate().update(entityType);
	}

	@Override
	public void update(Stream<EntityType> entities)
	{
		delegate().update(entities.filter(entityType ->
		{
			validate(entityType);
			return true;
		}));
	}

	@Override
	public void add(EntityType entityType)
	{
		validate(entityType);
		delegate().add(entityType);
	}

	@Override
	public Integer add(Stream<EntityType> entities)
	{
		return delegate().add(entities.filter(entityType ->
		{
			validate(entityType);
			return true;
		}));
	}

	private void validate(EntityType entityType)
	{
		EntityTypeValidationResult validationResult = entityTypeValidator.validate(entityType);
		if (validationResult.hasConstraintViolations())
		{
			throw new ValidationException(validationResult);
		}
	}
}
